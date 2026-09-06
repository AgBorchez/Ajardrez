package com.chess.ui.components;

import com.chess.ui.util.PieceImages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ChessBoardView extends JPanel {

    private final JButton[][] buttons = new JButton[8][8];
    private final char[][] boardState = new char[8][8];
    private final Set<String> highlightedSquares = new HashSet<>();

    private final Consumer<String> onSquareSelected; // Notifica "e2" para pedir jugadas legales
    private final Consumer<String> onMoveAttempted;  // Notifica "e2e4" para ejecutar la jugada

    private Point selectedPoint = null;
    private boolean flipped = false;

    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_COLOR = new Color(186, 202, 68);
    private static final Color MOVE_HINT_COLOR = new Color(130, 151, 105);

    public ChessBoardView(Consumer<String> onSquareSelected, Consumer<String> onMoveAttempted) {
        this.onSquareSelected = onSquareSelected;
        this.onMoveAttempted = onMoveAttempted;

        setLayout(new GridLayout(8, 8));

        setPreferredSize(new Dimension(560, 560));
        initButtons();
        resetState();
    }

    private void initButtons() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton btn = new JButton();
                btn.setFont(new Font("SansSerif", Font.BOLD, 36));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);

                final int row = r;
                final int col = c;
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        int logicalRow = flipped ? (7 - row) : row;
                        int logicalCol = flipped ? (7 - col) : col;
                        handleSquareClick(logicalRow, logicalCol);
                    }
                });

                buttons[r][c] = btn;
                add(btn);
            }
        }
    }

    private void handleSquareClick(int r, int c) {
        String clickedSq = toNotation(r, c);

        // 1. Si ya había una casilla seleccionada y se clickea una sugerencia válida
        if (selectedPoint != null && highlightedSquares.contains(clickedSq)) {
            String fromSq = toNotation(selectedPoint.y, selectedPoint.x);
            char movingPiece = boardState[selectedPoint.y][selectedPoint.x];
            String move = fromSq + clickedSq;

            if ((movingPiece == 'P' && r == 0) || (movingPiece == 'p' && r == 7)) {
                move += promptPromotion();
            }

            clearSelection();
            render();
            onMoveAttempted.accept(move); // Notifica la jugada completa a GameSession
            return;
        }

        // 2. Si no es un destino legal, se evalúa seleccionar la casilla tocada
        char piece = boardState[r][c];
        if (piece != ' ') {
            selectedPoint = new Point(c, r);
            render();
            onSquareSelected.accept(clickedSq); // Pide a GameSession las jugadas legales
        } else {
            clearSelection();
            render();
        }
    }

    private String promptPromotion() {
        String[] options = {"Reina", "Torre", "Alfil", "Caballo"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Elegí la pieza para coronar:",
                "Coronación",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        return switch (choice) {
            case 1 -> "r";
            case 2 -> "b";
            case 3 -> "n";
            default -> "q";
        };
    }

    public void setLegalMoveTargets(Set<String> targets) {
        this.highlightedSquares.clear();
        if (targets != null) {
            this.highlightedSquares.addAll(targets);
        }
        render();
    }

    public void clearSelection() {
        this.selectedPoint = null;
        this.highlightedSquares.clear();
    }

    public void resetState() {
        char[] backRankBlack = {'r','n','b','q','k','b','n','r'};
        char[] backRankWhite = {'R','N','B','Q','K','B','N','R'};

        for (int c = 0; c < 8; c++) {
            boardState[0][c] = backRankBlack[c];
            boardState[1][c] = 'p';
            for (int r = 2; r < 6; r++) boardState[r][c] = ' ';
            boardState[6][c] = 'P';
            boardState[7][c] = backRankWhite[c];
        }
        clearSelection();
        render();
    }

    public void render() {
        int targetSize = 58;

        for (int visualRow = 0; visualRow < 8; visualRow++) {
            for (int visualCol = 0; visualCol < 8; visualCol++) {
                int logicalRow = flipped ? (7 - visualRow) : visualRow;
                int logicalCol = flipped ? (7 - visualCol) : visualCol;

                char piece = boardState[logicalRow][logicalCol];
                JButton btn = buttons[visualRow][visualCol];

                btn.setText("");
                btn.setIcon(PieceImages.getIcon(piece, targetSize));

                Color baseColor = (logicalRow + logicalCol) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                btn.setBackground(baseColor);

                if (selectedPoint != null && selectedPoint.y == logicalRow && selectedPoint.x == logicalCol) {
                    btn.setBackground(SELECTED_COLOR);
                }

                String sq = toNotation(logicalRow, logicalCol);
                if (highlightedSquares.contains(sq)) {
                    btn.setBackground(MOVE_HINT_COLOR);
                }
            }
        }
    }

    public void applyMoveNotation(String moveStr) {
        if (moveStr == null || moveStr.length() < 4) return;

        int fromCol = moveStr.charAt(0) - 'a';
        int fromRow = 8 - Character.getNumericValue(moveStr.charAt(1));
        int toCol = moveStr.charAt(2) - 'a';
        int toRow = 8 - Character.getNumericValue(moveStr.charAt(3));

        char movingPiece = boardState[fromRow][fromCol];

        if (moveStr.length() == 5) {
            char promo = moveStr.charAt(4);
            movingPiece = Character.isUpperCase(movingPiece) ? Character.toUpperCase(promo) : Character.toLowerCase(promo);
        }

        if (Character.toUpperCase(movingPiece) == 'K' && Math.abs(toCol - fromCol) == 2) {
            if (toCol == 6) {
                boardState[fromRow][5] = boardState[fromRow][7];
                boardState[fromRow][7] = ' ';
            } else if (toCol == 2) {
                boardState[fromRow][3] = boardState[fromRow][0];
                boardState[fromRow][0] = ' ';
            }
        }

        boardState[toRow][toCol] = movingPiece;
        boardState[fromRow][fromCol] = ' ';
    }

    public static String toNotation(int row, int col) {
        return "" + (char)('a' + col) + (8 - row);
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }
}