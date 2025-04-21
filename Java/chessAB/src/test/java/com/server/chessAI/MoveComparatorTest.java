package com.server.chessAI;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveComparatorTest {
    @Test
    public void moveSortingTest(){
        Board b = new Board();
        b.loadFromFen("k7/8/8/3N4/1b6/8/8/7K w");
        List<Move> moves = new MoveSorter(b).sort();
        List<String> moveStrings = moves.stream().map(s -> s.toString()).toList();
        assertTrue(moveStrings.indexOf("d5c7") == 1 || moveStrings.indexOf("d5c7") == 0);
        assertTrue(moveStrings.indexOf("d5b6") == 1 || moveStrings.indexOf("d5b6") == 0);
        assertEquals(2, moveStrings.indexOf("d5b4"));

        System.out.println();
    }
}