package com.chess.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import com.chess.ui.util.Logger;

public class EngineProcess implements AutoCloseable {

    private final String binaryPath;
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;

    public EngineProcess(String binaryPath) {
        this.binaryPath = binaryPath;
    }

    /**
     * Inicia el subproceso de C++ y conecta los streams de entrada/salida.
     */
    public synchronized void start() throws IOException {
        File binary = new File(binaryPath);
        if (!binary.exists() || !binary.canExecute()) {
            throw new IOException("El binario del motor no existe o no tiene permisos de ejecución: " + binaryPath);
        }

        ProcessBuilder pb = new ProcessBuilder(binary.getAbsolutePath());
        pb.redirectError(ProcessBuilder.Redirect.INHERIT); // Errores de C++ saldrán directo en la consola de Java

        this.process = pb.start();
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));

        // Test inicial de conexión
        String response = sendCommand("PING");
        if (!"PONG".equals(response)) {
            throw new IOException("Fallo en el handshake inicial con el motor C++. Respuesta: " + response);
        }
    }

    /**
     * Envía un comando de texto al motor y espera una única línea de respuesta.
     */
    public synchronized String sendCommand(String command) throws IOException {
        ensureAlive();

        Logger.info("IPC-OUT", "Comando enviado: " + command);

        writer.write(command);
        writer.newLine();
        writer.flush(); // Crucial: vacía el buffer para que C++ lo reciba al instante

        String response = reader.readLine();
        if (response == null) {
            throw new IOException("El motor de C++ cerró el stream de comunicación inesperadamente.");
        }
        Logger.info("IPC-IN", "Respuesta recibida: " + response);
        return response;
    }

    /**
     * Mecanismo 'salvavidas': si el subproceso murió, levanta uno nuevo en caliente.
     */
    private void ensureAlive() throws IOException {
        if (process == null || !process.isAlive()) {
            System.err.println("[EngineProcess] Motor caído o no iniciado. Reiniciando...");
            start();
        }
    }

    @Override
    public synchronized void close() {
        if (process != null && process.isAlive()) {
            try {
                writer.write("QUIT");
                writer.newLine();
                writer.flush();
                process.waitFor();
            } catch (Exception e) {
                process.destroyForcibly();
            }
        }
    }
}