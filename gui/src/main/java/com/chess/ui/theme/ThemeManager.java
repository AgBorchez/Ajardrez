package com.chess.ui.theme;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

import javax.swing.ImageIcon;

import java.io.IOException;
import java.awt.Image;


public class ThemeManager {

    private final ObjectMapper mapper = new ObjectMapper();
    private BoardTheme currentTheme;


    public ThemeManager(String jsonResourcePath){
        this.loadTheme(jsonResourcePath);
    }

    public void loadTheme(String jsonResourcePath) {
        try (InputStream is = getClass().getResourceAsStream(jsonResourcePath)) {
            this.currentTheme = mapper.readValue(is, BoardTheme.class);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando el tema desde " + jsonResourcePath, e);
        }
    }

    public Image getLightSquare(int size) {
        return TextureLoader.get(currentTheme.board().lightSquare(), size);
    }

    public Image getDarkSquare(int size) {
        return TextureLoader.get(currentTheme.board().darkSquare(), size);
    }

    public Image getSelectedOverlay(int size) {
        return TextureLoader.get(currentTheme.overlays().selected(), size);
    }

    public Image getValidMoveOverlay(int size) {
        return TextureLoader.get(currentTheme.overlays().validMove(), size);
    }

    public ImageIcon getPieceImage(char piece, int size){
        System.out.println("[DEBUG getPieceImage] Entrando con char: '" + piece + "' (ASCII: " + (int)piece + ") | size: " + size);

        if (currentTheme == null) {
            System.out.println("ERROR: currentTheme es NULL q toque wacho");
            return null;
        }

        if (currentTheme.pieces() == null) {
            System.out.println("[DEBUG getPieceImage] ERROR: currentTheme.pieces() es NULL. Revisá el mapeo JSON.");
            return null;
        }

        System.out.println("[DEBUG getPieceImage] Claves cargadas en el mapa: " + currentTheme.pieces().keySet());

        String key = String.valueOf(piece);
        String path = currentTheme.pieces().get(key);

        System.out.println("[DEBUG getPieceImage] Buscando clave '" + key + "' -> Resultado path: " + path);

        if (path == null) {
            System.out.println("[DEBUG getPieceImage] No se encontró ruta para: " + piece);
            return null;
        }

        return TextureLoader.getIcon(path, size);
    }
}

