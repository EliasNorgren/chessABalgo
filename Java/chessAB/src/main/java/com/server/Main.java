package com.server;

import com.server.chessAI.ChessAI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
//        ChessAI ai = new ChessAI();
//        ai.getBestMove("r1bk1b1r/pppp1p1p/2n2p2/1B2p3/4Q3/5N2/PPPP1PPP/R1B2RK1 w KQkq - 0 1", 1);
    }
}
