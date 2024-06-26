package com.server.chessAI;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.*;

public class ChessAI {


    public AlphaBeta getBestMove(String fen, int depth) {
        Board board = new Board();
        board.loadFromFen(fen);
        return alphaBeta(board, depth, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static class MoveComparator implements Comparator<Move> {

        private final Board board;

        public MoveComparator(Board board){
            this.board = board;
        }

        @Override
        public int compare(Move o1, Move o2) {
            if (this.board.isAttackedBy(o1) && !this.board.isAttackedBy(o2)) {
                return -1; // o1 is a capture, o2 is not
            } else if (this.board.isAttackedBy(o1) && !this.board.isAttackedBy(o2)) {
                return 1; // o2 is a capture, o1 is not
            } else {
                return 0; // Both are either captures or non-captures
            }
        }
    }

    private AlphaBeta alphaBeta(Board board, int depth, Move prevMove, int alpha, int beta) {

        List<Move> orderedMoves = board.legalMoves();
        orderedMoves.sort(new MoveComparator(board));

        if (depth == 0 || orderedMoves.size() == 0){
            return new AlphaBeta(prevMove, evaluate(board));
        }

//        MAX
        if (board.getSideToMove() == Side.WHITE){
            int value = Integer.MIN_VALUE;
            Move bestMove = prevMove;

            for (Move move : orderedMoves ){
                board.doMove(move);
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

    private int evaluate(Board board) {
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

            if (p.getPieceSide() == Side.WHITE){
                long whitePieces = bitBoard & board.getBitboard(Side.WHITE);
                int whiteCount = Long.bitCount(whitePieces);
                whiteSum += ChessTables.PIECE_VALUES[p.ordinal()] * whiteCount;
//                System.out.println("White count " + p + " " + whiteCount + " " + whiteSum);
                for (int pos = 0; pos < 64; pos++) {
                    if ((whitePieces & (1L << pos)) != 0) {
                        int row = pos / 8;
                        int col = pos % 8;
                        whiteSum += getValueFromTable(p, row, col);
                    }
                }

            }else{
                long blackPieces = bitBoard & board.getBitboard(Side.BLACK);
                int blackCount = Long.bitCount(blackPieces);
                blackSum += ChessTables.PIECE_VALUES[p.ordinal()] * blackCount;
//                System.out.println("Black count " + p + " " + blackCount + " " + blackSum);
                for (int pos = 0; pos < 64; pos++) {
                    if ((blackPieces & (1L << pos)) != 0) {
                        int row = pos / 8;
                        int col = pos % 8;
                        blackSum += getValueFromTable(p, row, col);
                    }
                }
            }
        }

//        System.out.println(whiteSum + " " + blackSum);
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
