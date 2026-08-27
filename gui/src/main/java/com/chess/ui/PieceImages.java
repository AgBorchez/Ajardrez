package com.chess.ui;

import java.util.HashMap;
import java.util.Map;

public class PieceImages {
    private static final Map<Character, String> UNICODE_PIECES = new HashMap<>();

    static {
        UNICODE_PIECES.put('P', "♙");
        UNICODE_PIECES.put('R', "♖");
        UNICODE_PIECES.put('N', "♘");
        UNICODE_PIECES.put('B', "♗");
        UNICODE_PIECES.put('Q', "♕");
        UNICODE_PIECES.put('K', "♔");
        UNICODE_PIECES.put('p', "♟");
        UNICODE_PIECES.put('r', "♜");
        UNICODE_PIECES.put('n', "♞");
        UNICODE_PIECES.put('b', "♝");
        UNICODE_PIECES.put('q', "♛");
        UNICODE_PIECES.put('k', "♚");
    }

    public static String getSymbol(char piece) {
        return UNICODE_PIECES.getOrDefault(piece, "");
    }
}
    
