package ui;

import game.GameBackend;
import game.frontend.Frontend;
import util.Observer;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainDraw extends JPanel implements Observer {

    private Graphics graphics;
    private Image dbImage = null;
    private int panelWidth = 610;
    private int panelHeight = 610;

    double boxTimer;
    Frontend frontend;
    Queue<KeyEvent> keyEventQueue;

    public MainDraw() {
        keyEventQueue = new ConcurrentLinkedQueue<>();

        GameBackend gameBackend = new GameBackend();
        frontend = new Frontend(0, 0, panelWidth, panelHeight, gameBackend);
        GameThread gameThread = new GameThread(frontend, gameBackend, keyEventQueue, this);
        (new Thread(gameThread)).start();

        boxTimer = 0.0;

        if (dbImage == null) {
            dbImage = createImage(this.panelWidth, this.panelHeight);
            return;
        }
        graphics = dbImage.getGraphics();
    }

    private void paintScreen() {
        Graphics g;
        try {
            g = this.getGraphics();
            if ((g != null) && (dbImage != null))  {
                g.drawImage(dbImage, 0, 0, null);
                g.dispose();
            }
        } catch (Exception e) { System.out.println("Graphics context error: " + e); }
    }

    public void render() {

        if (dbImage == null) {
            dbImage = createImage(this.panelWidth, this.panelHeight);
            return;
        }
        graphics = dbImage.getGraphics();
        frontend.draw(graphics);
    }

    public void processKey(KeyEvent e) {
        keyEventQueue.add(e);
    }

    // called when backend changes
    public void update() {
        render();
        paintScreen();
//        repaint();
//        paintImmediately(0,0,600,600);
    }
}