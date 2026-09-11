package com.chess.ui.components.buttons;

import com.chess.ui.GameSession;
import com.chess.ui.components.BoardState;
import com.chess.ui.view.ChessBoardView;

import javax.swing.JButton;
import java.awt.Font;

public abstract class GameActionButton extends JButton {
    protected final GameSession session;
    protected final ChessBoardView boardView;
    protected final BoardState gameState;

    public GameActionButton(String text, GameSession session, ChessBoardView boardView, BoardState gameState) {
        super(text);
        this.session = session;
        this.boardView = boardView;
        this.gameState = gameState;
        session.addGameListener(this::updateState);

        setFont(new Font("SansSerif", Font.BOLD, 20));
        setFocusable(false);
        addActionListener(e -> executeAction());
    }

    public abstract void executeAction();
    public abstract void updateState();

    
}




