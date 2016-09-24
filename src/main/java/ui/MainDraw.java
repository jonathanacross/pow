package ui; /**
 * Created by jonathan on 9/23/16.
 */

import game.GameState;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class MainDraw extends JPanel {


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        GameState gs = GameState.getInstance();
        g.drawRect(gs.x, gs.y, 50, 50);
        g.fillRect(gs.x, gs.y, 50, 50);
        g.setColor(Color.BLACK);
    }

    public void processKey(KeyEvent e) {
        GameState gs = GameState.getInstance();
        if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            gs.moveRight();
        else if (e.getKeyCode() == KeyEvent.VK_LEFT)
            gs.moveLeft();
        else if (e.getKeyCode() == KeyEvent.VK_DOWN)
            gs.moveDown();
        else if (e.getKeyCode() == KeyEvent.VK_UP)
            gs.moveUp();
        repaint();
    }
}