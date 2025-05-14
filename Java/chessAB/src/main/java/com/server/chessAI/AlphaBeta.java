package com.server.chessAI;

import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Queue;

public class AlphaBeta {

    public Move move;
    public int eval;

    public AlphaBeta(Move move, int eval) {
        this.move = move;
        this.eval = eval;
    }

    @Override
    public String toString() {
        return "Move: " + move + " eval: " + eval;
    }
}
