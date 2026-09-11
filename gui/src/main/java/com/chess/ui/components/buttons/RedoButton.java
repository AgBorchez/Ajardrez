package com.chess.ui.components.buttons;

import java.util.List;

import com.chess.ui.GameSession;
import com.chess.ui.view.ChessBoardView;
import com.chess.ui.components.BoardState;

public class RedoButton extends GameActionButton {

    public RedoButton(GameSession session, ChessBoardView boardView, BoardState gameState ) {
        super("↷", session, boardView, gameState);
        setToolTipText("Rehacer jugada");
        setEnabled(false);
    }

    @Override
    public void executeAction() {
        if (!session.canRedo() || session.isAiTurn()) return;

        List<String> Movs = session.redo();
        for (String mov : Movs) { 
        gameState.applyMoveNotation(mov);   
        }        
        boardView.render();
        session.notifyListeners();
    }

    @Override
    public void updateState() {
        setEnabled(session.canRedo() && !session.isAiTurn());
    }

    
}