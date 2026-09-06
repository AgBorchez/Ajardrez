package com.chess.storage;

import java.util.ArrayDeque;
import java.util.Deque;

public class GameHistory {
    private final Deque<String> moveHistory = new ArrayDeque<>();
    private final Deque<String> undoHistory = new ArrayDeque<>();

    public void recordMove(String move) {
        moveHistory.push(move);
        undoHistory.clear(); // Nuevo movimiento invalida el árbol de movimientos previos
    }

    public String undo() {
        if (moveHistory.isEmpty()) return null;
        String move = moveHistory.pop();
        undoHistory.push(move);
        return move;
    }

    public String redo() {
        if (undoHistory.isEmpty()) return null;
        String move = undoHistory.pop();
        moveHistory.push(move);
        return move;
    }

    public Deque<String> getMoveHistory() {
        return moveHistory;
    }

    public Deque<String> getUndoHisory(){
        return undoHistory;
    } 

    public void clear() {
        moveHistory.clear();
        undoHistory.clear();
    }

    public java.util.List<String> getMovesChronological() {
        java.util.List<String> list = new java.util.ArrayList<>();
        java.util.Iterator<String> it = moveHistory.descendingIterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    

}