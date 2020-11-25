/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author hoang
 */
public class PlayGameView extends JpanelBackground implements ActionListener {

    private JPanel pikachuPanel;
    private BorderLayout mainLayout;
    private PlayGameListener playGameListener;
    private GridLayout pikachuLayout;
    private Pikachu[][] pikachuIcon;
    private int row;
    private int col;
    private int countClicked = 0;
    private Pikachu one;
    private Pikachu two;

    private boolean isPlaying = true;

    // them 2 phuong thuc getter
    public PlayGameView() {
        this(10, 10);
    }

    public PlayGameView(int row, int col) {
        super();
        this.row = row;
        this.col = col;
        setVisible(false);
        initUI();
    }

    private void initUI() {
        setVisible(false);
        mainLayout = new BorderLayout();
        this.setLayout(mainLayout);
        this.setBackgroundImage("../../Resource/bg_1.png");

        pikachuPanel = new JPanel();
        pikachuLayout = new GridLayout(row - 2, col - 2, 0, 0);
        pikachuPanel.setLayout(pikachuLayout);
        pikachuPanel.setOpaque(false);
        setAlignmentY(JPanel.CENTER_ALIGNMENT);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(pikachuPanel);
        add(panel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ++countClicked;
        switch (countClicked) {
            case 1:
                one = (Pikachu) e.getSource();
                if (playGameListener != null) {
                    playGameListener.onPikachuClicked(countClicked, one);
                }
                break;
            case 2:
                if (!one.equals(e.getSource())) {
                    two = (Pikachu) e.getSource();
                    if (playGameListener != null) {
                        playGameListener.onPikachuClicked(countClicked, one, two);
                    }
                } else {
                    one.removeBorder();
                }
                countClicked = 0;
                break;
            default:
                break;
        }
    }

    public void renderMap(int[][] matrix) {
        pikachuIcon = new Pikachu[row][col];
        pikachuPanel.removeAll();
        pikachuPanel.invalidate();
        for (int i = 1; i <= row - 2; i++) {
            for (int j = 1; j <= col - 2; j++) {
                pikachuIcon[i][j] = createButton(i, j);
                Icon icon = getIcon(matrix[i][j]);
                pikachuIcon[i][j].setIcon(icon);
                pikachuIcon[i][j].drawBorder(Color.white);
                pikachuPanel.add(pikachuIcon[i][j]);
            }
        }
        pikachuPanel.repaint();
    }

    public void updateMap(int[][] matrix) {
        for (int i = 1; i <= row - 2; i++) {
            for (int j = 1; j <= col - 2; j++) {
                pikachuIcon[i][j].setIcon(getIcon(matrix[i][j]));
                pikachuIcon[i][j].setVisible(true);
            }
        }
        pikachuPanel.invalidate();
        pikachuPanel.repaint();
    }

    private Icon getIcon(int index) {
        int width = 40, height = 40;
        Image image = new ImageIcon(getClass().getResource(
                "../../Resource/ic_" + index + ".png")).getImage();
        Icon icon = new ImageIcon(image.getScaledInstance(width, height,
                image.SCALE_SMOOTH));
        return icon;
    }

    private Pikachu createButton(int x, int y) {
        Pikachu btn = new Pikachu(x, y);
        btn.setBorder(null);
        btn.addActionListener(this);
        return btn;
    }

    public void setPlayGameListener(PlayGameListener playGameListener) {
        this.playGameListener = playGameListener;
    }

    public void setCountClicked(int value) {
        this.countClicked = value;
    }

    public interface PlayGameListener {

        void onPikachuClicked(int clickCounter, Pikachu... pikachus);
    }
}
