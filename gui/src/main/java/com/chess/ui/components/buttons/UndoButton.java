package com.chess.ui.components.buttons;

import com.chess.ui.GameSession;

public class UndoButton extends GameActionButton {

    public UndoButton(GameSession session, Runnable onActionExecuted) {
        super("↶", session, onActionExecuted);
        setToolTipText("Deshacer jugada");
        setEnabled(false);
    }

    @Override
    public void executeAction() {
        if (!session.canUndo() || session.isAiTurn()) return;

        session.undo();
        if (onActionExecuted != null) {
            onActionExecuted.run();
        }
    }

    @Override
    public void updateState() {
        setEnabled(session.canUndo() && !session.isAiTurn());
    }
}