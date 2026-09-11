package com.chess.ui.view;

import com.chess.engine.EngineBridge;
import com.chess.ui.GameSession;
import com.chess.ui.components.buttons.*;
import com.chess.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class GameWindow extends JFrame {

    private final GameSession session;
    private final ChessBoardView boardView;
    private final JMenuBar topMenu;
    private final JPanel bottomPanel;
    private ThemeManager Actualtheme;

    public GameWindow(EngineBridge bridge) {
        setTitle("Chess Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Instanciar la lógica pura
        this.session = new GameSession(bridge);

        this.Actualtheme = new ThemeManager("/themes/classicFirstTheme.json");
        // 2. Instanciar la vista pasiva conectando sus dos intenciones
        this.boardView = new ChessBoardView(
            this::handleSquareSelected,
            this::handleMoveAttempted,
            Actualtheme          
        );

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
        boardView.resetState();
        boardView.setFlipped(!playerPlaysWhite);
        boardView.render();

        if (!playerPlaysWhite) {
            requestAiMove();
        }
    }

    // Evento 1: Usuario toca una casilla -> pedimos destinos a la sesión y resaltamos
    private void handleSquareSelected(String sq) {
        if (session.isAiTurn()) return;

        Set<String> targets = session.getLegalTargets(sq);
        boardView.setLegalMoveTargets(targets);
    }

    // Evento 2: Usuario completa una jugada -> aplicamos a la lógica, luego al tablero y pedimos respuesta IA
    private void handleMoveAttempted(String uciMove) {
        if (session.isAiTurn()) return;

        boolean success = session.playPlayerMove(uciMove);
        if (success) {
            boardView.applyMoveNotation(uciMove);
            boardView.render();

            requestAiMove();
        }
    }

    private void requestAiMove() {
        session.triggerAiMoveAsync(aiMove -> SwingUtilities.invokeLater(() -> {
            if (aiMove != null && aiMove.length() >= 4) {
                boardView.applyMoveNotation(aiMove);
                boardView.render();
            }
        }));
    }

    private void setTheme(String Theme){
        this.Actualtheme.loadTheme(Theme);
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

        UndoButton undoButton = new UndoButton(this.session, this.boardView);
        RedoButton redoButton = new RedoButton(this.session, this.boardView);

        bottomPanel.add(undoButton);
        bottomPanel.add(redoButton);

        return bottomPanel;
    }
}