package com.chess.ui.components.buttons;

import com.chess.ui.GameSession;
import com.chess.ui.view.ChessBoardView;

import javax.swing.JButton;
import java.awt.Font;

public abstract class GameActionButton extends JButton {
    protected final GameSession session;
    protected final ChessBoardView boardView;

    public GameActionButton(String text, GameSession session, ChessBoardView boardView) {
        super(text);
        this.session = session;
        this.boardView = boardView;
        session.addGameListener(this::updateState);

        setFont(new Font("SansSerif", Font.BOLD, 20));
        setFocusable(false);
        addActionListener(e -> executeAction());
    }

    public abstract void executeAction();
    public abstract void updateState();

    
}




