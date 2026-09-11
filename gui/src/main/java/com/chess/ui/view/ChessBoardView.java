package com.chess.ui.view;

import com.chess.ui.components.buttons.SquareButton;
import com.chess.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ChessBoardView extends JPanel {

    private final SquareButton[][] squares = new SquareButton[8][8];
    private final char[][] boardState = new char[8][8];
    private final Set<String> highlightedSquares = new HashSet<>();
    private final ThemeManager themeManager;

    private final Consumer<String> onSquareSelected; // Notifica "e2" para pedir jugadas legales
    private final Consumer<String> onMoveAttempted;  // Notifica "e2e4" para ejecutar la jugada

    private Point selectedPoint = null;
    private boolean flipped = false;

    private int currentTileSize = 1;

    private Image selectedSquare;
    private Image hintSquare;
    private Image darkSquare;
    private Image lightSquare;

    

    public ChessBoardView(Consumer<String> onSquareSelected, Consumer<String> onMoveAttempted, ThemeManager themeManager) {
        this.onSquareSelected = onSquareSelected;
        this.onMoveAttempted = onMoveAttempted;
        this.themeManager = themeManager;

        addComponentListener(new ComponentAdapter(){
            @Override 
            public void componentResized(ComponentEvent e){
                updateTileScales();
                render();
            }
        });
        
        setLayout(new GridLayout(8, 8));

        //prueba
        

        setPreferredSize(new Dimension(560, 560));
        updateTileScales();
        initButtons();
        resetState();
        render();
    }
    

    private void initButtons() {

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                SquareButton btn = new SquareButton(lightSquare);
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

                squares[r][c] = btn;
                add(btn);
            }
        }
    }

    private void updateTileScales() {
        int boardSize = Math.min(getWidth(), getHeight());
        if (boardSize <= 0) {
            boardSize = Math.min(getPreferredSize().width, getPreferredSize().height);
        }
        
        int newTileSize = boardSize / 8;
        
        // Solo reescalamos si el tamaño realmente cambió
        if (newTileSize != currentTileSize) {
            currentTileSize = newTileSize;

            darkSquare = this.themeManager.getDarkSquare(currentTileSize);
            selectedSquare = this.themeManager.getSelectedOverlay(currentTileSize);
            hintSquare = this.themeManager.getValidMoveOverlay(currentTileSize);
            lightSquare = this.themeManager.getLightSquare(currentTileSize);
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
    }

    public void render() {

        for (int visualRow = 0; visualRow < 8; visualRow++) {
            for (int visualCol = 0; visualCol < 8; visualCol++) {
                int logicalRow = flipped ? (7 - visualRow) : visualRow;
                int logicalCol = flipped ? (7 - visualCol) : visualCol;

                char piece = boardState[logicalRow][logicalCol];
                SquareButton square = squares[visualRow][visualCol];

                square.setText("");
                square.setIcon(themeManager.getPieceImage(piece, currentTileSize));

                Image baseImage = (visualCol + visualRow) % 2 == 0 ? lightSquare : darkSquare;
                square.setTexture(baseImage);

                if (selectedPoint != null && selectedPoint.y == logicalRow && selectedPoint.x == logicalCol) {
                    square.setTexture(selectedSquare);
                }

                String sq = toNotation(logicalRow, logicalCol);
                if (highlightedSquares.contains(sq)) {
                    square.setTexture(hintSquare);
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