package com.chess.ui.view;

import com.chess.ui.components.buttons.SquareButton;
import com.chess.ui.theme.ThemeManager;
import com.chess.ui.components.BoardState;
import com.chess.ui.util.NotationUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChessBoardView extends JPanel {

    private final SquareButton[][] squares = new SquareButton[8][8];
    private final ThemeManager themeManager;

    private Consumer<String> onSquareSelected; // Notifica "e2" para pedir jugadas legales
    private Supplier<String> selectedSquare;
    private final BiFunction<Integer, Integer, Character> pieceGetter;
    private Predicate<String> hintSupplier;
    private boolean flipped = false;

    private int currentTileSize = 1;

    private Image selectedSquareTexture;
    private Image hintSquareTexture;
    private Image darkSquareTexture;
    private Image lightSquareTexture;

    

    public ChessBoardView(ThemeManager themeManager, BiFunction<Integer, Integer, Character> pieceGetter, 
        Consumer<String> onSquareSelected, Supplier<String> selectedSquare, 
        Predicate<String> hintsSupplier) {

        this.themeManager = themeManager;
        this.pieceGetter = pieceGetter;
        this.onSquareSelected = onSquareSelected;
        this.selectedSquare = selectedSquare;
        this.hintSupplier = hintsSupplier;
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
        render();
    }
    

    private void initButtons() {

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                SquareButton btn = new SquareButton(lightSquareTexture);
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

            darkSquareTexture = this.themeManager.getDarkSquare(currentTileSize);
            selectedSquareTexture = this.themeManager.getSelectedOverlay(currentTileSize);
            hintSquareTexture = this.themeManager.getValidMoveOverlay(currentTileSize);
            lightSquareTexture = this.themeManager.getLightSquare(currentTileSize);
        }
    }    

    private void handleSquareClick(int r, int c) {
        String clickedSq = NotationUtils.toNotation(r, c);

        if (onSquareSelected != null) {
            onSquareSelected.accept(clickedSq);
        }
    }

    

    public void render() {

        for (int visualRow = 0; visualRow < 8; visualRow++) {
            for (int visualCol = 0; visualCol < 8; visualCol++) {
                int logicalRow = flipped ? (7 - visualRow) : visualRow;
                int logicalCol = flipped ? (7 - visualCol) : visualCol;

                char piece = pieceGetter.apply(logicalRow, logicalCol);
                SquareButton square = squares[visualRow][visualCol];

                square.setText("");
                square.setIcon(themeManager.getPieceImage(piece, currentTileSize));

                Image baseImage = (visualCol + visualRow) % 2 == 0 ? lightSquareTexture : darkSquareTexture;
                square.setTexture(baseImage);

                String selectedPoint = selectedSquare.get();

                if (selectedPoint != null && NotationUtils.toRow(selectedPoint) == logicalRow && NotationUtils.toCol(selectedPoint) == logicalCol) {
                    square.setTexture(selectedSquareTexture);
                }

                String sq = NotationUtils.toNotation(logicalRow, logicalCol);
                if (hintSupplier.test(sq)) {
                    square.setTexture(hintSquareTexture);
                }
            }
        }
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

}