package com.chess.ui.components.buttons;

import com.chess.ui.GameSession;
import javax.swing.JButton;

public abstract class GameActionButton extends JButton {
    protected final GameSession session;

    public GameActionButton(String label, GameSession session) {
        super(label);
        this.session = session;
        setFocusPainted(false);
        addActionListener(e -> executeAction());
    }

    public abstract void executeAction();
    public abstract void updateState();
}