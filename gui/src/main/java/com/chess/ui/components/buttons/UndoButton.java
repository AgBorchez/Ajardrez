package com.chess.ui.components.buttons;

import java.util.Deque;
import java.util.Iterator;

import com.chess.ui.GameSession;
import com.chess.ui.components.BoardState;
import com.chess.ui.view.ChessBoardView;
public class UndoButton extends GameActionButton {

    public UndoButton(GameSession session, ChessBoardView boardView, BoardState gameState) {
        super("↶", session, boardView, gameState);
        setToolTipText("Deshacer jugada");
        setEnabled(false);
    }

    @Override
    public void executeAction() {
        if (!session.canUndo() || session.isAiTurn()) return;

            if (session.undo()) {
                // Reconstruir la posición del tablero a partir del historial restante
                gameState.resetState();
                Deque<String> pastMoves = session.getMoveHistory();
                Iterator<String> it = pastMoves.descendingIterator(); 
                
                while (it.hasNext()) {
                    String move = it.next();
                    gameState.applyMoveNotation(move);
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