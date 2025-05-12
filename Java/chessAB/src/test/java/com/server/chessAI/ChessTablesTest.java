package com.server.chessAI;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.github.bhlangonijr.chesslib.Board;
import org.junit.jupiter.api.Test;

import javax.xml.transform.Source;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChessTablesTest {

    @Test
    public void KingValueTest() {
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 e5 Nd5 d4 e6 Bc4 Nb6 Bb5 Be7 Bxc6 dxc6 c3 Qd5 O-O Qe4 Nbd2 Qg4 h3 Qf5 Qe2 Qh5 Ne4 Bd7 a3";
        BoardWrapper b = generateBoardFromMoves(moveStackString);

        AlphaBeta ret = ai.getBestMove(5, b);
        System.out.println(ret.move + " " + ret.eval);
        assertTrue(!ret.move.toString().equals("e8f8"));
    }

    @Test
    public void castleTest() {
        BoardWrapper b = new BoardWrapper();
        b.board.loadFromFen("r2k1b1r/1pp2pp1/p2q1n1p/4p3/2BN2b1/1PP2PP1/P1Q5/R3R1K1 w - - 1 23");
        ChessAI ai = new ChessAI();

        int shitMove = ai.evaluate(b);
        b = new BoardWrapper();
        b.board.loadFromFen("2kr1b1r/1pp2pp1/p2q1n1p/4p3/2BN2b1/1PP2PP1/P1Q5/R3R1K1 w - - 1 23");
        int goodMove = ai.evaluate(b);
        System.out.println(shitMove);
        assertTrue(goodMove < shitMove);
    }


    @Test
    public void castleTest2() {
        // rnbqkb1r/pp2ppp1/2p2n1p/8/8/5B2/PPPP1PPP/RNBQK1NR w KQkq - 0 6
        BoardWrapper b = new BoardWrapper();
        b.board.loadFromFen("rnbqkb1r/pp2ppp1/2p2n1p/8/8/5B2/PPPP1PPP/RNBQ1KNR b kq - 1 6");
        ChessAI ai = new ChessAI();
        int shitMove = ai.evaluate(b);

        b = new BoardWrapper();
        b.board.loadFromFen("rnbqkb1r/pp2ppp1/2p2n1p/8/3P4/5B2/PPP2PPP/RNBQK1NR b KQkq d3 0 6");

        int goodMove = ai.evaluate(b);
        assertTrue(goodMove > shitMove);
    }

//    depth 6
    @Test
    public void castleTest3() {
        BoardWrapper b = generateBoardFromMoves("c4 e6 g3 Nc6 Bg2 e5 e4 Nf6 d3 Nd4 Nc3 Bb4 Ne2 Nxe2 Qxe2 d6 O-O");

        ChessAI ai = new ChessAI();
        String fen = b.board.getFen();

        b.board.doMove("e8f8");
        fen = b.board.getFen();
        int shitMove = ai.evaluate(b);
        b.board.undoMove();

        b.board.doMove("e8g8");
        fen = b.board.getFen();
        int goodMove = ai.evaluate(b);
        b.board.undoMove();

        assertTrue(goodMove < shitMove);
    }

    @Test
    public void badEvalTest() {
        String fen = "rnbqkbnr/5pp1/3pp2p/pB6/3PP3/5N2/PPP2PPP/R1BQK2R b - - 0 3";
        ChessAI ai = new ChessAI();
        BoardWrapper b = new BoardWrapper();
        b.board.loadFromFen(fen);
        int res = ai.evaluate(b);
        assertTrue(res <= 0);
    }

    @Test public void queenEearlyTest() {
        String fen = "rnbqkbnr/ppp1pp1p/3p2p1/8/4P3/2N5/PPPP1PPP/R1BQKBNR w KQkq - 0 3";
        ChessAI ai = new ChessAI();
        BoardWrapper board = new BoardWrapper();
        board.board.loadFromFen(fen);
        board.board.doMove("d1f3");
        int eval = ai.evaluate(board);
        System.out.println(eval);
//        TODO: Add assertions
    }

    @Test public void stupidPassedPawnTest() {

        String fen = "5k2/2p2p2/p4b2/1pP4r/1P2R2N/1P5K/5P2/8 b - - 0 39";
        ChessAI ai = new ChessAI();
        BoardWrapper board = new BoardWrapper();
        board.board.loadFromFen(fen);
        AlphaBeta ret = ai.getBestMove(2, board);
        System.out.println(ret.move);
        // TODO: Add assertions
    }

    @Test
    public void trappedBishopTest() {
        String fen = "r1bqk1nr/pppp1ppp/2n1p3/b7/1PPP4/P7/3NPPPP/R1BQKBNR b KQkq b3 0 5";
        ChessAI ai = new ChessAI();
        BoardWrapper board = new BoardWrapper();
        board.board.loadFromFen(fen);
        int eval = ai.evaluate(board);
        System.out.println(eval);
    }

    private BoardWrapper generateBoardFromMoves(String moves){
        List<String> moveStack = Arrays.stream(moves.split(" ")).toList();
        BoardWrapper b = new BoardWrapper();
        for (String s : moveStack) {
            b.board.doMove(s);
        }
        return b;
    }
}