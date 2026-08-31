package com.chess.ui.components.buttons;

import com.chess.ui.GameSession;
import java.io.IOException;

public class UndoButton extends GameActionButton {

    public UndoButton(GameSession session) {
        super("Deshacer", session);
        setEnabled(false);
    }

    @Override
    public void executeAction() {
        if (!canUndo()) return;

        try {
            // En partida contra IA, siempre desarmamos el par (jugada del jugador + respuesta de la IA)
            int stepsToUndo = session.playerPlaysWhite && session.history.getMoveHistory().size() == 1 ? 1 : 2;

            for (int i = 0; i < stepsToUndo; i++) {
                boolean engineOk = session.bridge.undoMove();
                if (engineOk) {
                    session.history.undo();
                }
            }

            // Reconstruir la vista gráfica
            session.boardView.resetState();
            session.boardView.clearHighlights();
            for (String move : session.history.getMovesChronological()) {
                session.boardView.applyMoveNotation(move);
            }
            session.boardView.render();

            session.onStateChanged.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateState() {
        setEnabled(canUndo());
    }

    private boolean canUndo() {
        
        if (session.isAiTurn || session.history.getMoveHistory().isEmpty()) {
            return false;
        }

        // Si el usuario juega con Negras, necesita al menos 2 jugadas en el historial (1 de IA + 1 propia)
        if (!session.playerPlaysWhite && session.history.getMoveHistory().size() < 2) {
            return false;
        }

        return true;

    }
}