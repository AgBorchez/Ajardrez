package com.chess.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EngineBridge {
    private final EngineProcess process;

    public EngineBridge(EngineProcess process) {
        this.process = process;
    }

    public void newGame() throws IOException {
        process.sendCommand("NEW_GAME");
    }

    public List<String> getLegalMoves(String square) throws IOException {
        String res = process.sendCommand("GET_LEGAL_MOVES " + square);
        if (!res.startsWith("MOVES") || res.length() <= 6) {
            return new ArrayList<>();
        }
        String movesPart = res.substring(6).trim();
        if (movesPart.isEmpty()) return new ArrayList<>();
        return Arrays.asList(movesPart.split(","));
    }

    public void makeMove(String move) throws IOException {
        process.sendCommand("MAKE_MOVE " + move);
    }

    public boolean undoMove() throws IOException {
        String response = process.sendCommand("UNDO_MOVE");
        return "OK_UNDO".equals(response);
    }

    public String calculateAiMove() throws IOException {
        String res = process.sendCommand("CALCULATE_AI");
        if (res.startsWith("BESTMOVE")) {
            return res.substring(9).trim();
        }
        return null;
    }

    public String getGameStatus() throws IOException {
        String res = process.sendCommand("GET_STATUS");
        if (res.startsWith("STATUS")) {
            return res.substring(7).trim(); // "CHECKMATE", "STALEMATE", "ONGOING"
        }
        return "ONGOING";
    }
}