package com.server.chessAI;

import com.github.bhlangonijr.chesslib.Board;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChessTablesTest {

    @Test
    void KingValueTest() {
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 e5 Nd5 d4 e6 Bc4 Nb6 Bb5 Be7 Bxc6 dxc6 c3 Qd5 O-O Qe4 Nbd2 Qg4 h3 Qf5 Qe2 Qh5 Ne4 Bd7 a3";
        Board b = generateBoardFromMoves(moveStackString);

        AlphaBeta ret = ai.getBestMove(5, b);
        System.out.println(ret.prevMove + " " + ret.eval);
        assertTrue(!ret.prevMove.toString().equals("e8f8"));
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