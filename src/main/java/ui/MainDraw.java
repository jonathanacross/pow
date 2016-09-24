package ui;

import game.GameState;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class MainDraw extends JPanel {

    class AnimationTask implements ActionListener {
        MainDraw parent;

        public AnimationTask(MainDraw parent) {
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent e) {
            parent.updateAnimation();
        }
    }

    Timer clock;
    double boxTimer;
    AnimationTask animationTask;

    public MainDraw() {
        boxTimer = 0.0;
        animationTask = new AnimationTask(this);
        clock = new Timer(100, animationTask);
        clock.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        GameState gs = GameState.getInstance();

        Color boxColor = Color.getHSBColor((float) boxTimer, 1.0f, 0.5f);
        g.setColor(boxColor);
        g.drawRect(gs.x, gs.y, 50, 50);
        g.fillRect(gs.x, gs.y, 50, 50);
        if (gs.arrow > 0) {
            g.setColor(Color.BLUE);
            g.drawRect(gs.x + 20, gs.y + 50 + gs.arrow, 10, 10);
            g.fillRect(gs.x + 20, gs.y + 50 + gs.arrow, 10, 10);
        }
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

    public void updateAnimation() {
        boxTimer += 0.01;
        if (boxTimer > 1) {
            boxTimer -= 1.0;
        }
        repaint();
    }
}