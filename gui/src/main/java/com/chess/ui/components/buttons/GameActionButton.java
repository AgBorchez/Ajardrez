package com.chess.ui.components.buttons;

import com.chess.ui.GameSession;

import javax.swing.JButton;
import java.awt.Font;

public abstract class GameActionButton extends JButton {
    protected final GameSession session;
    protected final Runnable onActionExecuted;

    public GameActionButton(String text, GameSession session, Runnable onActionExecuted) {
        super(text);
        this.session = session;
        this.onActionExecuted = onActionExecuted;

        setFont(new Font("SansSerif", Font.BOLD, 20));
        setFocusable(false);
        addActionListener(e -> executeAction());
    }

    public abstract void executeAction();
    public abstract void updateState();
}