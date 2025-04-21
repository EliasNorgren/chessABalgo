package com.server.chessAI;

public class BestTurnInformation {
    public AlphaBeta bestMove;
    public int depth;

    public BestTurnInformation(AlphaBeta bestMove, int startDepth) {
        this.bestMove = bestMove;
        this.depth = startDepth;
    }
}
