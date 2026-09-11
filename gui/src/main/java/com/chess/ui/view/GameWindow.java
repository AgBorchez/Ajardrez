package com.chess.ui.view;

import com.chess.engine.EngineBridge;
import com.chess.ui.GameSession;
import com.chess.ui.components.BoardState;
import com.chess.ui.components.buttons.*;
import com.chess.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class GameWindow extends JFrame {

    private final GameSession session;
    private final ChessBoardView boardView;
    private final BoardState gameState;
    private final JMenuBar topMenu;
    private final JPanel bottomPanel;
    private final ThemeManager actualTheme;

    public GameWindow(EngineBridge bridge) {
        setTitle("Chess Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Instanciar la lógica y el estado
        this.session = new GameSession(bridge);
        this.gameState = new BoardState();
        this.actualTheme = new ThemeManager("/themes/classicFirstTheme.json");

        // 2. Instanciar la vista y enlazar el listener unificado de clics
        this.boardView = new ChessBoardView(actualTheme, this.gameState::getPiece, 
            this::handleSquareClicked, this.gameState::getSelectedSquare, 
            this.gameState::isLegalTarget);


        // 3. Estructura visual
        topMenu = createMenuBar();
        setJMenuBar(topMenu);
        add(boardView, BorderLayout.CENTER);
        bottomPanel = createBottomBar();
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setPreferredSize(new Dimension(560, 560));
        setMinimumSize(new Dimension(300, 300));
        setLocationRelativeTo(null);
        setResizable(true);

        startNewGame(true);
    }

    private void startNewGame(boolean playerPlaysWhite) {
        session.startNewGame(playerPlaysWhite);
        gameState.resetState();
        boardView.setFlipped(!playerPlaysWhite);
        boardView.render();

        if (!playerPlaysWhite) {
            requestAiMove();
        }
    }

    // Controlador central del clic en casillas (reemplaza a 'a', handleSquareSelected y handleMoveAttempted)
    private void handleSquareClicked(String clickedSq) {
        if (session.isAiTurn()) return;

        // Caso 1: Había selección previa y se clickeó un destino legal -> Intentar jugada
        if (gameState.getSelectedSquare() != null && gameState.isLegalTarget(clickedSq)) {
            String fromSq = gameState.getSelectedSquare();
            String move = fromSq + clickedSq;

            if (gameState.isPromotion(fromSq, clickedSq)) {
                String promChoice = promptPromotion();
                if (promChoice == null || promChoice.isBlank()) {
                    return; // Canceló coronación, no se ejecuta
                }
                move += promChoice;
            }

            boolean success = session.playPlayerMove(move);
            if (success) {
                gameState.applyMoveNotation(move);
                gameState.clearSelection();
                boardView.render();

                requestAiMove();
            }
            return;
        }

        // Caso 2: Clic sobre una pieza del jugador activo -> Seleccionar y pedir hints
        if (isPieceOfCurrentPlayer(clickedSq)) {
            gameState.setSelectedSquare(clickedSq);
            Set<String> legalTargets = session.getLegalTargets(clickedSq);
            gameState.setLegalMoveTargets(legalTargets);

            gameState.setSelectedSquare(clickedSq);
            boardView.render();
        } else {
            // Caso 3: Clic al aire o pieza rival sin jugada legal -> Limpiar foco
            gameState.clearSelection();
            boardView.render();
        }
    }

    private boolean isPieceOfCurrentPlayer(String clickedSq) {
        boolean playsWhite = session.isPlayerPlaysWhite();

        char piece = gameState.getPiece(clickedSq);

        if (playsWhite && Character.isLowerCase(piece)) return false;
        if (!playsWhite && !Character.isLowerCase(piece)) return false;
        if (piece == ' ') return false;

        return true;
    }

    private void requestAiMove() {
        session.triggerAiMoveAsync(aiMove -> SwingUtilities.invokeLater(() -> {
            if (aiMove != null && aiMove.length() >= 4) {
                gameState.applyMoveNotation(aiMove);
                boardView.render();
            }
        }));
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

    private void setTheme(String themePath) {
        this.actualTheme.loadTheme(themePath);
        this.boardView.render();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Partida");
        JMenuItem newGameItem = new JMenuItem("Nueva Partida");
        newGameItem.addActionListener(e -> startNewGame(true));
        gameMenu.add(newGameItem);

        JMenu optionsMenu = new JMenu("Opciones");
        JMenuItem toggleThemeItem = new JMenuItem("Cambiar tema visual");
        toggleThemeItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Opciones visuales en desarrollo."));
        optionsMenu.add(toggleThemeItem);

        menuBar.add(gameMenu);
        menuBar.add(optionsMenu);

        return menuBar;
    }

    private JPanel createBottomBar() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        UndoButton undoButton = new UndoButton(this.session, this.boardView, this.gameState);
        RedoButton redoButton = new RedoButton(this.session, this.boardView, this.gameState);

        bottomPanel.add(undoButton);
        bottomPanel.add(redoButton);

        return bottomPanel;
    }
}