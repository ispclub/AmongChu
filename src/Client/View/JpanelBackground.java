/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.View;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author hoang
 */
public class JpanelBackground extends JPanel {

    protected Image backgroundImage = null;

    public JpanelBackground() {
        this(null);
    }

    public JpanelBackground(String imagePath) {
        setOpaque(false);
        if (imagePath != null) {
            this.backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            int height, width;
            height = this.getSize().height;
            width = this.getSize().width;
            g.drawImage(backgroundImage, 0, 0, width, height, this);
        }
    }

    public void setBackgroundImage(String imagePath) {
        if (imagePath != null) {
            this.backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        }
        this.repaint();
    }
}
