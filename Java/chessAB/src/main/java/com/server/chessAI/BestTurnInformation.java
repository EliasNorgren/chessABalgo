package com.server.chessAI;

public class BestTurnInformation {
    public AlphaBeta bestMove;
    public int depth;
    public double timeElapsed;

    public BestTurnInformation(AlphaBeta bestMove, int startDepth) {
        this.bestMove = bestMove;
        this.depth = startDepth;
    }

    @Override
    public BestTurnInformation clone() throws CloneNotSupportedException {
        BestTurnInformation clone = new BestTurnInformation(this.bestMove, this.depth);
        clone.timeElapsed = this.timeElapsed;
        return clone;
    }

    @Override
    public String toString() {
        return "AlphaBeta: " + this.bestMove + " Time elapsed: " + this.timeElapsed + " Depth: " + this.depth;
    }
}
