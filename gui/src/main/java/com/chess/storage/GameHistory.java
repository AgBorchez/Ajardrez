package com.chess.storage;

import java.util.ArrayDeque;
import java.util.Deque;

public class GameHistory {
    private final Deque<String> moveHistory = new ArrayDeque<>();
    private final Deque<String> redoHistory = new ArrayDeque<>();

    public void recordMove(String move) {
        moveHistory.push(move);
        redoHistory.clear(); // Nuevo movimiento invalida el árbol de redo
    }

    public String undo() {
        if (moveHistory.isEmpty()) return null;
        String move = moveHistory.pop();
        redoHistory.push(move);
        return move;
    }

    public String redo() {
        if (redoHistory.isEmpty()) return null;
        String move = redoHistory.pop();
        moveHistory.push(move);
        return move;
    }

    public Deque<String> getMoveHistory() {
        return moveHistory;
    }

    public void clear() {
        moveHistory.clear();
        redoHistory.clear();
    }

}