package com.server.chessAI;

import com.github.bhlangonijr.chesslib.Board;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChessTablesTest {

    @Test
    public void KingValueTest() {
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 e5 Nd5 d4 e6 Bc4 Nb6 Bb5 Be7 Bxc6 dxc6 c3 Qd5 O-O Qe4 Nbd2 Qg4 h3 Qf5 Qe2 Qh5 Ne4 Bd7 a3";
        Board b = generateBoardFromMoves(moveStackString);

        AlphaBeta ret = ai.getBestMove(5, b);
        System.out.println(ret.move + " " + ret.eval);
        assertTrue(!ret.move.toString().equals("e8f8"));
    }

    @Test
    public void castleTest() {
        Board b = new Board();
        b.loadFromFen("r2k1b1r/1pp2pp1/p2q1n1p/4p3/2BN2b1/1PP2PP1/P1Q5/R3R1K1 w - - 1 23");
        ChessAI ai = new ChessAI();

        int shitMove = ai.evaluate(b);
        b = new Board();
        b.loadFromFen("2kr1b1r/1pp2pp1/p2q1n1p/4p3/2BN2b1/1PP2PP1/P1Q5/R3R1K1 w - - 1 23");
        int goodMove = ai.evaluate(b);
        System.out.println(shitMove);
        assertTrue(goodMove < shitMove);
    }


    @Test
    public void castleTest2() {
        // rnbqkb1r/pp2ppp1/2p2n1p/8/8/5B2/PPPP1PPP/RNBQK1NR w KQkq - 0 6
        Board b = new Board();
        b.loadFromFen("rnbqkb1r/pp2ppp1/2p2n1p/8/8/5B2/PPPP1PPP/RNBQ1KNR b kq - 1 6");
        ChessAI ai = new ChessAI();
        int shitMove = ai.evaluate(b);

        b = new Board();
        b.loadFromFen("rnbqkb1r/pp2ppp1/2p2n1p/8/3P4/5B2/PPP2PPP/RNBQK1NR b KQkq d3 0 6");

        int goodMove = ai.evaluate(b);
        assertTrue(goodMove > shitMove);
    }

//    depth 6
    @Test
    public void castleTest3() {
        Board b = generateBoardFromMoves("c4 e6 g3 Nc6 Bg2 e5 e4 Nf6 d3 Nd4 Nc3 Bb4 Ne2 Nxe2 Qxe2 d6 O-O");

        ChessAI ai = new ChessAI();
        String fen = b.getFen();

        b.doMove("e8f8");
        fen = b.getFen();
        int shitMove = ai.evaluate(b);
        b.undoMove();

        b.doMove("e8g8");
        fen = b.getFen();
        int goodMove = ai.evaluate(b);
        b.undoMove();

        assertTrue(goodMove < shitMove);
    }

    @Test
    public void badEvalTest() {
        String fen = "rnbqkbnr/5pp1/3pp2p/pB6/3PP3/5N2/PPP2PPP/R1BQK2R b - - 0 3";
        ChessAI ai = new ChessAI();
        Board b = new Board();
        b.loadFromFen(fen);
        int res = ai.evaluate(b);
        assertTrue(res <= 0);
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