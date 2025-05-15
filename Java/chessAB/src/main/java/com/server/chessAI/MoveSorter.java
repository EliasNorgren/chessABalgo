package com.server.chessAI;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Comparator;
import java.util.List;

public class MoveSorter {

    private final Board board;
    private final Move candidateMove;
    private final boolean firstRecursion;

    public MoveSorter(Board board, Move candidateMove, boolean firstRecursion){
        this.board = board;
        this.candidateMove = candidateMove;
        this.firstRecursion = firstRecursion;
    }

    private record CheckComparator(Board board) implements Comparator<Move> {

        @Override
            public int compare(Move o1, Move o2) {

                Side side = this.board.getSideToMove();

                this.board.doMove(o1);
                boolean o1CausedCheck = this.board.isKingAttacked();
                this.board.undoMove();

                this.board.doMove(o2);
                boolean o2CausedCheck = this.board.isKingAttacked();
                this.board.undoMove();

                if (o1CausedCheck && !o2CausedCheck) {
                    return -1;
                } else if (o2CausedCheck && !o1CausedCheck) {
                    return 1;
                }
                return 0;
            }
        }

    private record AttackComparator(Board board) implements Comparator<Move> {

        @Override
            public int compare(Move o1, Move o2) {

                long enemyPieces = this.board.getBitboard(this.board.getSideToMove().flip());
                boolean o1Attack = (o1.getTo().getBitboard() & enemyPieces) != 0L;
                boolean o2Attack = (o2.getTo().getBitboard() & enemyPieces) != 0L;

                if (o1Attack && !o2Attack) {
                    return -1; // o1 is a capture, o2 is not
                } else if (!o1Attack && o2Attack) {
                    return 1;
                }
                return 0;
            }
        }

    public List<Move> sort() {
        List<Move> moves = this.board.legalMoves();
        moves.sort(new AttackComparator(this.board));
        moves.sort(new CheckComparator(this.board));
        if (this.firstRecursion && this.candidateMove != null) {
            moves.addFirst(this.candidateMove);
        }
        return moves;
    }
}