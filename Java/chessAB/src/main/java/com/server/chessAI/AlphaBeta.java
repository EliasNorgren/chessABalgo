package com.server.chessAI;

import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Stack;

public class AlphaBeta {

    public Move move;
    public int eval;
    public Stack<Move> line;

    public AlphaBeta(Move move, int eval, Stack<Move> line) {
        this.move = move;
        this.eval = eval;
        this.line = line;
    }

    @Override
    public String toString() {
        return "Move: " + move + " eval: " + eval;
    }
}
