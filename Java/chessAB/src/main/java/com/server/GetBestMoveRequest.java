package com.server;

import java.util.List;

public class GetBestMoveRequest {
    private String fen;
    private List<String> moveStack;
    private String max_time;
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

    public String getMax_time() {
        return max_time;
    }

    public void setMax_time(String max_time) {
        this.max_time = max_time;
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
                + "Depth: " + this.getMax_time() + "\n"
                + "Processes: " + this.getProcesses() + "\n";
    }
}