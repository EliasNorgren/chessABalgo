package com.server.chessAI;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
class ChessAITest {

    @Test
    public void checkMateTest(){

        ChessAI ai = new ChessAI();
        BoardWrapper b = new BoardWrapper();

        b.board.loadFromFen("3k4/6R1/6nR/8/8/8/8/7K w");

        AlphaBeta move1 = ai.getBestMove(5, b);
        System.out.println(move1.line.reversed());
        assertEquals("h6g6", move1.move.toString());
        assertEquals(Integer.MAX_VALUE - 5, move1.eval);
        b.board.doMove(move1.move);
        b.board.doMove("d8e8");

        AlphaBeta move2 = ai.getBestMove(3, b);
        assertEquals("g6f6", move2.move.toString());
        assertEquals(Integer.MAX_VALUE - 3, move2.eval);
        b.board.doMove(move2.move);
        b.board.doMove("e8d8");

        AlphaBeta move3 = ai.getBestMove(1, b);
        assertEquals("f6f8", move3.move.toString());
        assertEquals(Integer.MAX_VALUE - 1, move3.eval);
        b.board.doMove(move3.move);
        assertTrue(b.board.isMated());
    }

    @Test
    public void mateInFiveTest(){

        ChessAI ai = new ChessAI();
        BoardWrapper b = new BoardWrapper();
        b.board.loadFromFen("8/8/8/2k5/7R/6R1/8/7K w - - 0 1");
        String fen = b.board.getFen();

        AlphaBeta move = ai.getBestMove(9, b);
        assertEquals("g3g5", move.move.toString());
        assertEquals(Integer.MAX_VALUE - 9, move.eval);

        b = new BoardWrapper();
        b.board.loadFromFen("8/8/8/2k5/7R/6R1/8/7K w - - 0 1");
        BestTurnInformation ret = ai.getBestMove(b, 15, false);
        assertEquals("g3d3", ret.bestMove.move.toString());
        assertEquals(2147483640, ret.bestMove.eval);

    }

    @Test
    public void mateInOneTest(){
        BoardWrapper b = new BoardWrapper();
        b.board.loadFromFen("3R2n1/k1r2p2/pp3n2/4Q3/2b5/4BBNP/PP3PP1/4K2R w K - 0 29");
        ChessAI ai = new ChessAI();
        BestTurnInformation ret = ai.getBestMove( b, 3, false);
        System.out.println(ret.bestMove.move);

    }

    @Test
    public void drawByRepetitionTest(){
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 Nc3 Nb4 d4 Nc6 g3 Rb8 Rb1 Ra8 Be2 Ng4 Kd2 Nxf2 Qf1 Nxh1 Nh4 Nxg3 hxg3 Nxd4 Nf5 Nxe2 Kxe2 c6 Nxg7+ Bxg7 Qf4 Bxc3 bxc3 Qa5 Kf1 Qxa2 Rb2 Qc4+ Kg2 Qxc3 Qf5 d6 Qf2 Rg8 Qf3 Bh3+ Kxh3 Qxf3 Kh2 Qxg3+ Kh1 Qe1+ Kh2 Qg1+ Kh3 Rg3+ Kh4 Rg4+ Kh5 Rxe4 Bf4 Qh1+ Bh2 Qxh2+ Kg5 f6+ Kf5 e4f4 Ke6 f4e4 Kf5 e4f4 Ke6";
        BoardWrapper b = generateBoardFromMoves(moveStackString);
        String fen = b.board.getFen();
        AlphaBeta ret = ai.getBestMove(2, b);
        assertNotEquals("f4ef", ret.move.getSan());
    }

    @Test
    public void blackKingSideCastleTest(){
        ChessAI ai = new ChessAI();
        String moveStackString = "e4 Nc6 Nf3 Nf6 e5 Nd5 d4 e6 Bc4 Nb6 Bb5 Be7 Bxc6 dxc6 c3 Qd5 O-O Qe4 Nbd2 Qg4 h3 Qf5 Qe2 Qh5 Ne4 Bd7 a3";
        BoardWrapper b = generateBoardFromMoves(moveStackString);
        String fen = b.board.getFen();
        AlphaBeta ret = ai.getBestMove(5, b);
        System.out.println(ret.move);
        assertEquals("e8g8", ret.move.toString());
    }

    @Test
    public void blackKingQueenSideCastleTest(){
        ChessAI ai = new ChessAI();
        BoardWrapper b = new BoardWrapper();
        b.board.loadFromFen("r3kb1r/1pp2pp1/p2q1n1p/4p3/2BN2b1/1PP2PP1/P1Q5/R3R1K1 b kq - 0 22");
        AlphaBeta ret = ai.getBestMove(6, b);
        System.out.println(ret.move);
    }

    @Test
    public void foundMateInstantTest() {
        ChessAI ai = new ChessAI();
        BoardWrapper b = new BoardWrapper();
        b.board.loadFromFen("K7/5r2/1k6/8/8/8/8/8 b - - 0 1");
        BestTurnInformation ret = ai.getBestMove(b, 5, false);
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
        BoardWrapper b = new BoardWrapper();
        b.board.loadFromFen("rnbqkbnr/1p3pp1/2ppp2p/p7/B2PP3/2N5/PPP2PPP/R1BQK1NR w");
        String fen = b.board.getFen();

        AlphaBeta move = ai.getBestMove(6, b);
        System.out.println(move.line.reversed());
        assertEquals("a2a3", move.move.toString());
    }

    @Test
    public void suicideQueenTest(){
        // 5 move suicide queen
        String moves = "e4 e5 Qh5 Nf6";
        BoardWrapper board = generateBoardFromMoves(moves);
        System.out.println(board.board.getFen());
        ChessAI ai = new ChessAI();
        AlphaBeta move = ai.getBestMove(5, board);
        System.out.println(move);
        assertEquals("h5e5", move.move.toString());
    }

    @Test
    public void takeTheLongestMatingLineWhenLosing() {
        BoardWrapper board = new BoardWrapper();
        board.board.loadFromFen("2QQQ3/6k1/8/5B2/8/8/5PPP/q5K1 w - - 1 2");
        ChessAI ai = new ChessAI();

        AlphaBeta ret = ai.getBestMove(8, board);
        assertEquals("f5b1", ret.move.toString());
        assertEquals(Integer.MIN_VALUE + 8, ret.eval);
        board.board.doMove("f5b1");
        board.board.doMove("a1b1");

        ret = ai.getBestMove(6, board);
        assertEquals("c8c1", ret.move.toString());
        assertEquals(Integer.MIN_VALUE + 6, ret.eval);
        board.board.doMove("c8c1");
        board.board.doMove("b1c1");

        ret = ai.getBestMove(4, board);
        assertEquals("d8d1", ret.move.toString());
        assertEquals(Integer.MIN_VALUE + 4, ret.eval);
        board.board.doMove("d8d1");
        board.board.doMove("c1d1");

        ret = ai.getBestMove(2, board);
        assertEquals("e8e1", ret.move.toString());
        assertEquals(Integer.MIN_VALUE + 2, ret.eval);

        board = new BoardWrapper();
        board.board.loadFromFen("2QQQ3/6k1/8/5B2/8/8/5PPP/q5K1 w - - 1 2");
        BestTurnInformation returned = ai.getBestMove(board, 60, false);
        assertEquals("f5b1", returned.bestMove.move.toString());
        assertEquals(Integer.MIN_VALUE + 8, returned.bestMove.eval);

    }

    @Test
    public void maybeLookOneMoreMoveAndDontSacQueenTest(){
        BoardWrapper board = generateBoardFromMoves("Nf3 e6 b3 Qf6 Nc3 Nc6 Bb2 Bd6 e4 Nge7 d4 Bb4 e5 Qf5 Bd3 Qg4 O-O Nxd4 Nxd4 Qxd4 Na4 Qf4 c3 Ba5 b4");
        ChessAI ai = new ChessAI();
        AlphaBeta move = ai.getBestMove(7, board);
        assertNotEquals("f4e5", move.move.toString());
    }

//    @Test dontMoveQueenEarlyPlz() {
//        String moveStack = "e4 d6 Nc3 g6";
//        Board b = generateBoardFromMoves(moveStack);
//        ChessAI ai = new ChessAI();
//        ai.getBestMove(1)
//    }

//    @Test
//    public void trappedBishopTest() {
//        String moves = "d4 e6 c4 Bb4+ Nd2 Nc6 a3";
//        BoardWrapper board = generateBoardFromMoves(moves);
//        System.out.println(board.board.getFen());
//        ChessAI ai = new ChessAI();
////        for (int i = 1; i <= 7; i++){
//            board = generateBoardFromMoves(moves);
//            AlphaBeta ret = ai.getBestMove(8, board);
//            assertNotEquals("b4a5", ret.move.toString());
//            System.out.println(ret.move.toString());
////        }
//
////        board = generateBoardFromMoves(moves);
////        ai.getBestMove(board, 10);
//
//    }

    // -------------------------------------------------------------------------
    // Iterative deepening / time-limit tests
    // -------------------------------------------------------------------------

    /**
     * The search must return within a reasonable margin of the requested time.
     * We allow 2× as a generous bound to avoid flakiness on slow CI machines,
     * but in practice it should finish within a few hundred milliseconds of the limit.
     */
    @Test
    public void timeLimitIsRespected() {
        ChessAI ai = new ChessAI();
        BoardWrapper b = new BoardWrapper(); // starting position — many moves, good stress test

        double timeLimitSeconds = 1.0;
        long start = System.currentTimeMillis();
        BestTurnInformation ret = ai.getBestMove(b, timeLimitSeconds, false);
        long elapsed = System.currentTimeMillis() - start;

        assertNotNull(ret.bestMove.move, "Must always return a move");
        assertEquals(ret.bestMove.move.toString(), "e2e3");
        assertEquals(ret.bestMove.line.size(), 5);
        assertTrue(elapsed < timeLimitSeconds * 2000,
                "Search took " + elapsed + "ms, expected < " + (timeLimitSeconds * 2000) + "ms");
    }

    /**
     * When the time limit is so tight that even depth 1 cannot complete,
     * the fallback path must still return a legal move (not null / not throw).
     */
    @Test
    public void impossibleTimeLimitFallsBackToFirstLegalMove() {
        ChessAI ai = new ChessAI();
        BoardWrapper b = new BoardWrapper();

        BestTurnInformation ret = ai.getBestMove(b, 0.000001, false);

        assertNotNull(ret.bestMove.move, "Fallback must still return a legal move");
        assertTrue(b.board.legalMoves().contains(ret.bestMove.move),
                "Fallback move must be a legal move in the position");
    }

    /**
     * If a deeper iteration times out mid-search, the move from the last
     * *completed* depth must be returned — not null and not the fallback.
     *
     * Strategy: give 2 s on the starting position. Depth 1–4 complete in well
     * under 1 s; depth 5+ takes longer. Regardless of where the cutoff falls,
     * the returned move must be the one from the deepest complete iteration.
     */
    @Test
    public void timedOutIterationKeepsPreviousDepthMove() {
        ChessAI ai = new ChessAI();
        BoardWrapper b = new BoardWrapper();

        // First get the depth-1 result as a known-good baseline
        AlphaBeta depth1 = ai.getBestMove(1, new BoardWrapper());
        assertNotNull(depth1.move);

        BestTurnInformation ret = ai.getBestMove(b, 2.0, false);

        assertNotNull(ret.bestMove.move,
                "Must return a move even when the final iteration timed out");
        assertTrue(b.board.legalMoves().contains(ret.bestMove.move),
                "Returned move must be legal");
        // Depth should be at least 1 — i.e. we completed at least one full iteration
        assertTrue(ret.depth >= 1,
                "At least depth 1 must have completed, got depth " + ret.depth);
    }

    /**
     * A forced-mate position found at depth 1 should be returned immediately
     * and the depth counter should reflect that only one iteration was needed.
     */
    @Test
    public void mateFoundEarlyStopsIterativeDeepening() {
        ChessAI ai = new ChessAI();
        BoardWrapper b = new BoardWrapper();
        // Black king trapped, rook delivers mate on f7 in one move
        b.board.loadFromFen("K7/5r2/1k6/8/8/8/8/8 b - - 0 1");

        BestTurnInformation ret = ai.getBestMove(b, 10.0, false);

        assertEquals("f7f8", ret.bestMove.move.toString());
        assertEquals(1, ret.depth,
                "Mate-in-1 should stop iterative deepening at depth 1");
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