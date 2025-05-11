package com.server.externalEval.cuckoochess;

public class BoardConfigImpl_Cuckoo {

    private static double[] zeros = new double[64];

    private double MATERIAL_PAWN_O = 92;
    private double MATERIAL_PAWN_E = 92;

    private double MATERIAL_KNIGHT_O = 385;
    private double MATERIAL_KNIGHT_E = 385;

    private double MATERIAL_BISHOP_O = 385;
    private double MATERIAL_BISHOP_E = 385;

    private double MATERIAL_ROOK_O = 593;
    private double MATERIAL_ROOK_E = 593;

    private double MATERIAL_QUEEN_O = 1244;
    private double MATERIAL_QUEEN_E = 1244;

    private double MATERIAL_KING_O = 9900;
    private double MATERIAL_KING_E = 9900;


    private double MATERIAL_BARIER_NOPAWNS_O	= Math.max(MATERIAL_KNIGHT_O, MATERIAL_BISHOP_O) + MATERIAL_PAWN_O;
    private double MATERIAL_BARIER_NOPAWNS_E	= Math.max(MATERIAL_KNIGHT_E, MATERIAL_BISHOP_E) + MATERIAL_PAWN_E;

    private static final double[] KING_O			= reverseSpecial(new double[] {
            -22,-35,-40,-40,-40,-40,-35,-22,
            -22,-35,-40,-40,-40,-40,-35,-22,
            -25,-35,-40,-45,-45,-40,-35,-25,
            -15,-30,-35,-40,-40,-35,-30,-15,
            -10,-15,-20,-25,-25,-20,-15,-10,
            4, -2, -5,-15,-15, -5, -2,  4,
            16, 14,  7, -3, -3,  7, 14, 16,
            24, 24,  9,  0,  0,  9, 24, 24
    });

    private static final double[] KING_E			= reverseSpecial(new double[] {
            0,  8, 16, 24, 24, 16,  8,  0,
            8, 16, 24, 32, 32, 24, 16,  8,
            16, 24, 32, 40, 40, 32, 24, 16,
            24, 32, 40, 48, 48, 40, 32, 24,
            24, 32, 40, 48, 48, 40, 32, 24,
            16, 24, 32, 40, 40, 32, 24, 16,
            8, 16, 24, 32, 32, 24, 16,  8,
            0,  8, 16, 24, 24, 16,  8,  0
    });

    private static final double[] PAWN_O			= reverseSpecial(new double[] {
            0,  0,  0,  0,  0,  0,  0,  0,
            8, 16, 24, 32, 32, 24, 16,  8,
            3, 12, 20, 28, 28, 20, 12,  3,
            -5,  4, 10, 20, 20, 10,  4, -5,
            -6,  4,  5, 16, 16,  5,  4, -6,
            -6,  4,  2,  5,  5,  2,  4, -6,
            -6,  4,  4,-15,-15,  4,  4, -6,
            0,  0,  0,  0,  0,  0,  0,  0
    });

    private static final double[] PAWN_E			= reverseSpecial(new double[] {
            0,  0,  0,  0,  0,  0,  0,  0,
            25, 40, 45, 45, 45, 45, 40, 25,
            17, 32, 35, 35, 35, 35, 32, 17,
            5, 24, 24, 24, 24, 24, 24,  5,
            -9, 11, 11, 11, 11, 11, 11, -9,
            -17,  3,  3,  3,  3,  3,  3,-17,
            -20,  0,  0,  0,  0,  0,  0,-20,
            0,  0,  0,  0,  0,  0,  0,  0
    });

    public static final double[] KNIGHT_O			= reverseSpecial(new double[] {
            -53,-42,-32,-21,-21,-32,-42,-53,
            -42,-32,-10,  0,  0,-10,-32,-42,
            -21,  5, 10, 16, 16, 10,  5,-21,
            -18,  0, 10, 21, 21, 10,  0,-18,
            -18,  0,  3, 21, 21,  3,  0,-18,
            -21,-10,  0,  0,  0,  0,-10,-21,
            -42,-32,-10,  0,  0,-10,-32,-42,
            -53,-42,-32,-21,-21,-32,-42,-53
    });

    private static final double[] KNIGHT_E			= reverseSpecial(new double[] {
            -56,-44,-34,-22,-22,-34,-44,-56,
            -44,-34,-10,  0,  0,-10,-34,-44,
            -22,  5, 10, 17, 17, 10,  5,-22,
            -19,  0, 10, 22, 22, 10,  0,-19,
            -19,  0,  3, 22, 22,  3,  0,-19,
            -22,-10,  0,  0,  0,  0,-10,-22,
            -44,-34,-10,  0,  0,-10,-34,-44,
            -56,-44,-34,-22,-22,-34,-44,-56
    });

    public static final double[] BISHOP_O			= reverseSpecial(new double[] {
            0,  0,  0,  0,  0,  0,  0,  0,
            0,  4,  2,  2,  2,  2,  4,  0,
            0,  2,  4,  4,  4,  4,  2,  0,
            0,  2,  4,  4,  4,  4,  2,  0,
            0,  2,  4,  4,  4,  4,  2,  0,
            0,  3,  4,  4,  4,  4,  3,  0,
            0,  4,  2,  2,  2,  2,  4,  0,
            -5, -5, -7, -5, -5, -7, -5, -5
    });

    private static final double[] BISHOP_E			= reverseSpecial(new double[] {
            0,  0,  0,  0,  0,  0,  0,  0,
            0,  2,  2,  2,  2,  2,  2,  0,
            0,  2,  4,  4,  4,  4,  2,  0,
            0,  2,  4,  4,  4,  4,  2,  0,
            0,  2,  4,  4,  4,  4,  2,  0,
            0,  2,  4,  4,  4,  4,  2,  0,
            0,  2,  2,  2,  2,  2,  2,  0,
            0,  0,  0,  0,  0,  0,  0,  0
    });

    private static final double[] ROOK_O			= reverseSpecial(new double[] {
            0,  3,  5,  5,  5,  5,  3,  0,
            15, 20, 20, 20, 20, 20, 20, 15,
            0,  0,  0,  0,  0,  0,  0,  0,
            0,  0,  0,  0,  0,  0,  0,  0,
            -2,  0,  0,  0,  0,  0,  0, -2,
            -2,  0,  0,  2,  2,  0,  0, -2,
            -3,  2,  5,  5,  5,  5,  2, -3,
            0,  3,  5,  5,  5,  5,  3,  0
    });

    private static final double[] ROOK_E			= zeros;

    private static final double[] QUEEN_O			= reverseSpecial(new double[] {
            -10, -5,  0,  0,  0,  0, -5,-10,
            -5,  0,  5,  5,  5,  5,  0, -5,
            0,  5,  5,  6,  6,  5,  5,  0,
            0,  5,  6,  6,  6,  6,  5,  0,
            0,  5,  6,  6,  6,  6,  5,  0,
            0,  5,  5,  6,  6,  5,  5,  0,
            -5,  0,  5,  5,  5,  5,  0, -5,
            -10, -5,  0,  0,  0,  0, -5,-10
    });

    private static final double[] QUEEN_E			= zeros;


    public boolean getFieldsStatesSupport() {
        return false;
    }


    public double[] getPST_PAWN_O() {
        return PAWN_O;
    }

    public double[] getPST_PAWN_E() {
        return PAWN_E;
    }

    public double[] getPST_KING_O() {
        return KING_O;
    }

    public double[] getPST_KING_E() {
        return KING_E;
    }

    public double[] getPST_KNIGHT_O() {
        return KNIGHT_O;
    }

    public double[] getPST_KNIGHT_E() {
        return KNIGHT_E;
    }

    public double[] getPST_BISHOP_O() {
        return BISHOP_O;
    }

    public double[] getPST_BISHOP_E() {
        return BISHOP_E;
    }

    public double[] getPST_ROOK_O() {
        return ROOK_O;
    }

    public double[] getPST_ROOK_E() {
        return ROOK_E;
    }

    public double[] getPST_QUEEN_O() {
        return QUEEN_O;
    }

    public double[] getPST_QUEEN_E() {
        return QUEEN_E;
    }


    public double getMaterial_PAWN_O() {
        return MATERIAL_PAWN_O;
    }

    public double getMaterial_PAWN_E() {
        return MATERIAL_PAWN_E;
    }

    public double getMaterial_KING_O() {
        return MATERIAL_KING_O;
    }

    public double getMaterial_KING_E() {
        return MATERIAL_KING_E;
    }

    public double getMaterial_KNIGHT_O() {
        return MATERIAL_KNIGHT_O;
    }

    public double getMaterial_KNIGHT_E() {
        return MATERIAL_KNIGHT_E;
    }

    public double getMaterial_BISHOP_O() {
        return MATERIAL_BISHOP_O;
    }

    public double getMaterial_BISHOP_E() {
        return MATERIAL_BISHOP_E;
    }

    public double getMaterial_ROOK_O() {
        return MATERIAL_ROOK_O;
    }

    public double getMaterial_ROOK_E() {
        return MATERIAL_ROOK_E;
    }

    public double getMaterial_QUEEN_O() {
        return MATERIAL_QUEEN_O;
    }

    public double getMaterial_QUEEN_E() {
        return MATERIAL_QUEEN_E;
    }

    public double getMaterial_BARIER_NOPAWNS_O() {
        return MATERIAL_BARIER_NOPAWNS_O;
    }

    public double getMaterial_BARIER_NOPAWNS_E() {
        return MATERIAL_BARIER_NOPAWNS_E;
    }

    public double getWeight_PST_PAWN_O() {
        return 1;
    }

    public double getWeight_PST_PAWN_E() {
        return 1;
    }

    public double getWeight_PST_KING_O() {
        return 1;
    }

    public double getWeight_PST_KING_E() {
        return 1;
    }

    public double getWeight_PST_KNIGHT_O() {
        return 1;
    }

    public double getWeight_PST_KNIGHT_E() {
        return 1;
    }

    public double getWeight_PST_BISHOP_O() {
        return 1;
    }

    public double getWeight_PST_BISHOP_E() {
        return 1;
    }

    public double getWeight_PST_ROOK_O() {
        return 1;
    }

    public double getWeight_PST_ROOK_E() {
        return 1;
    }

    public double getWeight_PST_QUEEN_O() {
        return 1;
    }

    public double getWeight_PST_QUEEN_E() {
        return 1;
    }

    private static double[] reverseSpecial(double[] arr) {
        if (arr.length != 64) {
            throw new IllegalStateException();
        }
        reverse(arr, 0, arr.length);

        reverse(arr, 0, 8);
        reverse(arr, 8, 16);
        reverse(arr, 16, 24);
        reverse(arr, 24, 32);
        reverse(arr, 32, 40);
        reverse(arr, 40, 48);
        reverse(arr, 48, 56);
        reverse(arr, 56, 64);

        return arr;
    }

    private static double[] reverse(double[] arr, int from, int to) {
        to--;
        while (from < to) {
            double f = arr[from];
            double t = arr[to];
            arr[from] = t;
            arr[to] = f;
            from++;
            to--;
        }
        return arr;
    }
}
