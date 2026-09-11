package com.chess.ui;

import com.chess.engine.EngineBridge;
import com.chess.storage.GameHistory;
import com.chess.ui.util.GameStateListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.ArrayList;

public class GameSession {

    private final EngineBridge bridge;
    private final GameHistory history = new GameHistory();
    protected final List<GameStateListener> listeners = new ArrayList<>();
    private boolean isAiTurn;
    private boolean playerPlaysWhite;

    public GameSession(EngineBridge bridge) {
        this.bridge = bridge;
    }

    public void addGameListener(GameStateListener listener){
        this.listeners.add(listener);
    }

    public void notifyListeners(){
        for (GameStateListener listener : listeners) {
            listener.onGameStateChanged();            
        }
    }

    public void startNewGame(boolean playsWhite) {
        try {
            bridge.newGame();
            history.clear();
            this.playerPlaysWhite = playsWhite;
            this.isAiTurn = !playsWhite;
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifyListeners();
    }

    // Consulta movimientos legales al motor en C++
    public Set<String> getLegalTargets(String sq) {
        if (isAiTurn) return Collections.emptySet();

        try {
            List<String> moves = bridge.getLegalMoves(sq);
            Set<String> targets = new HashSet<>();
            for (String m : moves) {
                targets.add(m.substring(2, 4));
            }

            notifyListeners();
            return targets;

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
        
    }

    // Aplica la jugada del jugador en C++ y en el historial
    public boolean playPlayerMove(String uciMove) {
        if (isAiTurn) return false;

        try {
            bridge.makeMove(uciMove);
            history.recordMove(uciMove);
            notifyListeners();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            notifyListeners();
            return false;
        }
    }

    // Calcula la respuesta de la IA en background y notifica el resultado por callback
    public void triggerAiMoveAsync(Consumer<String> onAiMoveCalculated) {
        this.isAiTurn = true;

        CompletableFuture.supplyAsync(() -> {
            try {
                return bridge.calculateAiMove();
            } catch (IOException e) {
                return null;
            }
        }).thenAccept(aiMove -> {
            this.isAiTurn = false;
            if (aiMove != null && aiMove.length() >= 4) {
                try {
                    bridge.makeMove(aiMove);
                    history.recordMove(aiMove);
                    notifyListeners();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            onAiMoveCalculated.accept(aiMove);
        });
    }

    // Deshace 2 jugadas en C++ e historial
    public boolean undo() {
        if (isAiTurn || !canUndo()) return false;

        try {
            for (int i = 0; i < 2; i++) {
                String move = history.undo();
                if (move != null) {
                    bridge.undoMove();
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Rehace 2 jugadas en C++
    public List<String> redo() {
        if (isAiTurn || !canRedo()) return Collections.emptyList();

        List<String> redoneMoves = new java.util.ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                String move = history.redo();
                if (move != null) {
                    bridge.makeMove(move);
                    redoneMoves.add(move);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return redoneMoves;
    }

    public Deque<String> getMoveHistory() {
        return history.getMoveHistory();
    }

    public boolean canUndo() {
        
        if (isAiTurn || history.getMoveHistory().isEmpty()) {
            return false;
        }

        // Si el usuario juega con Negras, necesita al menos 2 jugadas en el historial (1 de IA + 1 propia)
        if (!playerPlaysWhite && history.getMoveHistory().size() < 2) {
            return false;
        }

        return true;

    }

    public boolean canRedo() {
           
        if (history.getUndoHisory().isEmpty()) {
            return false;
        }

        return true;

    }

    public boolean isAiTurn() {
        return isAiTurn;
    }

    public boolean isPlayerPlaysWhite() {
        return playerPlaysWhite;
    }
}