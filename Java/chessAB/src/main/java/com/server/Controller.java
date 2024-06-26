package com.server;


import com.server.chessAI.AlphaBeta;
import com.server.chessAI.ChessAI;
import org.springframework.web.bind.annotation.*;

import java.util.Timer;


@RestController
@RequestMapping("/chess")
public class Controller {

    private final ChessAI ai = new ChessAI();

    @PostMapping("/getBestMove")
    public GetBestMoveResponse getBestMove(@RequestBody GetBestMoveRequest request) {
        // Implement the logic to get the best move based on the request parameters
        long start = System.currentTimeMillis();
        AlphaBeta ret = ai.getBestMove(request.getFen(), request.getDepth());
        long timeElapsed = System.currentTimeMillis() - start;
        GetBestMoveResponse response = new GetBestMoveResponse();
        // Example values
        response.setBestMove(ret.prevMove.toString());

        response.setTimeTaken(timeElapsed);
        response.setEval(ret.eval);

        return response;
    }
}