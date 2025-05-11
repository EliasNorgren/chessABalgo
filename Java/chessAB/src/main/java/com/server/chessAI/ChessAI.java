package com.server.chessAI;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.server.chessAI.InternalEval.MyEval;
import com.server.chessAI.TranspositionEntry.BoundType;
import com.server.externalEval.cuckoochess.Evaluate;

import java.util.*;

public class ChessAI {

    private int maxDepth;
    private Map<Long, TranspositionEntry> transpositionTable;
    private Move candidateMove;
    private final Evaluator evaluator = new Evaluate();

//    public ChessAI(Evaluator evaluator) {
//        this.evaluator = evaluator;
//    }

    public AlphaBeta getBestMove(int depth, BoardWrapper board) {
        transpositionTable = new HashMap<>();
        this.maxDepth = depth;
        return alphaBeta(board, depth, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public BestTurnInformation getBestMove(double max_time_seconds, List<String> moveStack) {
        System.out.println("Max time " + max_time_seconds);
        transpositionTable = new HashMap<>();
        BoardWrapper board = new BoardWrapper();
        for (String move : moveStack) {
            board.doMove(move);
        }
        System.out.println("Pushed board into FEN: " + board.getFen());

        return this.getBestMove(board, max_time_seconds);
    }

    public BestTurnInformation getBestMove(BoardWrapper boardWrapper, double max_time_seconds) {

        transpositionTable = new HashMap<>();
        CastleRight whiteCastleRight = boardWrapper.getCastleRight(Side.WHITE);
        CastleRight blackCastleRight = boardWrapper.getCastleRight(Side.BLACK);
        System.out.println("White castle: " + whiteCastleRight);
        System.out.println("Black castle: " + blackCastleRight);

        int eval = this.evaluator.evalPos(boardWrapper);
        System.out.println("Current eval: " + eval);
        long repetitions = getLastPositionHistoryTimes(boardWrapper.getHistory());
        System.out.println("This position has been seen " + repetitions + (repetitions == 1 ? " time." : " times.") + "\n");
        List<Move> moves = boardWrapper.legalMoves();
        if (moves.size() == 1) {
            System.out.println("Only one move possible: " + moves.get(0).toString() + " eval: " + eval);
            return new BestTurnInformation(new AlphaBeta(moves.get(0), eval), 0);
        }
        Side side = boardWrapper.getSideToMove();
        int value = side == Side.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        AlphaBeta bestMove = new AlphaBeta(null, value);
        int startDepth = 1;
        long startTime = System.currentTimeMillis();
        int previousEval = this.evaluator.evalPos(boardWrapper); // or 0
        do {
            this.maxDepth = startDepth;

            int aspirationWindow = 50;
            int alpha = Math.max(Integer.MIN_VALUE, previousEval - aspirationWindow);
            int beta = Math.min(Integer.MAX_VALUE, previousEval + aspirationWindow);

            AlphaBeta bestMoveAtThisDepth = alphaBeta(boardWrapper, startDepth, null, alpha, beta);

            if (bestMoveAtThisDepth.eval <= alpha || bestMoveAtThisDepth.eval >= beta) {
                // If evaluation was outside the window, re-search normally
                bestMoveAtThisDepth = alphaBeta(boardWrapper, startDepth, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            }

            bestMove = bestMoveAtThisDepth;
            previousEval = bestMove.eval;

            this.candidateMove = bestMove.move;

            System.out.println("Depth: " + startDepth
                    + " Eval: " + bestMove.eval + " Move: "
                    + bestMove.move + " Time: " + ((System.currentTimeMillis() - startTime) / 1000.0));

            if ((side == Side.WHITE && bestMove.eval == Integer.MAX_VALUE)
                    || side == Side.BLACK && bestMove.eval == Integer.MIN_VALUE) {
                break;
            }

            startDepth++;
        } while (System.currentTimeMillis() - startTime <= (max_time_seconds * 1000));
        if (bestMove.move == null) {
            System.out.println("AI did not find non losing move, picking first");
            bestMove.move = boardWrapper.legalMoves().get(0);
            bestMove.eval = value * -1;
        }
        System.out.println("-------------------------------------------------------------");
        return new BestTurnInformation(bestMove, startDepth);
    }

    public long getLastPositionHistoryTimes(LinkedList<Long> history) {
        long lastItem = history.getLast();
        return history.stream().filter(value -> value.equals(lastItem)).count();
    }


    private AlphaBeta alphaBeta(BoardWrapper board, int depth, Move prevMove, int alpha, int beta) {
        long zobristHash = board.getZobristKey();
        TranspositionEntry entry = this.transpositionTable.get(zobristHash);

        if (entry != null && entry.depth >= depth) {
            switch (entry.bound) {
                case EXACT:
                    return new AlphaBeta(entry.bestMove, entry.eval);
                case LOWERBOUND:
                    alpha = Math.max(alpha, entry.eval);
                    break;
                case UPPERBOUND:
                    beta = Math.min(beta, entry.eval);
                    break;
            }
            if (alpha >= beta) {
                return new AlphaBeta(entry.bestMove, entry.eval);
            }
        }

        if (board.isDraw()) {
            AlphaBeta ret = new AlphaBeta(prevMove, 0);
            return ret;
        }

        if (board.isMated()) {

            if (board.getSideToMove() == Side.BLACK) {
                return new AlphaBeta(prevMove, Integer.MAX_VALUE);
            } else {
                return new AlphaBeta(prevMove, Integer.MIN_VALUE);
            }
        }

        if (depth == 0) {
            AlphaBeta ret = new AlphaBeta(prevMove, this.evaluator.evalPos(board));
            return ret;
        }

        List<Move> orderedMoves = new MoveSorter(board, candidateMove, prevMove == null).sort();
        int originalAlpha = alpha;
        Move bestMove = prevMove;
        int value;
        //        MAX
        if (board.getSideToMove() == Side.WHITE) {
            value = Integer.MIN_VALUE;
            boolean firstMove = true;
            for (Move move : orderedMoves) {
                if (!board.doMove(move, true)) {
                    System.out.println("Error");
                    System.exit(1);
                }
                AlphaBeta ret;
                if (firstMove) {
                    // First move: full window search
                    ret = alphaBeta(board, depth - 1, move, alpha, beta);
                } else {
                    // Other moves: null-window search
                    ret = alphaBeta(board, depth - 1, move, alpha, alpha + 1);
                    int score = ret.eval;
                    if (score > alpha && score < beta) {
                        // Re-search with full window if null window failed
                        ret = alphaBeta(board, depth - 1, move, alpha, beta);
                    }
                }
                firstMove = false;
                board.undoMove();
                int currentVal = ret.eval;
                if (currentVal > value) {
                    value = currentVal;
                    bestMove = move;
                }
                alpha = Math.max(alpha, value);
                if (value >= beta) {
                    break;
                }
            }
//        MIN
        } else {
            value = Integer.MAX_VALUE;
            boolean firstMove = true;
            for (Move move : orderedMoves) {
                board.doMove(move);
                AlphaBeta ret;
                if (firstMove) {
                    ret = alphaBeta(board, depth - 1, move, alpha, beta);
                } else {
                    ret = alphaBeta(board, depth - 1, move, beta - 1, beta);
                    int score = ret.eval;
                    if (score < beta && score > alpha) {
                        ret = alphaBeta(board, depth - 1, move, alpha, beta);
                    }
                }
                firstMove = false;
                board.undoMove();
                int currentVal = ret.eval;
                if (currentVal < value) {
                    value = currentVal;
                    bestMove = move;
                }
                beta = Math.min(beta, value);
                if (value <= alpha) {
                    break;
                }
            }
        }
        // Determine bound type for TT
        BoundType bound;
        if (value <= originalAlpha) {
            bound = BoundType.UPPERBOUND;
        } else if (value >= beta) {
            bound = BoundType.LOWERBOUND;
        } else {
            bound = BoundType.EXACT;
        }
        this.transpositionTable.put(zobristHash,
                new TranspositionEntry(bestMove, value, depth, bound));
        return new AlphaBeta(bestMove, value);
    }

    public int evaluate(BoardWrapper board) {
        return this.evaluator.evalPos(board);
    }
}


