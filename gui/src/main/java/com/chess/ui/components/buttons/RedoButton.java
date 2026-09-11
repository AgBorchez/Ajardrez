package com.chess.ui.components.buttons;

import java.util.List;

import com.chess.ui.GameSession;
import com.chess.ui.view.ChessBoardView;

public class RedoButton extends GameActionButton {

    public RedoButton(GameSession session, ChessBoardView boardView) {
        super("↷", session, boardView);
        setToolTipText("Rehacer jugada");
        setEnabled(false);
    }

    @Override
    public void executeAction() {
        if (!session.canRedo() || session.isAiTurn()) return;

        List<String> Movs = session.redo();
        for (String mov : Movs) { 
        boardView.applyMoveNotation(mov);   
        }        
        boardView.render();
        session.notifyListeners();
    }

    @Override
    public void updateState() {
        setEnabled(session.canRedo() && !session.isAiTurn());
    }

    
}