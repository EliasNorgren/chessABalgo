package com.server;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.server.chessAI.BestTurnInformation;
import com.server.chessAI.BoardWrapper;
import com.server.chessAI.ChessAI;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/chess")
public class Controller {

    private volatile PonderThread currentThread;
    private volatile long currentHash;

    @PostMapping("/getBestMove")
    public synchronized GetBestMoveResponse getBestMove(@RequestBody GetBestMoveRequest request)
            throws CloneNotSupportedException, InterruptedException {

        long start = System.currentTimeMillis();
        ChessAI ai = new ChessAI();
        double maxTime = Double.parseDouble(request.getMax_time());

        List<String> moveStack = request.getMoveStack();

        Board b = new Board();
        for (String move : moveStack) {
            b.doMove(move);
        }

        long hash = b.getZobristKey();

        BestTurnInformation ret;

        // ✅ CASE 1: No match → kill current thread
        if (currentThread == null || currentHash != hash) {

            if (currentThread != null) {
                currentThread.stopThread();
            }

            ret = ai.getBestMove(new BoardWrapper(b), maxTime, false);

        } else {
            // ✅ CASE 2: Ponder hit
            PonderThread runningThread = currentThread;

            BestTurnInformation currentBest = runningThread.getBestMoveInfo();

            while (currentBest.timeElapsed < maxTime) {
                runningThread.getSemaphore().acquire();
                currentBest = runningThread.getBestMoveInfo();
            }

            ret = currentBest;
            runningThread.stopThread();
        }

        // -------------------------
        // Start next ponder thread
        // -------------------------

        Move bestMove = ret.bestMove.move;
        Move ponderMove = ret.bestMove.line.reversed().get(1);

        Board nextBoard = new Board();
        for (String move : moveStack) {
            nextBoard.doMove(move);
        }

        nextBoard.doMove(bestMove);
        nextBoard.doMove(ponderMove);

        long nextHash = nextBoard.getZobristKey();

        PonderThread newThread = new PonderThread(nextBoard);

        newThread.exitRunner = () -> {
            if (currentThread == newThread) {
                currentThread = null;
            }
        };

        currentThread = newThread;
        currentHash = nextHash;

        newThread.start();

        // -------------------------

        GetBestMoveResponse response = new GetBestMoveResponse();
        response.setBestMove(bestMove.toString());
        response.setTimeTaken(System.currentTimeMillis() - start);
        response.setEval(ret.bestMove.eval);
        response.setDepth(ret.depth);
        response.setPonderMove(ponderMove.toString());

        return response;
    }
}