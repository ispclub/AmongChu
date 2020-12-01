/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Model;

/**
 *
 * @author hoang
 */
import java.awt.Color;

import javax.swing.*;
import javax.swing.border.LineBorder;

public class Pikachu extends JButton {

    private int xPoint;
    private int yPoint;

    public Pikachu(int x, int y) {
        super();
        this.xPoint = x;
        this.yPoint = y;
    }

    public int getXPoint() {
        return xPoint;
    }

    public int getYPoint() {
        return yPoint;
    }

    public void drawBorder(Color color) {
        this.setBorder(new LineBorder(color, 2));
    }

    public void removeBorder() {
        this.drawBorder(Color.white);
    }
}
