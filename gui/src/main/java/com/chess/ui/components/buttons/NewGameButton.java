package com.chess.ui.components.buttons;

import com.chess.ui.GameSession;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

public class NewGameButton extends GameActionButton {

    public NewGameButton(GameSession session, Runnable onActionExecuted) {
        super("Nueva Partida", session, onActionExecuted);
    }

    @Override
    public void executeAction() {
        if (session.isAiTurn()) return;

        Component parent = SwingUtilities.getWindowAncestor(this);
        boolean playsWhite = promptPlayerColor(parent);

        session.startNewGame(playsWhite);

        if (onActionExecuted != null) {
            onActionExecuted.run();
        }
    }

    @Override
    public void updateState() {
        setEnabled(!session.isAiTurn());
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