package com.chess.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import com.chess.ui.util.PieceImages;

public class ChessBoardView extends JPanel {

    private final JButton[][] buttons = new JButton[8][8];
    private final char[][] boardState = new char[8][8];
    private final Set<String> highlightedSquares = new HashSet<>();
    private Point selectedPoint = null;
    private boolean flipped = false;

    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_COLOR = new Color(186, 202, 68);
    private static final Color MOVE_HINT_COLOR = new Color(130, 151, 105);

    public ChessBoardView(BiConsumer<Integer, Integer> onSquareClicked) {
        setLayout(new GridLayout(8, 8));
        initButtons(onSquareClicked);
        resetState();
    }

    private void initButtons(BiConsumer<Integer, Integer> onSquareClicked) {
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
                        // Traducir la posición visual de la pantalla a la coordenada lógica (0 a 7)
                        int logicalRow = flipped ? (7 - row) : row;
                        int logicalCol = flipped ? (7 - col) : col;
                        onSquareClicked.accept(logicalRow, logicalCol);
                    }
                });

                buttons[r][c] = btn;
                add(btn);
            }
        }
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
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
        clearHighlights();
        render();
    }

    public char getPieceAt(int r, int c) {
        return boardState[r][c];
    }

    public void setPieceAt(int r, int c, char piece) {
        boardState[r][c] = piece;
    }

    public void setSelectedSquare(Point p) {
        this.selectedPoint = p;
    }

    public void setHighlightedSquares(Set<String> squares) {
        this.highlightedSquares.clear();
        if (squares != null) {
            this.highlightedSquares.addAll(squares);
        }
    }

    public void clearHighlights() {
        this.selectedPoint = null;
        this.highlightedSquares.clear();
    }

    public void render() {
        int targetSize = 58; 

        for (int visualRow = 0; visualRow < 8; visualRow++) {
            for (int visualCol = 0; visualCol < 8; visualCol++) {
                // Mapeo lógico según orientación
                int logicalRow = flipped ? (7 - visualRow) : visualRow;
                int logicalCol = flipped ? (7 - visualCol) : visualCol;

                char piece = boardState[logicalRow][logicalCol];

                JButton btn = buttons[visualRow][visualCol];
                btn.setText("");
                btn.setIcon(PieceImages.getIcon(piece, targetSize));

                // El patrón de colores del tablero sigue basándose en la posición lógica
                Color baseColor = (logicalRow + logicalCol) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                btn.setBackground(baseColor);

                if (selectedPoint != null && selectedPoint.y == logicalRow && selectedPoint.x == logicalCol) {
                    btn.setBackground(SELECTED_COLOR);
                }

                String sq = "" + (char)('a' + logicalCol) + (8 - logicalRow);
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

        // Coronación
        if (moveStr.length() == 5) {
            char promo = moveStr.charAt(4);
            movingPiece = Character.isUpperCase(movingPiece) ? Character.toUpperCase(promo) : Character.toLowerCase(promo);
        }

        // Enroque visual
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
}