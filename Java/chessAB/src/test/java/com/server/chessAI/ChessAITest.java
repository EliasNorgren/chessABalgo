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
        AlphaBeta move1 = ai.getBestMove(5, "1n3k1r/3p1B2/p5pp/4Q1N1/8/8/PPPP1PPP/R1B1K2R w");
        assertEquals(move1.prevMove.getSan(), "Qf6");
        assertEquals(move1.eval, Integer.MAX_VALUE);

    }

    @Test
    public void drawByRepetitionTest(){
        ChessAI ai = new ChessAI();
        String moveStackString = "e4-Nc6-Nf3-Nf6-Nc3-Nb4-d4-Nc6-g3-Rb8-Rb1-Ra8-Be2-Ng4-Kd2-Nxf2-Qf1-Nxh1-Nh4-Nxg3-hxg3-Nxd4-Nf5-Nxe2-Kxe2-c6-Nxg7+-Bxg7-Qf4-Bxc3-bxc3-Qa5-Kf1-Qxa2-Rb2-Qc4+-Kg2-Qxc3-Qf5-d6-Qf2-Rg8-Qf3-Bh3+-Kxh3-Qxf3-Kh2-Qxg3+-Kh1-Qe1+-Kh2-Qg1+-Kh3-Rg3+-Kh4-Rg4+-Kh5-Rxe4-Bf4-Qh1+-Bh2-Qxh2+-Kg5-f6+-Kf5-e4f4-Ke6-f4e4-Kf5-e4f4-Ke6";
        String[] moveStack = moveStackString.split("-");
        Board b = new Board();

        AlphaBeta ret = ai.getBestMove(2, Arrays.stream(moveStack).toList(), true);
        assertNotEquals("f4ef", ret.prevMove.getSan());
    }

    @Test
    public void castleTest(){
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 e5 Nd5 d4 e6 Bc4 Nb6 Bb5 Be7 Bxc6 dxc6 c3 Qd5 O-O Qe4 Nbd2 Qg4 h3 Qf5 Qe2 Qh5 Ne4 Bd7 a3";
        List<String> moveStack = Arrays.stream(moveStackString.split(" ")).toList();
        Board b = new Board();
        AlphaBeta ret = ai.getBestMove(5, moveStack, true);

    }
}