package com.server;


import com.server.chessAI.BestTurnInformation;
import com.server.chessAI.ChessAI;
import com.server.chessAI.InternalEval.MyEval;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/chess")
public class Controller {


    @PostMapping("/getBestMove")
    public GetBestMoveResponse getBestMove(@RequestBody GetBestMoveRequest request) {
        System.out.println("Received request: \n" + request.toString());
        // Implement the logic to get the best move based on the request parameters
        long start = System.currentTimeMillis();
        ChessAI ai = new ChessAI();
        BestTurnInformation ret = ai.getBestMove(Double.parseDouble(request.getMax_time()), request.getMoveStack());
        long timeElapsed = System.currentTimeMillis() - start;
        GetBestMoveResponse response = new GetBestMoveResponse();
        // Example values
        response.setBestMove(ret.bestMove.move.toString());
        response.setTimeTaken(timeElapsed);
        response.setEval(ret.bestMove.eval);
        response.setDepth(ret.depth);

        return response;
    }
}