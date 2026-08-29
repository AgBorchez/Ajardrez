package com.chess.ui;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PieceImages {

    private static final Map<Character, BufferedImage> RAW_IMAGES = new HashMap<>();
    private static final Map<String, ImageIcon> CACHED_ICONS = new HashMap<>();

    static {
        loadPiece('P', "wP.png");
        loadPiece('N', "wN.png");
        loadPiece('B', "wB.png");
        loadPiece('R', "wR.png");
        loadPiece('Q', "wQ.png");
        loadPiece('K', "wK.png");

        loadPiece('p', "bP.png");
        loadPiece('n', "bN.png");
        loadPiece('b', "bB.png");
        loadPiece('r', "bR.png");
        loadPiece('q', "bQ.png");
        loadPiece('k', "bK.png");
    }

    private static void loadPiece(char key, String fileName) {
        String path = "/assets/" + fileName;
        try (InputStream is = PieceImages.class.getResourceAsStream(path)) {
            if (is != null) {
                RAW_IMAGES.put(key, ImageIO.read(is));
            } else {
                System.err.println("[PieceImages] No se encontró el recurso: " + path);
            }
        } catch (Exception e) {
            System.err.println("[PieceImages] Error al cargar: " + path);
            e.printStackTrace();
        }
    }

    public static ImageIcon getIcon(char piece, int size) {
        if (piece == ' ' || !RAW_IMAGES.containsKey(piece)) {
            return null;
        }

        String cacheKey = piece + "_" + size;
        if (CACHED_ICONS.containsKey(cacheKey)) {
            return CACHED_ICONS.get(cacheKey);
        }

        BufferedImage raw = RAW_IMAGES.get(piece);
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(raw, 0, 0, size, size, null);
        g2.dispose();

        ImageIcon icon = new ImageIcon(scaled);
        CACHED_ICONS.put(cacheKey, icon);
        return icon;
    }
}