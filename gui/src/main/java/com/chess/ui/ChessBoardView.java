package com.chess.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class ChessBoardView extends JPanel {

    private final JButton[][] buttons = new JButton[8][8];
    private final char[][] boardState = new char[8][8];
    private final Set<String> highlightedSquares = new HashSet<>();
    private Point selectedPoint = null;

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
                        onSquareClicked.accept(row, col);
                    }
                });

                buttons[r][c] = btn;
                add(btn);
            }
        }
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
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char piece = boardState[r][c];
                buttons[r][c].setText(PieceImages.getSymbol(piece));

                Color baseColor = (r + c) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                buttons[r][c].setBackground(baseColor);

                if (selectedPoint != null && selectedPoint.y == r && selectedPoint.x == c) {
                    buttons[r][c].setBackground(SELECTED_COLOR);
                }

                String sq = "" + (char)('a' + c) + (8 - r);
                if (highlightedSquares.contains(sq)) {
                    buttons[r][c].setBackground(MOVE_HINT_COLOR);
                }
            }
        }
    }
}