package com.server.chessAI;

import com.github.bhlangonijr.chesslib.move.Move;

public class TranspositionEntry {
    int eval;
    Move bestMove;
    int depth;
    BoundType bound;

    TranspositionEntry(Move bestMove, int eval, int depth, BoundType bound) {
        this.eval = eval;
        this.bestMove = bestMove;
        this.depth = depth;
        this.bound = bound;
    }

    public enum BoundType {
        EXACT, LOWERBOUND, UPPERBOUND
    }

}
