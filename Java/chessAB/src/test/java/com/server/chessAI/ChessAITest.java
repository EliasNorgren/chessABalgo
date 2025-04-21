package com.server.chessAI;

import com.github.bhlangonijr.chesslib.Board;
import org.junit.jupiter.api.Test;


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class ChessAITest {
    @Test
    public void checkMateTest(){
        ChessAI ai = new ChessAI();
        Board b = new Board();
        b.loadFromFen("3k4/6R1/6nR/8/8/8/8/7K w");

        AlphaBeta move1 = ai.getBestMove(5, b);
        assertEquals("h6g6", move1.prevMove.toString());
        assertEquals(Integer.MAX_VALUE, move1.eval);
        b.doMove(move1.prevMove);
        b.doMove("d8e8");

        AlphaBeta move2 = ai.getBestMove(4, b);
        assertEquals("g6f6", move2.prevMove.toString());
        assertEquals(Integer.MAX_VALUE, move2.eval);
        b.doMove(move2.prevMove);
        b.doMove("e8d8");

        AlphaBeta move3 = ai.getBestMove(1, b);
        assertEquals("f6f8", move3.prevMove.toString());
        assertEquals(Integer.MAX_VALUE, move1.eval);
        b.doMove(move3.prevMove);
        assertTrue(b.isMated());
    }

    @Test
    public void drawByRepetitionTest(){
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 Nc3 Nb4 d4 Nc6 g3 Rb8 Rb1 Ra8 Be2 Ng4 Kd2 Nxf2 Qf1 Nxh1 Nh4 Nxg3 hxg3 Nxd4 Nf5 Nxe2 Kxe2 c6 Nxg7+ Bxg7 Qf4 Bxc3 bxc3 Qa5 Kf1 Qxa2 Rb2 Qc4+ Kg2 Qxc3 Qf5 d6 Qf2 Rg8 Qf3 Bh3+ Kxh3 Qxf3 Kh2 Qxg3+ Kh1 Qe1+ Kh2 Qg1+ Kh3 Rg3+ Kh4 Rg4+ Kh5 Rxe4 Bf4 Qh1+ Bh2 Qxh2+ Kg5 f6+ Kf5 e4f4 Ke6 f4e4 Kf5 e4f4 Ke6";
        Board b = generateBoardFromMoves(moveStackString);

        AlphaBeta ret = ai.getBestMove(2, b);
        assertNotEquals("f4ef", ret.prevMove.getSan());
    }

    @Test
    public void castleTest(){
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 e5 Nd5 d4 e6 Bc4 Nb6 Bb5 Be7 Bxc6 dxc6 c3 Qd5 O-O Qe4 Nbd2 Qg4 h3 Qf5 Qe2 Qh5 Ne4 Bd7 a3";
        Board b = generateBoardFromMoves(moveStackString);
        String fen = b.getFen();
        AlphaBeta ret = ai.getBestMove(5, b);
        System.out.println(ret.prevMove);
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