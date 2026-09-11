package com.chess.ui.components.buttons;

import java.util.Deque;
import java.util.Iterator;

import com.chess.ui.GameSession;
import com.chess.ui.view.ChessBoardView;
public class UndoButton extends GameActionButton {

    public UndoButton(GameSession session, ChessBoardView boardView) {
        super("↶", session, boardView);
        setToolTipText("Deshacer jugada");
        setEnabled(false);
    }

    @Override
    public void executeAction() {
        if (!session.canUndo() || session.isAiTurn()) return;

            if (session.undo()) {
                // Reconstruir la posición del tablero a partir del historial restante
                boardView.resetState();
                Deque<String> pastMoves = session.getMoveHistory();
                Iterator<String> it = pastMoves.descendingIterator(); 
                
                while (it.hasNext()) {
                    String move = it.next();
                    boardView.applyMoveNotation(move);
                }
                boardView.render();
                session.notifyListeners();
            }
        
    }

    @Override
    public void updateState() {
        setEnabled(session.canUndo() && !session.isAiTurn());
    }
}