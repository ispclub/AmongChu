/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Controller;

import Client.Model.Matrix;
import Client.View.Pikachu;
import Client.View.PlayGameView;
import Server.Model.Message.ClientMessage;
import Utils.Utils;
import static Utils.Utils.MAP_COL;
import static Utils.Utils.MAP_ROW;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Image;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 *
 * @author hoang
 */
public class PikachuController extends JFrame{
    private PlayGameView playGameView;
    private Matrix matrix;
    private int coupleDone;
    private ConnectThread ct;
    public PikachuController(Matrix maxtrix, ConnectThread ct) throws HeadlessException
    {
        super("Quẩy lên bạn ơi");
        this.ct = ct;
        Image icon = (new ImageIcon(getClass().getResource("../../Resource/Pikamong.png"))).getImage();
        this.matrix = maxtrix;
        setIconImage(icon);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    @Override
    protected void frameInit() {
        super.frameInit();
        this.playGameView = new PlayGameView(MAP_ROW, MAP_COL);
        this.playGameView.setSize(Utils.WINDOW_WIDTH, Utils.WINDOW_HEIGHT);
        
    }

    public void close()
    {
        this.dispose();
    }
    public void start() {
        playGameView.renderMap(matrix.renderMatrix());

        int i = (new Random()).nextInt(5);
        playGameView.setBackgroundImage("../../Resource/bg_" + i + ".png");

        coupleDone = 0;

        playGameView.setVisible(true);

        this.playGameView.setPlayGameListener(new PlayGameView.PlayGameListener() {

            @Override
            public void onPikachuClicked(int clickCounter, Pikachu... pikachus) {
                if (clickCounter == 1) {
                    pikachus[0].drawBorder(Color.red);
                } else if (clickCounter == 2) {
                    pikachus[1].drawBorder(Color.red);
                    if (matrix.algorithm(pikachus[0], pikachus[1])) {

                        //Ẩn pikachu nếu chọn đúng
                        matrix.setXY(pikachus[0], 0);
                        matrix.setXY(pikachus[1], 0);

                        pikachus[0].removeBorder();
                        pikachus[1].removeBorder();

                        pikachus[0].setVisible(false);
                        pikachus[1].setVisible(false);

                        //Tăng số cặp chọn đúng lên 1
                        coupleDone++;

                        if (!matrix.canPlay() && coupleDone < (matrix.getRow() - 2) * (matrix.getCol() - 2) / 2) {
                            JOptionPane.showMessageDialog(null, "Không thể chơi tiếp!");
                        }

                        if (coupleDone == (matrix.getRow() - 2) * (matrix.getCol() - 2) / 2) {
                            // TODO : chuc mung chien thang!
                            ClientMessage cm = new ClientMessage(ClientMessage.REQUEST.WIN, null);
                            ct.send(data, socketChannel);
                        }
                    } else {
                        pikachus[0].removeBorder();
                        pikachus[1].removeBorder();
                        playGameView.setCountClicked(0);
                    }
                }
            }
        });

        this.add(playGameView, BorderLayout.CENTER);
        setVisible(true);
    }
}
