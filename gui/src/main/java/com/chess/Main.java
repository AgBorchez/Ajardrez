package com.chess;

import com.chess.engine.EngineBridge;
import com.chess.engine.EngineProcess;
import com.chess.ui.GameWindow;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Path enginePath = Paths.get("engine/build/chess_engine");
        if (!enginePath.toFile().exists()) {
            enginePath = Paths.get("../engine/build/chess_engine");
        }

        try {
            EngineProcess process = new EngineProcess(enginePath.toAbsolutePath().toString());
            process.start();

            EngineBridge bridge = new EngineBridge(process);
            bridge.newGame();

            SwingUtilities.invokeLater(() -> {
                GameWindow window = new GameWindow(bridge);
                window.setVisible(true);
            });

            Runtime.getRuntime().addShutdownHook(new Thread(process::close));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}