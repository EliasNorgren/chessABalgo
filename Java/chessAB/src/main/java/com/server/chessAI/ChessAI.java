package com.server.chessAI;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.server.chessAI.TranspositionEntry.BoundType;
import com.server.externalEval.cuckoochess.Evaluate;
import org.springframework.web.server.adapter.AbstractReactiveWebInitializer;

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

    public long getNodeCount() { return nodeCount; }
    public void resetNodeCount() { nodeCount = 0; }

    public AlphaBeta getBestMove(int depth, BoardWrapper board) {
        transpositionTable = new HashMap<>();
        this.maxDepth = depth;
        nodeCount = 0;
        return alphaBeta(board, depth, Integer.MIN_VALUE + depth, Integer.MAX_VALUE - depth);
    }

    public BestTurnInformation getBestMove(BoardWrapper boardWrapper, double max_time_seconds, boolean isPonderThread) {
        this.isPonderThread = isPonderThread;
        System.out.println("Max time " + max_time_seconds);
        transpositionTable = new HashMap<>();
        nodeCount = 0;
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
        AlphaBeta bestMove;
        int startDepth = 1;
        long startTime = System.currentTimeMillis();
        int previousEval = this.evaluator.evalPos(boardWrapper); // or 0
        do {
            this.maxDepth = startDepth;

            int aspirationWindow = 50;
            int alpha = Math.max(Integer.MIN_VALUE + startDepth, previousEval - aspirationWindow);
            int beta = Math.min(Integer.MAX_VALUE - startDepth, previousEval + aspirationWindow);

            AlphaBeta bestMoveAtThisDepth = alphaBeta(boardWrapper.copy(), startDepth,  alpha, beta);

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
            if (isPonderThread){
                this.ponderInfoLock.writeLock().lock();
                this.ponderInfo = new BestTurnInformation(bestMove, startDepth);
                this.ponderInfo.timeElapsed = timeElapsed;
                this.ponderInfoLock.writeLock().unlock();
                this.semaphore.drainPermits();
                this.semaphore.release();

                if (!this.ponderThreadShouldRun){
                    break;
                }
            }
            startDepth++;
        } while (System.currentTimeMillis() - startTime <= (max_time_seconds * 1000) ||
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


    private AlphaBeta alphaBeta(BoardWrapper board, int depth, int alpha, int beta) {
        nodeCount++;

        if (!ponderThreadShouldRun && isPonderThread) {
            return new AlphaBeta(null, 0, new Stack<>());
        }

        long zobristHash = board.board.getZobristKey();
        TranspositionEntry entry = this.transpositionTable.get(zobristHash);

        if (entry != null && entry.depth >= depth) {
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
        if (this.maxDepth == depth && this.candidateMove != null) {
            if (checks.contains(this.candidateMove)) {
                checks.remove(this.candidateMove);
            } else if (captures.contains(this.candidateMove)) {
                captures.remove(this.candidateMove);
            } else if (others.contains(this.candidateMove)) {
                others.remove(this.candidateMove);
            }else if(captures.contains(this.candidateMove)){
                captures.remove(this.candidateMove);
            }
            checks.addFirst(this.candidateMove);
        }

        int originalAlpha = alpha;
        // TODO: Set value to alpha or beta??
        AlphaBeta bestMove = new AlphaBeta(null, 0, null);

        List<Move> orderedMoves = new ArrayList<>(checks);
        orderedMoves.addAll(captures);
        orderedMoves.addAll(attacks);
        orderedMoves.addAll(others);

        //        MAX
        if (board.board.getSideToMove() == Side.WHITE) {
            bestMove.eval = Integer.MIN_VALUE;
            boolean firstMove = true;
            for (Move move : orderedMoves) {
                board.board.doMove(move);
                AlphaBeta ret;
                if (firstMove) {
                    // First move: full window search
                    firstMove = false;
                    ret = alphaBeta(board, depth - 1, alpha, beta);
                } else {
                    // Other moves: null-window search
                    ret = alphaBeta(board, depth - 1, alpha, alpha + 1);
                    int score = ret.eval;
                    if (score > alpha && score < beta) {
                        // Re-search with full window if null window failed
                        ret = alphaBeta(board, depth - 1, alpha, beta);
                    }
                }
                board.board.undoMove();

                if (ret.eval > bestMove.eval) {
                    bestMove = new AlphaBeta(move, ret.eval, ret.line);
                }
                alpha = Math.max(alpha, bestMove.eval);
                if (bestMove.eval >= beta) {
                    break;
                }
            }
//        MIN
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
                    ret = alphaBeta(board, depth - 1,beta - 1, beta);
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
                    break;
                }
            }
        }
        // Determine bound type for TT
        BoundType bound;
        if (bestMove.eval <= originalAlpha) {
            bound = BoundType.UPPERBOUND;
        } else if (bestMove.eval >= beta) {
            bound = BoundType.LOWERBOUND;
        } else {
            bound = BoundType.EXACT;
        }
        bestMove.line.push(bestMove.move);
        this.transpositionTable.put(zobristHash,
                new TranspositionEntry(bestMove.move, bestMove.eval, depth, bound, bestMove.line));
        return bestMove;
    }

    private void initMoveLists(BoardWrapper board, List<Move> checks, List<Move> captures, List<Move> attacks, List<Move> others) {
        List<Move> allMoves = board.board.legalMoves();
        for (Move move : allMoves) {
            if (isCheck(move, board)) {
                checks.add(move);
            } else if (isCapture(move, board)){
                captures.add(move);
            } else if (isAttack(move, board)){
                attacks.add(move);
            }
            else{
                others.add(move);
            }
        }
        sortCaptures(captures, board);
    }

    private void sortCaptures(List<Move> captures, BoardWrapper board) {
        captures.sort((move1, move2) -> {
            int move1FromVal = board.getValueForSquare(move1.getFrom());
            int move1ToVal = board.getValueForSquare(move1.getTo());

            int move2FromVal = board.getValueForSquare(move2.getFrom());
            int move2ToVal = board.getValueForSquare(move2.getTo());

            int move1value = move1ToVal - move1FromVal;
            int move2value = move2ToVal - move2FromVal;
            if (move1value > move2value) {
                return -1;
            }
            return 1;
        });
    }

    private boolean isCapture(Move move, BoardWrapper board) {
        long enemyPieces = board.board.getBitboard(board.board.getSideToMove().flip());
        return (move.getTo().getBitboard() & enemyPieces) != 0L;
    }

    private boolean isAttack(Move move, BoardWrapper board) {
        Side enemy = board.board.getSideToMove().flip(); // save before doMove flips it
        board.board.doMove(move);
        long enemyPieces = board.board.getBitboard(enemy);
        boolean result = false;
        long remaining = enemyPieces;
        while (remaining != 0L) {
            Square enemySquare = Square.squareAt(Long.numberOfTrailingZeros(remaining));
            // squareAttackedBy(sq, side) returns squares FROM WHICH side's pieces attack sq
            // If move.getTo() is in that set, our moved piece attacks this enemy square
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


