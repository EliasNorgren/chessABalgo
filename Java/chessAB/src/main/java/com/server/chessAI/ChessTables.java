package com.server.chessAI;

public class ChessTables {

    public static final int[] PIECE_VALUES = {
            100,320,330,500,900,2000,
            100,320,330,500,900,2000
    };

    public static final int[][] PAWN_TABLE_WHITE = {
            {0,  0,  0,  0,  0,  0,  0,  0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {5,  5, 10, 25, 25, 10,  5,  5},
            {0,  0,  0, 20, 20,  0,  0,  0},
            {5, -5, -10,  0,  0, -10, -5,  5},
            {5, 10, 10, -20, -20, 10, 10,  5},
            {0,  0,  0,  0,  0,  0,  0,  0}
    };

    public static final int[][] KNIGHT_TABLE_WHITE = {
            {-50, -40, -30, -30, -30, -30, -40, -50},
            {-40, -20,  0,  0,  0,  0, -20, -40},
            {-30,  0, 10, 15, 15, 10,  0, -30},
            {-30,  5, 15, 20, 20, 15,  5, -30},
            {-30,  0, 15, 20, 20, 15,  0, -30},
            {-30,  5, 10, 15, 15, 10,  5, -30},
            {-40, -20,  0,  5,  5,  0, -20, -40},
            {-50, -40, -30, -30, -30, -30, -40, -50}
    };

    public static final int[][] BISHOP_TABLE_WHITE = {
            {-20, -10, -10, -10, -10, -10, -10, -20},
            {-10,  0,  0,  0,  0,  0,  0, -10},
            {-10,  0,  5, 10, 10,  5,  0, -10},
            {-10,  5,  5, 10, 10,  5,  5, -10},
            {-10,  0, 10, 10, 10, 10,  0, -10},
            {-10, 10, 10, 10, 10, 10, 10, -10},
            {-10,  5,  0,  0,  0,  0,  5, -10},
            {-20, -10, -10, -10, -10, -10, -10, -20}
    };

    public static final int[][] ROOK_TABLE_WHITE = {
            {0,  0,  0,  0,  0,  0,  0,  0},
            {5, 10, 10, 10, 10, 10, 10,  5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {0,  0,  0,  5,  5,  0,  0,  0}
    };

    public static final int[][] QUEEN_TABLE_WHITE = {
            {-20, -10, -10, -5, -5, -10, -10, -20},
            {-10,  0,  0,  0,  0,  0,  0, -10},
            {-10,  0,  5,  5,  5,  5,  0, -10},
            {-5,  0,  5,  5,  5,  5,  0, -5},
            {0,  0,  5,  5,  5,  5,  0, -5},
            {-10,  5,  5,  5,  5,  5,  0, -10},
            {-10,  0,  5,  0,  0,  0,  0, -10},
            {-20, -10, -10, -5, -5, -10, -10, -20}
    };

    public static final int[][] KING_TABLE_WHITE = {
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-20, -30, -30, -40, -40, -30, -30, -20},
            {-10, -20, -20, -20, -20, -20, -20, -10},
            {20, 20,  0,  0,  0,  0, 20, 20},
            {20, 30, 10,  0,  0, 10, 30, 20}
    };

    public static final int[][] PAWN_TABLE_BLACK;
    public static final int[][] KNIGHT_TABLE_BLACK;
    public static final int[][] BISHOP_TABLE_BLACK;
    public static final int[][] ROOK_TABLE_BLACK;
    public static final int[][] QUEEN_TABLE_BLACK;
    public static final int[][] KING_TABLE_BLACK;

    static {
        PAWN_TABLE_BLACK = rotateTable(PAWN_TABLE_WHITE);
        KNIGHT_TABLE_BLACK = rotateTable(KNIGHT_TABLE_WHITE);
        BISHOP_TABLE_BLACK = rotateTable(BISHOP_TABLE_WHITE);
        ROOK_TABLE_BLACK = rotateTable(ROOK_TABLE_WHITE);
        QUEEN_TABLE_BLACK = rotateTable(QUEEN_TABLE_WHITE);
        KING_TABLE_BLACK = rotateTable(KING_TABLE_WHITE);
    }

    private static int[][] rotateTable(int[][] table) {
        int size = table.length;
        int[][] rotatedTable = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                rotatedTable[i][j] = table[size - 1 - i][size - 1 - j];
            }
        }
        return rotatedTable;
    }

    public static void printTable(int[][] table){
        for (int i = 0; i < table.length; i++){
            for (int j = 0; j < table[i].length; j++){
                System.out.printf("%3d", table[i][j]);
            }
            System.out.println();
        }
    }
}
