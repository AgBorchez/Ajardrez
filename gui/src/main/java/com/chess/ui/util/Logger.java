package com.chess.ui.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "chess_gui.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static synchronized void info(String tag, String message) {
        log("INFO", tag, message);
    }

    public static synchronized void error(String tag, String message, Throwable t) {
        log("ERROR", tag, message + (t != null ? " | Exception: " + t.getMessage() : ""));
    }

    private static void log(String level, String tag, String message) {
        String logEntry = String.format("[%s] [%s] [%s]: %s", 
                LocalDateTime.now().format(FORMATTER), level, tag, message);

        System.out.println(logEntry);

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error al escribir el log: " + e.getMessage());
        }
    }
}