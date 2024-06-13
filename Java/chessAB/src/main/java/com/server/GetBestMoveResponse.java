package com.server;

public class GetBestMoveResponse {
    private String bestMove;
    private float timeTaken;
    private float eval;

    // Getters and setters

    public String getBestMove() {
        return bestMove;
    }

    public void setBestMove(String bestMove) {
        this.bestMove = bestMove;
    }

    public float getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(float timeTaken) {
        this.timeTaken = timeTaken;
    }

    public float getEval() {
        return eval;
    }

    public void setEval(float eval) {
        this.eval = eval;
    }
}