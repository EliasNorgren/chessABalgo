package com.server;


import com.server.chessAI.AlphaBeta;
import com.server.chessAI.BestTurnInformation;
import com.server.chessAI.ChessAI;
import org.springframework.web.bind.annotation.*;

import java.util.Timer;


@RestController
@RequestMapping("/chess")
public class Controller {

    private final ChessAI ai = new ChessAI();

    @PostMapping("/getBestMove")
    public GetBestMoveResponse getBestMove(@RequestBody GetBestMoveRequest request) {
        System.out.println("Received request: \n" + request.toString());
        // Implement the logic to get the best move based on the request parameters
        long start = System.currentTimeMillis();
        BestTurnInformation ret = ai.getBestMove(request.getDepth(), request.getMoveStack());
        long timeElapsed = System.currentTimeMillis() - start;
        GetBestMoveResponse response = new GetBestMoveResponse();
        // Example values
        response.setBestMove(ret.bestMove.prevMove.toString());
        response.setTimeTaken(timeElapsed);
        response.setEval(ret.bestMove.eval);
        response.setDepth(ret.depth);

        return response;
    }
}