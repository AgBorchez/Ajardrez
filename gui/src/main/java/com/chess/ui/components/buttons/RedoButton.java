package com.chess.ui.components.buttons;

import com.chess.ui.GameSession;

public class RedoButton extends GameActionButton {

    public RedoButton(GameSession session, Runnable onActionExecuted) {
        super("↷", session, onActionExecuted);
        setToolTipText("Rehacer jugada");
        setEnabled(false);
    }

    @Override
    public void executeAction() {
        if (!session.canRedo() || session.isAiTurn()) return;

        session.redo();
        if (onActionExecuted != null) {
            onActionExecuted.run();
        }
    }

    @Override
    public void updateState() {
        setEnabled(session.canRedo() && !session.isAiTurn());
    }
}