package com.chess.ui;

import com.chess.engine.EngineBridge;
import com.chess.ui.components.ChessBoardView;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class GameWindow extends JFrame {

    private final GameSession session;
    private final ChessBoardView boardView;
    private JButton undoButton;
    private JButton redoButton;

    public GameWindow(EngineBridge bridge) {
        setTitle("Chess Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Instanciar la lógica pura
        this.session = new GameSession(bridge);

        // 2. Instanciar la vista pasiva conectando sus dos intenciones
        this.boardView = new ChessBoardView(
            this::handleSquareSelected,
            this::handleMoveAttempted
        );

        // 3. Estructura visual
        setJMenuBar(createMenuBar());
        add(boardView, BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

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
        updateUIState();
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
            updateUIState();

            requestAiMove();
        }
    }

    private void requestAiMove() {
        updateUIState();
        session.triggerAiMoveAsync(aiMove -> SwingUtilities.invokeLater(() -> {
            if (aiMove != null && aiMove.length() >= 4) {
                boardView.applyMoveNotation(aiMove);
                boardView.render();
            }
            updateUIState();
        }));
    }

    private void executeUndo() {
        if (session.undo()) {
            // Reconstruir la posición del tablero a partir del historial restante
            boardView.resetState();
            for (String pastMove : session.getMoveHistory()) {
                boardView.applyMoveNotation(pastMove);
            }
            boardView.render();
            updateUIState();
        }
    }

    private void executeRedo() {
        List<String> moves = session.redo();
        if (!moves.isEmpty()) {
            for (String move : moves) {
                boardView.applyMoveNotation(move);
            }
            boardView.render();
            updateUIState();
        }
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

        undoButton = new JButton("↶");
        undoButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        undoButton.setFocusable(false);
        undoButton.setToolTipText("Deshacer jugada");
        undoButton.addActionListener(e -> executeUndo());

        redoButton = new JButton("↷");
        redoButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        redoButton.setFocusable(false);
        redoButton.setToolTipText("Rehacer jugada");
        redoButton.addActionListener(e -> executeRedo());

        bottomPanel.add(undoButton);
        bottomPanel.add(redoButton);

        return bottomPanel;
    }

    private void updateUIState() {
        boolean canInteract = !session.isAiTurn();
        undoButton.setEnabled(canInteract && session.canUndo());
        redoButton.setEnabled(canInteract && session.canRedo());
    }
}