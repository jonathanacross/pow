package ui;

import game.CommandRequest;
import game.GameBackend;
import game.Move;
import game.frontend.Frontend;
import util.Observer;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;

public class MainDraw extends JPanel implements Observer {

    private Graphics graphics;
    private Image dbImage = null;
    private int panelWidth = 610;
    private int panelHeight = 610;

    double boxTimer;
    GameBackend gameBackend;
    Frontend frontend;
    BlockingQueue<CommandRequest> commandQueue;

    public MainDraw(GameBackend gameBackend, BlockingQueue<CommandRequest> commandQueue) {
        boxTimer = 0.0;
        this.gameBackend = gameBackend;
        this.commandQueue = commandQueue;
        gameBackend.attach(this);
        frontend = new Frontend(0, 0, panelWidth, panelHeight, gameBackend);

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
        try {
            if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                commandQueue.put(new Move(1, 0));
            else if (e.getKeyCode() == KeyEvent.VK_LEFT)
                commandQueue.put(new Move(-1, 0));
            else if (e.getKeyCode() == KeyEvent.VK_DOWN)
                commandQueue.put(new Move(0, 1));
            else if (e.getKeyCode() == KeyEvent.VK_UP)
                commandQueue.put(new Move(0, -1));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void updateAnimation() {
        boxTimer += 0.01;
        if (boxTimer > 1) {
            boxTimer -= 1.0;
        }
        render();
        paintScreen();
        //repaint();
    }

    // called when backend changes
    public void update() {
        render();
        paintScreen();
//        repaint();
//        paintImmediately(0,0,600,600);
    }
}