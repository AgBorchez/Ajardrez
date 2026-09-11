package com.chess.ui.theme;

import java.util.Map;

public record BoardTheme(
    String themeName,
    BoardPaths board,
    OverlayPaths overlays,
    Map<String, String> pieces
) {
    public record BoardPaths(String lightSquare, String darkSquare) {}
    public record OverlayPaths(String selected, String validMove, String castling, String check) {}
}
