package com.server;

import java.util.List;

public class GetBestMoveRequest {
    private String fen;
    private List<String> moveStack;
    private int depth;
    private int processes;

    // Getters and setters

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public List<String> getMoveStack() {
        return moveStack;
    }

    public void setMoveStack(List<String> moveStack) {
        this.moveStack = moveStack;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getProcesses() {
        return processes;
    }

    public void setProcesses(int processes) {
        this.processes = processes;
    }

    @Override
    public String toString() {
        return "Fen: " + this.getFen() + "\n"
                + "MoveStack: " + String.join(" ", this.moveStack) + "\n"
                + "Depth: " + this.getDepth() + "\n"
                + "Processes: " + this.getProcesses() + "\n";
    }
}