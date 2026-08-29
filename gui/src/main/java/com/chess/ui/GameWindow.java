package com.chess.ui;

import com.chess.engine.EngineBridge;
import com.chess.storage.GameHistory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GameWindow extends JFrame {

    private final EngineBridge bridge;
    private final GameHistory history = new GameHistory();
    private final ChessBoardView boardView;

    private Point selectedSquare = null;
    private final Set<String> currentLegalMoves = new HashSet<>();
    private boolean isAiTurn = false;

    public GameWindow(EngineBridge bridge) {
        this.bridge = bridge;
        setTitle("Chess Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.boardView = new ChessBoardView(this::onSquareClicked);
        add(boardView, BorderLayout.CENTER);
        add(createToolbar(), BorderLayout.SOUTH);
    }

    private JPanel createToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnNew = new JButton("Nueva Partida");
        btnNew.addActionListener(e -> resetGame());

        btnUndo = new JButton("Deshacer");
        btnUndo.setEnabled(false); 
        btnUndo.addActionListener(e -> handleUndo());

        bar.add(btnNew);
        bar.add(btnUndo);
        return bar;
    }

    private String toNotation(int r, int c) {
        return "" + (char)('a' + c) + (8 - r);
    }

    private Point fromNotation(String sq) {
        int c = sq.charAt(0) - 'a';
        int r = 8 - Character.getNumericValue(sq.charAt(1));
        return new Point(c, r);
    }

    private void onSquareClicked(int r, int c) {
        if (isAiTurn) return;

        String clickedSq = toNotation(r, c);

        if (selectedSquare != null) {
            String fromSq = toNotation(selectedSquare.y, selectedSquare.x);
            String move = fromSq + clickedSq;

            if (currentLegalMoves.contains(move)) {
                char piece = boardView.getPieceAt(selectedSquare.y, selectedSquare.x);
                if (piece == 'P' && r == 0) {
                    move += promptPromotionPiece();
                }
                executeMove(move, selectedSquare, new Point(c, r), true);
                selectedSquare = null;
                boardView.clearHighlights();
                boardView.render();
                return;
            }
        }

        char piece = boardView.getPieceAt(r, c);
        if (piece != ' ' && Character.isUpperCase(piece)) {
            selectedSquare = new Point(c, r);
            boardView.setSelectedSquare(selectedSquare);
            try {
                List<String> moves = bridge.getLegalMoves(clickedSq);
                currentLegalMoves.clear();
                currentLegalMoves.addAll(moves);

                Set<String> targetSquares = new HashSet<>();
                for (String m : moves) {
                    targetSquares.add(m.substring(2, 4));
                }
                boardView.setHighlightedSquares(targetSquares);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            selectedSquare = null;
            boardView.clearHighlights();
        }
        boardView.render();
    }

    private void executeMove(String moveStr, Point from, Point to, boolean isPlayer) {
        try {
            bridge.makeMove(moveStr);
            history.recordMove(moveStr);

            char movingPiece = boardView.getPieceAt(from.y, from.x);
            if (moveStr.length() == 5) {
                char promoChar = moveStr.charAt(4);
                movingPiece = isPlayer ? Character.toUpperCase(promoChar) : Character.toLowerCase(promoChar);
            }

            boardView.setPieceAt(to.y, to.x, movingPiece);
            boardView.setPieceAt(from.y, from.x, ' ');
            boardView.render();

            updateButtonStates();
            checkGameEnd();

            if (isPlayer) {
                isAiTurn = true;
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return bridge.calculateAiMove();
                    } catch (IOException ex) {
                        return null;
                    }
                }).thenAccept(aiMove -> SwingUtilities.invokeLater(() -> {
                    isAiTurn = false;
                    if (aiMove != null && aiMove.length() >= 4) {
                        Point aiFrom = fromNotation(aiMove.substring(0, 2));
                        Point aiTo = fromNotation(aiMove.substring(2, 4));
                        executeMove(aiMove, aiFrom, aiTo, false);
                    }
                }));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String promptPromotionPiece() {
        Object[] options = {"Dama (♕)", "Torre (♖)", "Alfil (♗)", "Caballo (♘)"};
        int choice = JOptionPane.showOptionDialog(
            this, "Coronar a:", "Promoción",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]
        );
        switch (choice) {
            case 1: return "r";
            case 2: return "b";
            case 3: return "n";
            default: return "q";
        }
    }

    private void checkGameEnd() {
        try {
            String status = bridge.getGameStatus();
            if ("CHECKMATE".equals(status)) {
                String msg = isAiTurn ? "¡Jaque Mate! La IA ha ganado." : "¡Ganaste por Jaque Mate!";
                JOptionPane.showMessageDialog(this, msg, "Fin de Partida", JOptionPane.INFORMATION_MESSAGE);
            } else if ("STALEMATE".equals(status)) {
                JOptionPane.showMessageDialog(this, "Tablas por Rey Ahogado.", "Fin", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetGame() {
        try {
            bridge.newGame();
            history.clear();
            boardView.resetState();
            selectedSquare = null;
            currentLegalMoves.clear();
            isAiTurn = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUndo(){

        if (isAiTurn || history.getMoveHistory().isEmpty()) return;

        try {
            int stepsToUndo = (history.getMoveHistory().size() >= 2) ? 2 : 1;

            for (int i = 0; i < stepsToUndo; i++){

                boolean engineOk = bridge.undoMove();

                if (engineOk) {
                    history.undo();
                }
            }

            boardView.render();

            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JButton btnUndo;

    

    private void updateButtonStates() {
        if (btnUndo != null) {
            btnUndo.setEnabled(!history.getMoveHistory().isEmpty() && !isAiTurn);
        }
    }
}