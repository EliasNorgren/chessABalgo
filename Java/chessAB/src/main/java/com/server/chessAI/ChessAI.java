package com.server.chessAI;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.server.chessAI.TranspositionEntry.BoundType;
import com.server.externalEval.cuckoochess.Evaluate;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.github.bhlangonijr.chesslib.PieceType.*;

public class ChessAI {

    private int maxDepth;
    private Map<Long, TranspositionEntry> transpositionTable;
    private Move candidateMove;
    private final Evaluator evaluator = new Evaluate();
    public boolean ponderThreadShouldRun = true;
    public BestTurnInformation ponderInfo;
    public ReentrantReadWriteLock ponderInfoLock = new ReentrantReadWriteLock();
    public Semaphore semaphore;
    private boolean isPonderThread = false;

    private long nodeCount = 0;
    private long searchDeadline = Long.MAX_VALUE; // epoch ms; alphaBeta aborts when exceeded

    // Killer moves: killerMoves[ply][0..1] — quiet moves that caused beta cutoffs at this ply
    private Move[][] killerMoves;
    // History heuristic: history[from][to] — cumulative score for quiet moves that caused cutoffs
    private int[][] historyTable;

    public long getNodeCount() { return nodeCount; }
    public void resetNodeCount() { nodeCount = 0; }

    public AlphaBeta getBestMove(int depth, BoardWrapper board) {
        transpositionTable = new HashMap<>();
        this.maxDepth = depth;
        nodeCount = 0;
        killerMoves = new Move[depth + 2][2];
        historyTable = new int[65][65]; // 64 squares + NONE (ordinal 64)
        searchDeadline = Long.MAX_VALUE; // no time limit for fixed-depth searches
        return alphaBeta(board, depth, Integer.MIN_VALUE + depth, Integer.MAX_VALUE - depth);
    }

    public BestTurnInformation getBestMove(BoardWrapper boardWrapper, double max_time_seconds, boolean isPonderThread) {
        this.isPonderThread = isPonderThread;
        System.out.println("Max time " + max_time_seconds);
        transpositionTable = new HashMap<>();
        nodeCount = 0;
        // History persists across iterative deepening iterations; killers are reset per iteration
        historyTable = new int[65][65];

        CastleRight whiteCastleRight = boardWrapper.board.getCastleRight(Side.WHITE);
        CastleRight blackCastleRight = boardWrapper.board.getCastleRight(Side.BLACK);
        System.out.println("White castle: " + whiteCastleRight);
        System.out.println("Black castle: " + blackCastleRight);

        int eval = this.evaluator.evalPos(boardWrapper);
        System.out.println("Current eval: " + eval);
        long repetitions = getLastPositionHistoryTimes(boardWrapper.board.getHistory());
        System.out.println("This position has been seen " + repetitions + (repetitions == 1 ? " time." : " times.") + "\n");
        List<Move> moves = boardWrapper.board.legalMoves();
        if (moves.size() == 1) {
            System.out.println("-------------------------------------------------------------\n");
            System.out.println("Only one move possible: " + moves.getFirst().toString() + " eval: " + eval);
            return new BestTurnInformation(new AlphaBeta(moves.getFirst(), eval, new Stack<>()), 0);
        }
        Side side = boardWrapper.board.getSideToMove();
        int value = side == Side.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        AlphaBeta bestMove = new AlphaBeta(null, value, new Stack<>());
        int startDepth = 1;
        long startTime = System.currentTimeMillis();
        this.searchDeadline = startTime + (long) (max_time_seconds * 1000);
        int previousEval = this.evaluator.evalPos(boardWrapper);
        do {
            this.maxDepth = startDepth;
            // Reset killers per iteration (they are ply-indexed to current maxDepth)
            killerMoves = new Move[startDepth + 2][2];

            int aspirationWindow = 50;
            int alpha = Math.max(Integer.MIN_VALUE + startDepth, previousEval - aspirationWindow);
            int beta = Math.min(Integer.MAX_VALUE - startDepth, previousEval + aspirationWindow);

            AlphaBeta bestMoveAtThisDepth = alphaBeta(boardWrapper.copy(), startDepth, alpha, beta);

            if (bestMoveAtThisDepth.eval <= alpha || bestMoveAtThisDepth.eval >= beta) {
                // If evaluation was outside the window, re-search normally
                bestMoveAtThisDepth = alphaBeta(boardWrapper.copy(), startDepth,
                        Integer.MIN_VALUE + startDepth, Integer.MAX_VALUE - startDepth);
            }

            bestMove = bestMoveAtThisDepth;
            previousEval = bestMove.eval;

            this.candidateMove = bestMove.move;
            double timeElapsed = ((System.currentTimeMillis() - startTime) / 1000.0);
            System.out.println("Depth: " + startDepth
                    + " Eval: " + bestMove.eval + " Move: "
                    + bestMove.move + " Time: " + timeElapsed);
            System.out.println("Line: " + bestMove.line.reversed().stream()
                    .map(Move::toString)
                    .collect(Collectors.joining(" ")) + "\n");
            if (bestMove.eval == Integer.MAX_VALUE - startDepth || bestMove.eval == Integer.MIN_VALUE + startDepth) {
                break;
            }
            if (isPonderThread) {
                this.ponderInfoLock.writeLock().lock();
                this.ponderInfo = new BestTurnInformation(bestMove, startDepth);
                this.ponderInfo.timeElapsed = timeElapsed;
                this.ponderInfoLock.writeLock().unlock();
                this.semaphore.drainPermits();
                this.semaphore.release();

                if (!this.ponderThreadShouldRun) {
                    break;
                }
            }
            startDepth++;
        } while (System.currentTimeMillis() < searchDeadline ||
                (isPonderThread && this.ponderThreadShouldRun));
        if (bestMove.move == null) {
            System.out.println("AI did not find move, picking first");
            bestMove.move = boardWrapper.board.legalMoves().getFirst();
            bestMove.eval = value * -1;
        }
        System.out.println("-------------------------------------------------------------");
        return new BestTurnInformation(bestMove, startDepth);
    }

    public long getLastPositionHistoryTimes(LinkedList<Long> history) {
        long lastItem = history.getLast();
        return history.stream().filter(value -> value.equals(lastItem)).count();
    }

    private void storeKiller(Move move, int ply) {
        if (ply < 0 || ply >= killerMoves.length) return;
        if (!move.equals(killerMoves[ply][0])) {
            killerMoves[ply][1] = killerMoves[ply][0];
            killerMoves[ply][0] = move;
        }
    }

    private void updateHistory(Move move, int depth) {
        historyTable[move.getFrom().ordinal()][move.getTo().ordinal()] += depth * depth;
    }

    private AlphaBeta alphaBeta(BoardWrapper board, int depth, int alpha, int beta) {
        nodeCount++;
        int ply = this.maxDepth - depth;

        if (!ponderThreadShouldRun && isPonderThread) {
            return new AlphaBeta(null, 0, new Stack<>());
        }

        if (System.currentTimeMillis() >= searchDeadline) {
            return new AlphaBeta(null, 0, new Stack<>());
        }

        long zobristHash = board.board.getZobristKey();
        TranspositionEntry entry = this.transpositionTable.get(zobristHash);

        if (entry != null) {
            if (entry.depth >= depth) {
                switch (entry.bound) {
                    case EXACT:
                        return new AlphaBeta(entry.bestMove, entry.eval, new Stack<>());
                    case LOWERBOUND:
                        alpha = Math.max(alpha, entry.eval);
                        break;
                    case UPPERBOUND:
                        beta = Math.min(beta, entry.eval);
                        break;
                }
                if (alpha >= beta) {
                    return new AlphaBeta(entry.bestMove, entry.eval, (Stack<Move>) entry.line.clone());
                }
            }
        }

        if (board.board.isDraw()) {
            return new AlphaBeta(null, 0, new Stack<>());
        }

        if (board.board.isMated()) {
            int currentDepth = this.maxDepth - depth;
            if (board.board.getSideToMove() == Side.BLACK) {
                return new AlphaBeta(null, Integer.MAX_VALUE - currentDepth, new Stack<>());
            } else {
                return new AlphaBeta(null, Integer.MIN_VALUE + currentDepth, new Stack<>());
            }
        }

        if (depth == 0) {
            return new AlphaBeta(null, this.evaluator.evalPos(board), new Stack<>());
        }

        List<Move> checks = new ArrayList<>();
        List<Move> captures = new ArrayList<>();
        List<Move> others = new ArrayList<>();
        List<Move> attacks = new ArrayList<>();
        initMoveLists(board, checks, captures, attacks, others);

        // Determine the primary move ordering hint.
        // Only use at root (ply == 0) where candidateMove is from a completed previous iteration.
        // At non-root nodes, TT moves from shallow/different paths can disrupt PVS by jumping
        // ahead of checks and captures, so we rely on killers and history there instead.
        Move firstMoveHint = null;
        if (ply == 0 && this.candidateMove != null) {
            firstMoveHint = this.candidateMove;
        }

        // Collect killer moves (quiet moves that caused cutoffs at this ply in sibling subtrees)
        List<Move> killerList = new ArrayList<>();
        if (ply >= 0 && ply < killerMoves.length) {
            for (Move killer : killerMoves[ply]) {
                if (killer != null && !killer.equals(firstMoveHint)
                        && (others.contains(killer) || attacks.contains(killer))) {
                    killerList.add(killer);
                }
            }
        }

        // Sort quiet moves by history score (descending) — moves that caused cutoffs before rank higher
        final int[][] hist = this.historyTable;
        others.sort((m1, m2) -> Integer.compare(
                hist[m2.getFrom().ordinal()][m2.getTo().ordinal()],
                hist[m1.getFrom().ordinal()][m1.getTo().ordinal()]));

        // Build the final ordered move list, avoiding duplicates via an exclusion set
        Set<Move> excluded = new HashSet<>();
        List<Move> orderedMoves = new ArrayList<>();

        // 1. TT / candidate move first
        if (firstMoveHint != null) {
            boolean legal = checks.contains(firstMoveHint) || captures.contains(firstMoveHint)
                    || attacks.contains(firstMoveHint) || others.contains(firstMoveHint);
            if (legal) {
                orderedMoves.add(firstMoveHint);
                excluded.add(firstMoveHint);
            }
        }
        // 2. Checks
        for (Move m : checks) {
            if (excluded.add(m)) orderedMoves.add(m);
        }
        // 3. Captures (sorted by SEE heuristic from initMoveLists)
        for (Move m : captures) {
            if (excluded.add(m)) orderedMoves.add(m);
        }
        // 4. Killer moves (quiet moves proven to be strong at this ply)
        for (Move m : killerList) {
            if (excluded.add(m)) orderedMoves.add(m);
        }
        // 5. Attacking moves (sorted by attack score from initMoveLists)
        for (Move m : attacks) {
            if (excluded.add(m)) orderedMoves.add(m);
        }
        // 6. Other quiet moves (sorted by history heuristic)
        for (Move m : others) {
            if (excluded.add(m)) orderedMoves.add(m);
        }

        int originalAlpha = alpha;
        AlphaBeta bestMove = new AlphaBeta(null, 0, null);

        //  MAX node (White to move)
        if (board.board.getSideToMove() == Side.WHITE) {
            bestMove.eval = Integer.MIN_VALUE;
            boolean firstMove = true;
            for (Move move : orderedMoves) {
                board.board.doMove(move);
                AlphaBeta ret;
                if (firstMove) {
                    firstMove = false;
                    ret = alphaBeta(board, depth - 1, alpha, beta);
                } else {
                    // Null-window search (PVS)
                    ret = alphaBeta(board, depth - 1, alpha, alpha + 1);
                    int score = ret.eval;
                    if (score > alpha && score < beta) {
                        ret = alphaBeta(board, depth - 1, alpha, beta);
                    }
                }
                board.board.undoMove();

                if (ret.eval > bestMove.eval) {
                    bestMove = new AlphaBeta(move, ret.eval, ret.line);
                }
                alpha = Math.max(alpha, bestMove.eval);
                if (bestMove.eval >= beta) {
                    // Beta cutoff — update killers and history for quiet moves
                    if (!isCapture(move, board)) {
                        storeKiller(move, ply);
                        updateHistory(move, depth);
                    }
                    break;
                }
            }
        //  MIN node (Black to move)
        } else {
            bestMove.eval = Integer.MAX_VALUE;
            boolean firstMove = true;
            for (Move move : orderedMoves) {
                board.board.doMove(move);
                AlphaBeta ret;
                if (firstMove) {
                    firstMove = false;
                    ret = alphaBeta(board, depth - 1, alpha, beta);
                } else {
                    // Null-window search (PVS)
                    ret = alphaBeta(board, depth - 1, beta - 1, beta);
                    int score = ret.eval;
                    if (score < beta && score > alpha) {
                        ret = alphaBeta(board, depth - 1, alpha, beta);
                    }
                }
                board.board.undoMove();

                if (ret.eval < bestMove.eval) {
                    bestMove = new AlphaBeta(move, ret.eval, ret.line);
                }
                beta = Math.min(beta, bestMove.eval);
                if (bestMove.eval <= alpha) {
                    // Alpha cutoff — update killers and history for quiet moves
                    if (!isCapture(move, board)) {
                        storeKiller(move, ply);
                        updateHistory(move, depth);
                    }
                    break;
                }
            }
        }

        // Determine bound type for the transposition table entry
        BoundType bound;
        if (bestMove.eval <= originalAlpha) {
            bound = BoundType.UPPERBOUND;
        } else if (bestMove.eval >= beta) {
            bound = BoundType.LOWERBOUND;
        } else {
            bound = BoundType.EXACT;
        }
        if (bestMove.move != null && bestMove.line != null) {
            bestMove.line.push(bestMove.move);
        }
        this.transpositionTable.put(zobristHash,
                new TranspositionEntry(bestMove.move, bestMove.eval, depth, bound,
                        bestMove.line != null ? bestMove.line : new Stack<>()));
        return bestMove;
    }

    private void initMoveLists(BoardWrapper board, List<Move> checks, List<Move> captures, List<Move> attacks, List<Move> others) {
        List<Move> allMoves = board.board.legalMoves();
        for (Move move : allMoves) {
            if (isCheck(move, board)) {
                checks.add(move);
            } else if (isCapture(move, board)) {
                captures.add(move);
            } else if (isAttack(move, board)) {
                attacks.add(move);
            } else {
                others.add(move);
            }
        }
        if (captures.size() > 1) {
            sortCaptures(captures, board);
        }
        if (attacks.size() > 1) {
            sortAttacks(attacks, board);
        }
    }

    public void printMoveOrder(BoardWrapper board) {
        List<Move> checks = new ArrayList<>(), captures = new ArrayList<>(),
                   attacks = new ArrayList<>(), others = new ArrayList<>();
        initMoveLists(board, checks, captures, attacks, others);

        System.out.println("--- CHECKS (" + checks.size() + ") ---");
        for (Move m : checks) System.out.printf("  %s%n", m);

        System.out.println("--- CAPTURES (" + captures.size() + ") score=toVal-fromVal[-fromVal if defended cheaper] ---");
        for (Move m : captures) System.out.printf("  %-8s  score=%d%n", m, captureScore(m, board));

        System.out.println("--- ATTACKS (" + attacks.size() + ") score=sumAttacked[-movingVal if defended cheaper] ---");
        for (Move m : attacks) System.out.printf("  %-8s  score=%d%n", m, attackScore(m, board));

        System.out.println("--- OTHERS (" + others.size() + ") ---");
        for (Move m : others) System.out.printf("  %s%n", m);
    }

    private void sortCaptures(List<Move> captures, BoardWrapper board) {
        captures.sort((move1, move2) -> Integer.compare(captureScore(move2, board), captureScore(move1, board)));
    }

    private int captureScore(Move move, BoardWrapper board) {
        int fromVal = board.getValueForSquare(move.getFrom());
        int toVal = board.getValueForSquare(move.getTo());
        int score = toVal - fromVal;
        board.board.doMove(move);
        long recapturers = board.board.squareAttackedBy(move.getTo(), board.board.getSideToMove());
        while (recapturers != 0L) {
            Square sq = Square.squareAt(Long.numberOfTrailingZeros(recapturers));
            if (board.getValueForSquare(sq) < fromVal) {
                score -= fromVal;
                break;
            }
            recapturers &= recapturers - 1;
        }
        board.board.undoMove();
        return score;
    }

    private void sortAttacks(List<Move> attacks, BoardWrapper board) {
        attacks.sort((move1, move2) -> Integer.compare(attackScore(move2, board), attackScore(move1, board)));
    }

    private int attackScore(Move move, BoardWrapper board) {
        int movingPieceValue = board.getValueForSquare(move.getFrom());
        Side enemy = board.board.getSideToMove().flip();
        board.board.doMove(move);
        long enemyPieces = board.board.getBitboard(enemy);
        int score = 0;
        long remaining = enemyPieces;
        while (remaining != 0L) {
            Square enemySquare = Square.squareAt(Long.numberOfTrailingZeros(remaining));
            if ((board.board.squareAttackedBy(enemySquare, enemy.flip()) & move.getTo().getBitboard()) != 0L) {
                score += board.getValueForSquare(enemySquare);
            }
            remaining &= remaining - 1;
        }
        long recapturers = board.board.squareAttackedBy(move.getTo(), board.board.getSideToMove());
        while (recapturers != 0L) {
            Square sq = Square.squareAt(Long.numberOfTrailingZeros(recapturers));
            if (board.getValueForSquare(sq) < movingPieceValue) {
                score -= movingPieceValue;
                break;
            }
            recapturers &= recapturers - 1;
        }
        board.board.undoMove();
        return score;
    }

    private boolean isCapture(Move move, BoardWrapper board) {
        long enemyPieces = board.board.getBitboard(board.board.getSideToMove().flip());
        return (move.getTo().getBitboard() & enemyPieces) != 0L;
    }

    private boolean isAttack(Move move, BoardWrapper board) {
        Side enemy = board.board.getSideToMove().flip();
        board.board.doMove(move);
        long enemyPieces = board.board.getBitboard(enemy);
        boolean result = false;
        long remaining = enemyPieces;
        while (remaining != 0L) {
            Square enemySquare = Square.squareAt(Long.numberOfTrailingZeros(remaining));
            if ((board.board.squareAttackedBy(enemySquare, enemy.flip()) & move.getTo().getBitboard()) != 0L) {
                result = true;
                break;
            }
            remaining &= remaining - 1;
        }
        board.board.undoMove();
        return result;
    }

    private boolean isCheck(Move move, BoardWrapper board) {
        board.board.doMove(move);
        boolean causedCheck = board.board.isKingAttacked();
        board.board.undoMove();
        return causedCheck;
    }

    public int evaluate(BoardWrapper board) {
        return this.evaluator.evalPos(board);
    }
}
