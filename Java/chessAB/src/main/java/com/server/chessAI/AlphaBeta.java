package com.server.chessAI;

import com.github.bhlangonijr.chesslib.move.Move;

public class AlphaBeta {

    public Move move;
    public int eval;

    public AlphaBeta(Move prevMove, int eval) {
        this.move = prevMove;
        this.eval = eval;
    }
}
