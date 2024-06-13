package com.server.chessAI;

import com.github.bhlangonijr.chesslib.move.Move;

public class AlphaBeta {

    public Move prevMove;
    public int eval;

    public AlphaBeta(Move prevMove, int eval) {
        this.prevMove = prevMove;
        this.eval = eval;
    }
}
