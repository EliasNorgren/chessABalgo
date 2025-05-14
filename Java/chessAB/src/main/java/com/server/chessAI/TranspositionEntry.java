package com.server.chessAI;

import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Stack;

public class TranspositionEntry {
    int eval;
    Move bestMove;
    int depth;
    BoundType bound;
    Stack<Move> line;

    TranspositionEntry(Move bestMove, int eval, int depth, BoundType bound, Stack<Move> line) {
        this.eval = eval;
        this.bestMove = bestMove;
        this.depth = depth;
        this.bound = bound;
        this.line = line;
    }

    public enum BoundType {
        EXACT, LOWERBOUND, UPPERBOUND
    }

}
