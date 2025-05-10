package com.server.chessAI;

import com.github.bhlangonijr.chesslib.Board;
import org.junit.jupiter.api.Test;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class ChessAITest {

    @Test
    public void checkMateTest(){
        ChessAI ai = new ChessAI();
        Board b = new Board();
        b.loadFromFen("3k4/6R1/6nR/8/8/8/8/7K w");

        AlphaBeta move1 = ai.getBestMove(5, b);
        assertEquals("h6g6", move1.move.toString());
        assertEquals(Integer.MAX_VALUE, move1.eval);
        b.doMove(move1.move);
        b.doMove("d8e8");

        AlphaBeta move2 = ai.getBestMove(4, b);
        assertEquals("g6f6", move2.move.toString());
        assertEquals(Integer.MAX_VALUE, move2.eval);
        b.doMove(move2.move);
        b.doMove("e8d8");

        AlphaBeta move3 = ai.getBestMove(1, b);
        assertEquals("f6f8", move3.move.toString());
        assertEquals(Integer.MAX_VALUE, move1.eval);
        b.doMove(move3.move);
        assertTrue(b.isMated());
    }

    @Test
    public void mateInFiveTest(){

        ChessAI ai = new ChessAI();
        Board b = new Board();
        b.loadFromFen("8/8/8/2k5/7R/6R1/8/7K w - - 0 1");
        String fen = b.getFen();

        AlphaBeta move = ai.getBestMove(9, b);
        assertEquals("g3g5", move.move.toString());
        assertEquals(Integer.MAX_VALUE, move.eval);

    }

    @Test
    public void mateInOneTest(){
        Board b = new Board();
        b.loadFromFen("3R2n1/k1r2p2/pp3n2/4Q3/2b5/4BBNP/PP3PP1/4K2R w K - 0 29");
        ChessAI ai = new ChessAI();
        BestTurnInformation ret = ai.getBestMove( b, 3);
        System.out.println(ret.bestMove.move);

    }

    @Test
    public void drawByRepetitionTest(){
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 Nc3 Nb4 d4 Nc6 g3 Rb8 Rb1 Ra8 Be2 Ng4 Kd2 Nxf2 Qf1 Nxh1 Nh4 Nxg3 hxg3 Nxd4 Nf5 Nxe2 Kxe2 c6 Nxg7+ Bxg7 Qf4 Bxc3 bxc3 Qa5 Kf1 Qxa2 Rb2 Qc4+ Kg2 Qxc3 Qf5 d6 Qf2 Rg8 Qf3 Bh3+ Kxh3 Qxf3 Kh2 Qxg3+ Kh1 Qe1+ Kh2 Qg1+ Kh3 Rg3+ Kh4 Rg4+ Kh5 Rxe4 Bf4 Qh1+ Bh2 Qxh2+ Kg5 f6+ Kf5 e4f4 Ke6 f4e4 Kf5 e4f4 Ke6";
        Board b = generateBoardFromMoves(moveStackString);
        String fen = b.getFen();
        AlphaBeta ret = ai.getBestMove(2, b);
        assertNotEquals("f4ef", ret.move.getSan());
    }

    @Test
    public void blackKingSideCastleTest(){
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 e5 Nd5 d4 e6 Bc4 Nb6 Bb5 Be7 Bxc6 dxc6 c3 Qd5 O-O Qe4 Nbd2 Qg4 h3 Qf5 Qe2 Qh5 Ne4 Bd7 a3";
        Board b = generateBoardFromMoves(moveStackString);
        String fen = b.getFen();
        AlphaBeta ret = ai.getBestMove(5, b);
        System.out.println(ret.move);
        assertEquals("e8g8", ret.move.toString());
    }

    @Test
    public void blackKingQueenSideCastleTest(){
        ChessAI ai = new ChessAI();
        Board b = new Board();
        b.loadFromFen("r3kb1r/1pp2pp1/p2q1n1p/4p3/2BN2b1/1PP2PP1/P1Q5/R3R1K1 b kq - 0 22");
        AlphaBeta ret = ai.getBestMove(6, b);
        System.out.println(ret.move);
    }

    @Test
    public void foundMateInstantTest() {
        ChessAI ai = new ChessAI();
        Board b = new Board();
        b.loadFromFen("K7/5r2/1k6/8/8/8/8/8 b - - 0 1");
        BestTurnInformation ret = ai.getBestMove(b, 5);
        assertTrue(ret.depth == 1);
    }

    @Test
    public void repetitionCounterTest(){
        ChessAI ai = new ChessAI();
        LinkedList<Long> list = new LinkedList<>();
        list.add(10L);
        list.add(20L);
        list.add(10L);
        list.add(30L);
        list.add(10L);
        long actual = ai.getLastPositionHistoryTimes(list);
        assertEquals(3, actual);
    }

    @Test
    public void saveBishopTest(){

        ChessAI ai = new ChessAI();
        Board b = new Board();
        b.loadFromFen("rnbqkbnr/1p3pp1/2ppp2p/p7/B2PP3/2N5/PPP2PPP/R1BQK1NR w");
        String fen = b.getFen();

        AlphaBeta move = ai.getBestMove(5, b);
        assertEquals("a4b3", move.move.toString());

    }

    private Board generateBoardFromMoves(String moves){
        List<String> moveStack = Arrays.stream(moves.split(" ")).toList();
        Board b = new Board();
        for (String s : moveStack) {
            b.doMove(s);
        }
        return b;
    }
}