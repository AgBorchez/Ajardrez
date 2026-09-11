package com.chess.ui.components.buttons;

import javax.swing.JButton;
import java.awt.*;

public class SquareButton extends JButton {
    
    private Image texture;
    private boolean selected;
    private boolean hint;


    public SquareButton(Image floorTextureImage) {
        setContentAreaFilled(false); 
        setBorderPainted(false);
        setFocusPainted(false);
        this.selected = false;
        this.hint = false;
        this.texture = floorTextureImage;
    }

    public void setTexture(Image Texture) {
        this.texture = Texture;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        
        Graphics2D g2 = (Graphics2D) g.create();
        
        // 1. Capa inferior: Textura de mármol/suelo
        if (texture != null && !hint && !selected) {
            g2.drawImage(texture, 0, 0, getWidth(), getHeight(), this);
        }

        super.paintComponent(g);

        

        g2.dispose();

        // 3. Capa superior: El ícono de la pieza (lo pinta super.paintComponent)
        
    }

    
}
