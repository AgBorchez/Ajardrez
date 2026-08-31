package com.chess.ui;

import com.chess.engine.EngineBridge;
import com.chess.storage.GameHistory;
import com.chess.ui.components.ChessBoardView;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class GameSession {
    public final EngineBridge bridge;
    public final GameHistory history = new GameHistory();
    public final ChessBoardView boardView;
    public final Runnable onStateChanged;

    public Point selectedSquare = null;
    public final Set<String> currentLegalMoves = new HashSet<>();
    public boolean isAiTurn = false;
    public boolean playerPlaysWhite = true;

    public GameSession(EngineBridge bridge, ChessBoardView boardView, Runnable onStateChanged) {
        this.bridge = bridge;
        this.boardView = boardView;
        this.onStateChanged = onStateChanged;
    }
}