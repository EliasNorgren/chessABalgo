package com.server.chessAI;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.game.GameContext;
import com.server.externalEval.cuckoochess.BitBoard;
import com.server.externalEval.cuckoochess.Evaluate;
import com.server.externalEval.cuckoochess.IPosition;

import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;


public class BoardWrapper implements IPosition {

    private final static  Piece[] PIDToPieceTable;
    private static final Map<Piece, Integer> pieceToPIDTable;

    private static final long[] ZOBRIST_WHITE_PAWNS = new long[64];
    private static final long[] ZOBRIST_BLACK_PAWNS = new long[64];

    public final Board board;

    public BoardWrapper(Board b){
        this.board = b;
    }

    public BoardWrapper(){
        this.board = new Board();
    }


    static {
        PIDToPieceTable = new Piece[13];
        pieceToPIDTable = new EnumMap<>(Piece.class);

        PIDToPieceTable[0] = Piece.NONE;
        PIDToPieceTable[1] = Piece.WHITE_KING;
        PIDToPieceTable[2] = Piece.WHITE_QUEEN;
        PIDToPieceTable[3] = Piece.WHITE_ROOK;
        PIDToPieceTable[4] = Piece.WHITE_BISHOP;
        PIDToPieceTable[5] = Piece.WHITE_KNIGHT;
        PIDToPieceTable[6] = Piece.WHITE_PAWN;

        PIDToPieceTable[7] = Piece.BLACK_KING;
        PIDToPieceTable[8] = Piece.BLACK_QUEEN;
        PIDToPieceTable[9] = Piece.BLACK_ROOK;
        PIDToPieceTable[10] = Piece.BLACK_BISHOP;
        PIDToPieceTable[11] = Piece.BLACK_KNIGHT;
        PIDToPieceTable[12] = Piece.BLACK_PAWN;

        for (int i = 0; i < PIDToPieceTable.length; i++) {
            pieceToPIDTable.put(PIDToPieceTable[i], i);
        }

        Random rng = new SecureRandom(); // or new Random(seed) for reproducibility
        for (int i = 0; i < 64; i++) {
            ZOBRIST_WHITE_PAWNS[i] = rng.nextLong();
            ZOBRIST_BLACK_PAWNS[i] = rng.nextLong();
        }

    }

    @Override
    public long getPieceTypeBB(int pid) {
        if (isWhite(pid)) {
            return board.getBitboard(Side.WHITE)
                    & board.getBitboard(PIDToPieceTable[pid]);
        }else {
            return board.getBitboard(Side.BLACK)
                    & board.getBitboard(PIDToPieceTable[pid]);
        }
    }

    @Override
    public long getWhiteBB() {
        return board.getBitboard(Side.WHITE);
    }

    @Override
    public long getBlackBB() {
        return board.getBitboard(Side.BLACK);
    }

    @Override
    public int getPIDBySquare(int squareID) {
        return pieceToPIDTable.get(board.getPiece(Square.squareAt(squareID)));
    }

    @Override
    public int getWKingSQ() {
        return board.getKingSquare(Side.WHITE).ordinal();
    }

    @Override
    public int getBKingSQ() {
        return board.getKingSquare(Side.BLACK).ordinal();
    }

    @Override
    public int getPST1(int pid) {
        Piece p = PIDToPieceTable[pid];
        int score = 0;
        int[] pst1 = Evaluate.psTab1[pid];
        for (Square sq : board.getPieceLocation(p)) {
            score += pst1[sq.ordinal()];
        }
        return score;
    }

    @Override
    public int getPST2(int pid) {
        Piece p = PIDToPieceTable[pid];
        int score = 0;
        int[] pst2 = Evaluate.psTab2[pid];
        for (Square sq : board.getPieceLocation(p)) {
            score += pst2[sq.ordinal()];
        }
        return score;
    }


    @Override
    public long pawnZobristHash() {
        long hash = 0L;

        long wp = board.getBitboard(Piece.WHITE_PAWN);
        long bp = board.getBitboard(Piece.BLACK_PAWN);

        // White pawns
        while (wp != 0) {
            int sq = Long.numberOfTrailingZeros(wp);
            hash ^= ZOBRIST_WHITE_PAWNS[sq];
            wp &= wp - 1; // Clear lowest bit
        }

        // Black pawns
        while (bp != 0) {
            int sq = Long.numberOfTrailingZeros(bp);
            hash ^= ZOBRIST_BLACK_PAWNS[sq];
            bp &= bp - 1;
        }

        return hash;
    }

    @Override
    public int getKingSq(boolean whiteMove) {
        return whiteMove ? this.getWKingSQ() : this.getBKingSQ();
    }

    @Override
    public int getwMtrl() {
        return getAllMtrlForSide(Side.WHITE);
    }

    @Override
    public int getbMtrl() {
        return getAllMtrlForSide(Side.BLACK);
    }

    @Override
    public int getwMtrlPawns() {
        return getSumForPieceAndSide(Side.WHITE, Piece.WHITE_PAWN);
    }

    @Override
    public int getbMtrlPawns() {
        return getSumForPieceAndSide(Side.BLACK, Piece.BLACK_PAWN);
    }

    @Override
    public boolean isWhiteMove() {
        return board.getSideToMove().equals(Side.WHITE);
    }

    @Override
    public boolean a1Castle() {
        CastleRight castleRight = board.getCastleRight(Side.WHITE);
        return castleRight.equals(CastleRight.KING_AND_QUEEN_SIDE) || castleRight.equals(CastleRight.QUEEN_SIDE);
    }

    @Override
    public boolean h1Castle() {
        CastleRight castleRight = board.getCastleRight(Side.WHITE);
        return castleRight.equals(CastleRight.KING_AND_QUEEN_SIDE) || castleRight.equals(CastleRight.KING_SIDE);
    }

    @Override
    public boolean a8Castle() {
        CastleRight castleRight = board.getCastleRight(Side.BLACK);
        return castleRight.equals(CastleRight.KING_AND_QUEEN_SIDE) || castleRight.equals(CastleRight.QUEEN_SIDE);
    }

    @Override
    public boolean h8Castle() {
        CastleRight castleRight = board.getCastleRight(Side.BLACK);
        return castleRight.equals(CastleRight.KING_AND_QUEEN_SIDE) || castleRight.equals(CastleRight.KING_SIDE);
    }

    @Override
    public int getY(int sq) {
        return BitBoard.getY(sq);
    }

    @Override
    public int getX(int sq) {
        return BitBoard.getX(sq);
    }

    private static boolean isWhite(int pType) {
        return pType < 7;
    }

    private int getAllMtrlForSide(Side s){
        int sum = 0;
        for (Piece p : Piece.values()){
            sum += getSumForPieceAndSide(s, p);
        }
        return sum;
    }

    private int getSumForPieceAndSide(Side s, Piece p) {
        return Long.bitCount(board.getBitboard(s) & board.getBitboard(p))
                * Evaluate.pieceValue[pieceToPIDTable.get(p)];
    }

    @Override
    public String toString() {
        return board.getFen();
    }

    public BoardWrapper copy() {
        return new BoardWrapper(this.board.clone());
    }

}
