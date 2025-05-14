package com.server;

public class GetBestMoveResponse {
    private String bestMove;
    private float timeTaken;
    private float eval;
    private int depth;

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    private String line;
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

    public void setDepth(int depth) {
        this.depth = depth;
    }
}