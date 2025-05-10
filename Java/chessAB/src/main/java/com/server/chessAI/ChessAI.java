package com.server.chessAI;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.server.chessAI.TranspositionEntry.BoundType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChessAI {

    private int maxDepth;
    private Map<Long, TranspositionEntry> transpositionTable;
    private Move candidateMove;

    public AlphaBeta getBestMove(int depth, Board board) {
        transpositionTable = new HashMap<>();
        this.maxDepth = depth;
        return alphaBeta(board, depth, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public BestTurnInformation getBestMove(double max_time_seconds, List<String> moveStack) {
        System.out.println("Max time " + max_time_seconds);
        transpositionTable = new HashMap<>();
        Board board = new Board();
        for (String move : moveStack) {
            board.doMove(move);
        }
        System.out.println("Pushed board into FEN: " + board.getFen());

        return this.getBestMove(board, max_time_seconds);
    }

    public BestTurnInformation getBestMove(Board board, double max_time_seconds) {
        transpositionTable = new HashMap<>();
        CastleRight whiteCastleRight = board.getCastleRight(Side.WHITE);
        CastleRight blackCastleRight = board.getCastleRight(Side.BLACK);
        System.out.println("White castle: " + whiteCastleRight);
        System.out.println("Black castle: " + blackCastleRight);

        int eval = evaluate(board);
        System.out.println("Current eval: " + eval);
        long repetitions = getLastPositionHistoryTimes(board.getHistory());
        System.out.println("This position has been seen " + repetitions + (repetitions == 1 ? " time." : " times.") + "\n");
        List<Move> moves = board.legalMoves();
        if (moves.size() == 1) {
            System.out.println("Only one move possible: " + moves.get(0).toString() + " eval: " + eval);
            return new BestTurnInformation(new AlphaBeta(moves.get(0), eval), 0);
        }
        Side side = board.getSideToMove();
        int value = side == Side.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        AlphaBeta bestMove = new AlphaBeta(null, value);
        int startDepth = 1;
        long startTime = System.currentTimeMillis();
        int previousEval = evaluate(board); // or 0
        do {
            this.maxDepth = startDepth;

            int aspirationWindow = 50;
            int alpha = Math.max(Integer.MIN_VALUE, previousEval - aspirationWindow);
            int beta = Math.min(Integer.MAX_VALUE, previousEval + aspirationWindow);

            AlphaBeta bestMoveAtThisDepth = alphaBeta(board, startDepth, null, alpha, beta);

            if (bestMoveAtThisDepth.eval <= alpha || bestMoveAtThisDepth.eval >= beta) {
                // If evaluation was outside the window, re-search normally
                bestMoveAtThisDepth = alphaBeta(board, startDepth, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
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
            bestMove.move = board.legalMoves().get(0);
            bestMove.eval = value * -1;
        }
        System.out.println("-------------------------------------------------------------");
        return new BestTurnInformation(bestMove, startDepth);
    }

    public long getLastPositionHistoryTimes(LinkedList<Long> history) {
        long lastItem = history.getLast();
        return history.stream().filter(value -> value.equals(lastItem)).count();
    }


    private AlphaBeta alphaBeta(Board board, int depth, Move prevMove, int alpha, int beta) {
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

        if (depth == 0 || board.isDraw() || board.isMated()) {
            AlphaBeta ret = new AlphaBeta(prevMove, evaluate(board));
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

    public int evaluate(Board board) {

        if (board.isMated()) {
            if (board.getSideToMove() == Side.BLACK) {
                return Integer.MAX_VALUE;
            } else {
                return Integer.MIN_VALUE;
            }
        }

        if (board.isDraw()) {
            return 0;
        }

        int whiteSum = 0;
        int blackSum = 0;

//        whiteSum += calculateCastlePoints(board.getCastleRight(Side.WHITE));
//        blackSum += calculateCastlePoints(board.getCastleRight(Side.BLACK));

        for (Piece p : Piece.values()) {
            if (p == Piece.NONE) {
                continue;
            }

            long bitBoard = board.getBitboard(p);

            if (bitBoard == 0L) {
                continue;
            }

            if (p.getPieceSide() == Side.WHITE) {
                long whitePieces = bitBoard & board.getBitboard(Side.WHITE);
                int whiteCount = Long.bitCount(whitePieces);
                whiteSum += ChessTables.PIECE_VALUES[p.ordinal()] * whiteCount;
                List<Square> squares = board.getPieceLocation(p);
//                for (Square sq : squares) {
//                    int ordinal = sq.ordinal();
//                    int row = 7 - ordinal / 8;
//                    int col = ordinal % 8;
//                    whiteSum += getValueFromTable(p, row, col);
//                }
            } else {
                long blackPieces = bitBoard & board.getBitboard(Side.BLACK);
                int blackCount = Long.bitCount(blackPieces);
                blackSum += ChessTables.PIECE_VALUES[p.ordinal()] * blackCount;
                List<Square> squares = board.getPieceLocation(p);
//                for (Square sq : squares) {
//                    int ordinal = sq.ordinal();
//                    int row = 7 - ordinal / 8;
//                    int col = ordinal % 8;
//                    blackSum += getValueFromTable(p, row, col);
//                }
            }
        }
        return whiteSum - blackSum;
    }

    private int calculateCastlePoints(CastleRight castleRight) {
        switch (castleRight) {
            case NONE -> {
                return -50;
            }
            case KING_SIDE, QUEEN_SIDE -> {
                return -25;
            }
            case KING_AND_QUEEN_SIDE -> {
                return 0;
            }
        }
        return 0;
    }

    private int getValueFromTable(Piece p, int row, int col) {
        return switch (p) {
            case WHITE_PAWN -> ChessTables.PAWN_TABLE_WHITE[row][col];
            case WHITE_KNIGHT -> ChessTables.KNIGHT_TABLE_WHITE[row][col];
            case WHITE_BISHOP -> ChessTables.BISHOP_TABLE_WHITE[row][col];
            case WHITE_ROOK -> ChessTables.ROOK_TABLE_WHITE[row][col];
            case WHITE_QUEEN -> ChessTables.QUEEN_TABLE_WHITE[row][col];
            case WHITE_KING -> ChessTables.KING_TABLE_WHITE[row][col];
            case BLACK_PAWN -> ChessTables.PAWN_TABLE_BLACK[row][col];
            case BLACK_KNIGHT -> ChessTables.KNIGHT_TABLE_BLACK[row][col];
            case BLACK_BISHOP -> ChessTables.BISHOP_TABLE_BLACK[row][col];
            case BLACK_ROOK -> ChessTables.ROOK_TABLE_BLACK[row][col];
            case BLACK_QUEEN -> ChessTables.QUEEN_TABLE_BLACK[row][col];
            case BLACK_KING -> ChessTables.KING_TABLE_BLACK[row][col];
            default -> 0;
        };
    }
}


