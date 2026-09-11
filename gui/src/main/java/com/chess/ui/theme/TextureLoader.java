package com.chess.ui.theme;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class TextureLoader {

    // 1. Guarda las imágenes originales leídas del disco (evita I/O repetido)
    private static final Map<String, BufferedImage> RAW_CACHE = new HashMap<>();

    // 2. Guarda las imágenes ya escaladas por "ruta:tamaño" (evita recalcular el escalado)
    private static final Map<String, Image> SCALED_CACHE = new HashMap<>();

    private TextureLoader() {}

    /**
     * Devuelve la imagen cargada y escalada al tamaño exacto solicitado.
     */
    public static Image get(String path, int size) {
        String cacheKey = path + ":" + size;

        return SCALED_CACHE.computeIfAbsent(cacheKey, k -> {
            BufferedImage raw = getRaw(path);
            if (raw == null) return null;

            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            try {
                // 1. Limpia a transparente
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, size, size);

                // 2. CRUCIAL: Volver a SrcOver para que el drawImage pinte los píxeles
                g2.setComposite(AlphaComposite.SrcOver);

                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.drawImage(raw, 0, 0, size, size, null);
            } finally {
                g2.dispose();
            }
            return scaled;
        });
    }

    /**
     * Carga el archivo original desde resources (solo si no se cargó antes).
     */
    public static BufferedImage getRaw(String path) {
        return RAW_CACHE.computeIfAbsent(path, p -> {
            try (InputStream is = TextureLoader.class.getResourceAsStream(p)) {
                if (is == null) {
                    System.err.println("No se encontró el recurso: " + p);
                    return null;
                }
                return ImageIO.read(is);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static ImageIcon getIcon(String path, int size){
        Image img = TextureLoader.get(path, size);

        return (img != null ? new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH)) : null);
        
    }
}