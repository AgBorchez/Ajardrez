package com.chess.ui;

import com.chess.engine.EngineBridge;
import com.chess.ui.components.ChessBoardView;
import com.chess.ui.components.buttons.GameActionButton;
import com.chess.ui.components.buttons.NewGameButton;
import com.chess.ui.components.buttons.UndoButton;
import com.chess.ui.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameWindow extends JFrame {

    private final GameSession session;
    private final ChessBoardView boardView;
    private final List<GameActionButton> actionButtons = new ArrayList<>();

    public GameWindow(EngineBridge bridge) {
        setTitle("Chess Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.boardView = new ChessBoardView(this::onSquareClicked);
        this.session = new GameSession(bridge, boardView, this::updateUIState);

        add(boardView, BorderLayout.CENTER);
        add(createToolbar(), BorderLayout.SOUTH);

        updateUIState();
    }

    private JPanel createToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        NewGameButton btnNew = new NewGameButton(session);
        UndoButton btnUndo = new UndoButton(session);

        actionButtons.add(btnNew);
        actionButtons.add(btnUndo);

        bar.add(btnNew);
        bar.add(btnUndo);
        btnNew.executeAction();
        return bar;
    }

    private void updateUIState() {
        for (GameActionButton btn : actionButtons) {
            btn.updateState();
        }
    }

    private void onSquareClicked(int r, int c) {
        if (session.isAiTurn) return;

        char piece = boardView.getPieceAt(r, c);
        boolean isOwnPiece = session.playerPlaysWhite ? Character.isUpperCase(piece) : Character.isLowerCase(piece);
        String clickedSq = "" + (char)('a' + c) + (8 - r);

        Logger.info("UI", "Casilla seleccionada: " + clickedSq);

        if (session.selectedSquare != null) {
            String fromSq = "" + (char)('a' + session.selectedSquare.x) + (8 - session.selectedSquare.y);
            String move = fromSq + clickedSq;
            Logger.info("UI", "Ejecutando jugada: " + move);

            if (session.currentLegalMoves.contains(move)) {
                if (piece == 'P' && r == 0) {
                    move += promptPromotion();
                }
                executePlayerMove(move);
                session.selectedSquare = null;
                boardView.clearHighlights();
                boardView.render();
                return;
            }
        }

        if (piece != ' ' && isOwnPiece) {
            session.selectedSquare = new Point(c, r);
            boardView.setSelectedSquare(session.selectedSquare);
            try {
                List<String> moves = session.bridge.getLegalMoves(clickedSq);
                session.currentLegalMoves.clear();
                session.currentLegalMoves.addAll(moves);

                java.util.Set<String> targets = new java.util.HashSet<>();
                for (String m : moves) targets.add(m.substring(2, 4));
                boardView.setHighlightedSquares(targets);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            session.selectedSquare = null;
            boardView.clearHighlights();
        }
        boardView.render();
    }

    private void executePlayerMove(String moveStr) {
        try {
            session.bridge.makeMove(moveStr);
            session.history.recordMove(moveStr);
            boardView.applyMoveNotation(moveStr);
            boardView.render();

            session.isAiTurn = true;
            updateUIState();

            CompletableFuture.supplyAsync(() -> {
                try {
                    return session.bridge.calculateAiMove();
                } catch (IOException ex) {
                    return null;
                }
            }).thenAccept(aiMove -> SwingUtilities.invokeLater(() -> {
                session.isAiTurn = false;
                if (aiMove != null && aiMove.length() >= 4) {
                    try {
                        session.bridge.makeMove(aiMove);
                        session.history.recordMove(aiMove);
                        boardView.applyMoveNotation(aiMove);
                        boardView.render();
                    } catch (IOException ignored) {}
                }
                updateUIState();
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String promptPromotion() {
        Object[] options = {"Dama (♕)", "Torre (♖)", "Alfil (♗)", "Caballo (♘)"};
        int choice = JOptionPane.showOptionDialog(
            this, "Coronar a:", "Promoción",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]
        );
        return switch (choice) {
            case 1 -> "r";
            case 2 -> "b";
            case 3 -> "n";
            default -> "q";
        };
    }
}