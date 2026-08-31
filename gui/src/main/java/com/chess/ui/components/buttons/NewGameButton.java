package com.chess.ui.components.buttons;

import com.chess.ui.GameSession;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class NewGameButton extends GameActionButton {

    public NewGameButton(GameSession session) {
        super("Nueva Partida", session);
    }

    @Override
    public void executeAction() {
        Component parent = SwingUtilities.getWindowAncestor(this);
        boolean playsWhite = promptPlayerColor(parent);

        try {
            session.bridge.newGame();
            session.history.clear();
            session.boardView.resetState();
            session.selectedSquare = null;
            session.currentLegalMoves.clear();
            session.playerPlaysWhite = playsWhite;
            session.boardView.setFlipped(!playsWhite);

            if (!playsWhite) {
                session.isAiTurn = true;
                session.onStateChanged.run();

                CompletableFuture.supplyAsync(() -> {
                    try {
                        return session.bridge.calculateAiMove();
                    } catch (IOException ex) {
                        return null;
                    }
                }).thenAccept(aiMove -> SwingUtilities.invokeLater(() -> {
                    session.isAiTurn = false;
                    if (aiMove != null && aiMove.length() >= 4) {
                        applyAiMove(aiMove);
                    }
                    session.onStateChanged.run();
                }));
            } else {
                session.isAiTurn = false;
                session.onStateChanged.run();
            }

            session.boardView.render();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyAiMove(String moveStr) {
        try {
            session.bridge.makeMove(moveStr);
            session.history.recordMove(moveStr);
            session.boardView.applyMoveNotation(moveStr);
            session.boardView.render();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateState() {
        setEnabled(!session.isAiTurn);
    }

    private boolean promptPlayerColor(Component parent) {
        Object[] options = {"Blancas (Juegas primero)", "Negras (Juega la IA)"};
        int choice = JOptionPane.showOptionDialog(
            parent,
            "Selecciona con qué bando deseas jugar:",
            "Nueva Partida",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        return choice != 1;
    }
}