package com.server.chessAI.InternalEval;

import com.github.bhlangonijr.chesslib.*;
import com.server.chessAI.BoardWrapper;
import com.server.chessAI.ChessTables;
import com.server.chessAI.Evaluator;

import java.util.List;

public class MyEval implements Evaluator {
    @Override
    public int evalPos(BoardWrapper board) {

        if (board.board.isMated()) {
            if (board.board.getSideToMove() == Side.BLACK) {
                return Integer.MAX_VALUE;
            } else {
                return Integer.MIN_VALUE;
            }
        }

        if (board.board.isDraw()) {
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

            long bitBoard = board.board.getBitboard(p);

            if (bitBoard == 0L) {
                continue;
            }

            if (p.getPieceSide() == Side.WHITE) {
                long whitePieces = bitBoard & board.board.getBitboard(Side.WHITE);
                int whiteCount = Long.bitCount(whitePieces);
                whiteSum += ChessTables.PIECE_VALUES[p.ordinal()] * whiteCount;
                List<Square> squares = board.board.getPieceLocation(p);
                for (Square sq : squares) {
                    int ordinal = sq.ordinal();
                    int row = 7 - ordinal / 8;
                    int col = ordinal % 8;
                    whiteSum += getValueFromTable(p, row, col);
                }
            } else {
                long blackPieces = bitBoard & board.board.getBitboard(Side.BLACK);
                int blackCount = Long.bitCount(blackPieces);
                blackSum += ChessTables.PIECE_VALUES[p.ordinal()] * blackCount;
                List<Square> squares = board.board.getPieceLocation(p);
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
