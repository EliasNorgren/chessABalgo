package com.server.chessAI;

import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hardware-independent search benchmark using the Bratko-Kopec EDP test suite.
 *
 * The primary metric is **node count** — how many times alphaBeta() is invoked.
 * For a deterministic algorithm with a fixed position and depth, this is identical
 * on any machine. Wall-clock time is printed for reference but is NOT the benchmark.
 *
 * Supported EDP operations:
 *   bm  - best move(s) in SAN
 *   id  - position identifier (quoted string)
 *   acd - analysis correct depth (integer); depth to search this position
 *   acn - analysis correct nodes (long); expected node count for regression
 *
 * Example line:
 *   1k1r4/pp1b1R2/3q2pp/4p3/2C5/4Q3/PPP2B2/2K5 b - - bm Qd1+; id "BK.01"; acd 6; acn 123456;
 *
 * Lines starting with # are ignored.
 */
public class SearchBenchmarkTest {

    static final int DEFAULT_DEPTH = 6;

    record EdpRecord(String id, String fen, List<String> bestMovesUci, int depth, Long expectedNodes) {}

    /**
     * Convert a SAN move string to UCI (e.g. "Qd1+" → "d3d1") by applying
     * it on a temporary board and reading back the Move object.
     */
    private String sanToUci(String fen, String san) {
        try {
            BoardWrapper temp = new BoardWrapper();
            temp.board.loadFromFen(fen);
            temp.board.doMove(san);
            Move move = temp.board.getBackup().getLast().getMove();
            return move.toString();
        } catch (Exception e) {
            return san; // fall back to original string if conversion fails
        }
    }

    /** Extract the integer value of an EDP operation like "acd 6" from the operations string. */
    private Integer extractInt(String ops, String key) {
        int idx = ops.indexOf(key + " ");
        if (idx == -1) return null;
        int start = idx + key.length() + 1;
        int end = ops.indexOf(';', start);
        String val = (end >= 0 ? ops.substring(start, end) : ops.substring(start)).trim();
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return null; }
    }

    /** Extract the long value of an EDP operation like "acn 123456" from the operations string. */
    private Long extractLong(String ops, String key) {
        int idx = ops.indexOf(key + " ");
        if (idx == -1) return null;
        int start = idx + key.length() + 1;
        int end = ops.indexOf(';', start);
        String val = (end >= 0 ? ops.substring(start, end) : ops.substring(start)).trim();
        try { return Long.parseLong(val); } catch (NumberFormatException e) { return null; }
    }

    /** Parse one line of EDP format into an EdpRecord with best moves in UCI. */
    private EdpRecord parseEdpLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) return null;

        int bmIdx = line.indexOf(" bm ");
        if (bmIdx == -1) return null;

        String fen = line.substring(0, bmIdx).trim();
        // Append move counters if missing (EDP FENs often omit them)
        if (fen.split(" ").length == 4) fen += " 0 1";

        String ops = line.substring(bmIdx + 4); // everything after "bm "

        // Best moves: everything before the first ";"
        int firstSemi = ops.indexOf(';');
        String bmPart = firstSemi >= 0 ? ops.substring(0, firstSemi).trim() : ops.trim();
        String finalFen = fen;
        List<String> bestMovesUci = Arrays.stream(bmPart.split("\\s+"))
            .map(san -> sanToUci(finalFen, san))
            .toList();

        // id (quoted string)
        String id = "";
        int idIdx = ops.indexOf("id \"");
        if (idIdx >= 0) {
            int start = idIdx + 4;
            int end = ops.indexOf('"', start);
            id = end >= 0 ? ops.substring(start, end) : ops.substring(start);
        }

        int depth = DEFAULT_DEPTH;
        Integer acd = extractInt(ops, "acd");
        if (acd != null) depth = acd;

        Long expectedNodes = extractLong(ops, "acn");

        return new EdpRecord(id, fen, bestMovesUci, depth, expectedNodes);
    }

    private List<EdpRecord> loadEdpRecords() throws Exception {
        List<EdpRecord> records = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("EDP_records.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                EdpRecord record = parseEdpLine(line);
                if (record != null) records.add(record);
            }
        }
        return records;
    }

    /**
     * Main benchmark: run all EDP positions at their individual acd depth.
     * Reports node count vs expected (acn) and whether the best move was found.
     */
    @Test
    public void runBenchmark() throws Exception {
        List<EdpRecord> records = loadEdpRecords();

        System.out.println("=".repeat(100));
        System.out.println("CHESS AI SEARCH BENCHMARK  —  Bratko-Kopec suite");
        System.out.println("Primary metric: node count (hardware-independent)");
        System.out.println("=".repeat(100));
        System.out.printf("%-8s  %5s  %-7s  %12s  %12s  %8s  %-8s  %-10s%n",
            "ID", "Depth", "Correct", "Nodes", "Expected", "Delta%", "Move", "Expected move(s)");
        System.out.println("-".repeat(100));

        long totalNodes = 0;
        long totalExpectedNodes = 0;
        long totalActualNodesForExpected = 0;
        long totalMs = 0;
        int correct = 0;
        int nodeRegressions = 0;
        int positionsWithExpected = 0;

        for (EdpRecord rec : records) {
            ChessAI ai = new ChessAI();
            ai.ponderThreadShouldRun = false;
            BoardWrapper board = new BoardWrapper();
            board.board.loadFromFen(rec.fen());

            long startMs = System.currentTimeMillis();
            AlphaBeta result = ai.getBestMove(rec.depth(), board);
            long elapsedMs = System.currentTimeMillis() - startMs;

            long nodes = ai.getNodeCount();
            totalNodes += nodes;
            totalMs += elapsedMs;

            String moveUci = result.move != null ? result.move.toString() : "null";
            boolean isCorrect = rec.bestMovesUci().contains(moveUci);
            if (isCorrect) correct++;

            String expectedStr = rec.expectedNodes() != null ? String.format("%,d", rec.expectedNodes()) : "-";
            String deltaStr = "-";
            if (rec.expectedNodes() != null) {
                double delta = 100.0 * (nodes - rec.expectedNodes()) / rec.expectedNodes();
                deltaStr = String.format("%+.1f%%", delta);
                if (delta > 3) nodeRegressions++; // only flag increases — fewer nodes = improvement
                totalExpectedNodes += rec.expectedNodes();
                totalActualNodesForExpected += nodes;
                positionsWithExpected++;
            }

            System.out.printf("%-8s  %5d  %-7s  %,12d  %12s  %8s  %-8s  %-10s  (%dms)%n",
                rec.id(), rec.depth(), isCorrect ? "YES" : "NO",
                nodes, expectedStr, deltaStr,
                moveUci, String.join("/", rec.bestMovesUci()),
                elapsedMs);
        }

        System.out.println("=".repeat(100));
        System.out.printf("TOTAL: %d/%d correct  nodes=%,d  time=%dms",
            correct, records.size(), totalNodes, totalMs);
        boolean hadNodeRegressions = false;
        if (nodeRegressions > 0){
            hadNodeRegressions = true;
            System.out.printf("  *** %d node regression(s) >3%% ***", nodeRegressions);
        }
        boolean hadAvgRegression = false;
        if (positionsWithExpected > 0) {
            double avgDelta = 100.0 * (totalActualNodesForExpected - totalExpectedNodes) / totalExpectedNodes;
            System.out.printf("  avg-delta=%+.1f%%", avgDelta);
            if (avgDelta > 3) {
                hadAvgRegression = true;
                System.out.printf("  *** avg node regression >3%% ***");
            }
        }
        System.out.println();
        System.out.println("=".repeat(100));
        boolean allMovesCorrect = correct == records.size();
        boolean success = !hadNodeRegressions && !hadAvgRegression && allMovesCorrect;
        Assertions.assertTrue(success);
    }

    /**
     * Depth-scaling benchmark: run all EDP positions across increasing depths to show
     * how node count grows (effective branching factor indicator).
     */
//    @Test
    public void branchingFactorBenchmark() throws Exception {
        List<EdpRecord> records = loadEdpRecords();

        System.out.println("=".repeat(70));
        System.out.println("BRANCHING FACTOR BENCHMARK  —  Bratko-Kopec suite");
        System.out.println("=".repeat(70));
        System.out.printf("%-6s  %14s  %8s  %8s%n", "Depth", "Total Nodes", "Ratio", "Time(ms)");
        System.out.println("-".repeat(70));

        long prevNodes = 1;
        for (int depth = 1; depth <= 6; depth++) {
            long depthNodes = 0;
            long depthMs = 0;

            for (EdpRecord rec : records) {
                ChessAI ai = new ChessAI();
                ai.ponderThreadShouldRun = false;
                BoardWrapper board = new BoardWrapper();
                board.board.loadFromFen(rec.fen());

                long startMs = System.currentTimeMillis();
                ai.getBestMove(depth, board);
                depthMs += System.currentTimeMillis() - startMs;
                depthNodes += ai.getNodeCount();
            }

            System.out.printf("%-6d  %,14d  %8.2fx  %8d%n",
                depth, depthNodes, (double) depthNodes / prevNodes, depthMs);
            prevNodes = depthNodes;
        }

        System.out.println("=".repeat(70));
        System.out.println("Lower ratio = better pruning (ideal alpha-beta ~sqrt(N) of minimax)");
    }

    /**
     * Debug test: print the scored move ordering for BK.21 at the root, then
     * show node counts at each depth so you can see where the explosion happens.
     */
//    @Test
    public void debugBK21() throws Exception {
        String fen = "2r3k1/pppR1pp1/4p3/4P1P1/5P2/1P4K1/P1P5/8 w - -";

        System.out.println("=".repeat(70));
        System.out.println("DEBUG: —  root move ordering");
        System.out.println("FEN: " + fen);
        System.out.println("=".repeat(70));
        BoardWrapper board = new BoardWrapper();
        board.board.loadFromFen(fen);
        ChessAI ai = new ChessAI();
        ai.ponderThreadShouldRun = false;
        ai.printMoveOrder(board);

        System.out.println();
        System.out.println("--- NODE COUNT PER DEPTH ---");
        for (int depth = 9; depth <= 11; depth++) {
            BoardWrapper b = new BoardWrapper();
            b.board.loadFromFen(fen);
            ChessAI a = new ChessAI();
            a.ponderThreadShouldRun = false;
            AlphaBeta result = a.getBestMove(depth, b);
            System.out.printf("  depth=%d  nodes=%,d  move=%s%n", depth, a.getNodeCount(), result.move);
        }
        System.out.println("=".repeat(70));
    }
}
