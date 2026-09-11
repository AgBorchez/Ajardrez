package com.chess.ui.util;

public final class NotationUtils {

    private NotationUtils() {}

    public static String toNotation(int row, int col) {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }

    public static int toRow(String sq) {
        return 8 - Character.getNumericValue(sq.charAt(1));
    }

    public static int toCol(String sq) {
        return sq.charAt(0) - 'a';
    }
}