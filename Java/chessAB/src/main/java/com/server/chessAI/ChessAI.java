package com.server.chessAI;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.*;

public class ChessAI {

    public AlphaBeta getBestMove(int depth, Board board) {
        return alphaBeta(board, depth, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public BestTurnInformation getBestMove(int max_time_seconds, List<String> moveStack) {
        Board board = new Board();
        for (String move : moveStack) {
            board.doMove(move);
        }
        System.out.println("Pushed board into FEN: " + board.getFen());

        return this.getBestMove(board, max_time_seconds);
    }

    private BestTurnInformation getBestMove(Board board, int depth) {

        CastleRight whiteCastleRight = board.getCastleRight(Side.WHITE);
        CastleRight blackCastleRight = board.getCastleRight(Side.BLACK);
        System.out.println("White castle: " + whiteCastleRight);
        System.out.println("Black castle: " + blackCastleRight + "\n");

        int value = board.getSideToMove() == Side.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        AlphaBeta bestMove = new AlphaBeta(null, value);
        int startDepth = 1;
        long startTime = System.currentTimeMillis();
        do {
            bestMove = alphaBeta(board, startDepth, null, Integer.MIN_VALUE, Integer.MAX_VALUE);

            System.out.println("Depth: " + startDepth + " Eval: " + bestMove.eval + " Move: " + bestMove.prevMove + " Time: " + ((System.currentTimeMillis() - startTime) / 1000.0));
            startDepth++;
        } while (System.currentTimeMillis() - startTime <= (depth * 1000L));
        if (bestMove.prevMove == null){
            System.out.println("AI did not find non losing move, picking first");
            bestMove.prevMove = board.legalMoves().get(0);
            bestMove.eval = value * -1;
        }
        System.out.println("-------------------------------------------------------------");
        return new BestTurnInformation(bestMove, startDepth);
    }



    private AlphaBeta alphaBeta(Board board, int depth, Move prevMove, int alpha, int beta) {
        if (depth == 0 || board.isDraw() || board.isMated()){
            return new AlphaBeta(prevMove, evaluate(board));
        }

        List<Move> orderedMoves = new MoveSorter(board).sort();

//        MAX
        if (board.getSideToMove() == Side.WHITE){
            int value = Integer.MIN_VALUE;
            Move bestMove = prevMove;

            for (Move move : orderedMoves ){
                if(!board.doMove(move, true)){
                    System.out.println("Error");
                    System.exit(1);
                }
                AlphaBeta ret = alphaBeta(board, depth - 1, prevMove, alpha, beta);
                board.undoMove();
                int currentVal = ret.eval;
                if (currentVal > value) {
                    value = currentVal;
                    bestMove = move;
                }
                alpha = Math.max(alpha, value);
                if (value >= beta){
                    break;
                }
            }
            return new AlphaBeta(bestMove, value);
//        MIN
        } else {
            int value = Integer.MAX_VALUE;
            Move bestMove = prevMove;
            for (Move move : orderedMoves ){
                board.doMove(move);
                AlphaBeta ret = alphaBeta(board, depth - 1, prevMove, alpha, beta);
                board.undoMove();
                int currentVal = ret.eval;
                if (currentVal < value){
                    value = currentVal;
                    bestMove = move;
                }
                beta = Math.min(beta, value);
                if (value <= alpha){
                    break;
                }
            }
            return new AlphaBeta(bestMove, value);
        }
    }

    public int evaluate(Board board) {

        if (board.isMated()){
            if (board.getSideToMove() == Side.BLACK){
                return Integer.MAX_VALUE;
            }else {
                return Integer.MIN_VALUE;
            }
        }

        if (board.isDraw()){
            return 0;
        }

        int whiteSum = 0;
        int blackSum = 0;

        for (Piece p : Piece.values()){
            if (p == Piece.NONE){
                continue;
            }

            long bitBoard = board.getBitboard(p);

            if (bitBoard == 0L){
                continue;
            }

            if (p.getPieceSide() == Side.WHITE){
                long whitePieces = bitBoard & board.getBitboard(Side.WHITE);
                int whiteCount = Long.bitCount(whitePieces);
                whiteSum += ChessTables.PIECE_VALUES[p.ordinal()] * whiteCount;
                List<Square> squares = board.getPieceLocation(p);
                for (Square sq : squares) {
                    int ordinal = sq.ordinal();
                    int row = 7 - ordinal / 8;
                    int col = ordinal % 8;
                    whiteSum += getValueFromTable(p, row, col);
                }
            }else{
                long blackPieces = bitBoard & board.getBitboard(Side.BLACK);
                int blackCount = Long.bitCount(blackPieces);
                blackSum += ChessTables.PIECE_VALUES[p.ordinal()] * blackCount;
                List<Square> squares = board.getPieceLocation(p);
                for (Square sq : squares) {
                    int ordinal = sq.ordinal();
                    int row = 7 - ordinal / 8;
                    int col = ordinal % 8;
                    blackSum += getValueFromTable(p, row, col);
                }
            }
        }
        return whiteSum - blackSum;
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
