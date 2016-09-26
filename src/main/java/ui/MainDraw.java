package ui;

import game.CommandRequest;
import game.GameBackend;
import game.GameState;
import game.Move;
import util.Observer;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;

public class MainDraw extends JPanel implements Observer {

    private Graphics dbg;
    private Image dbImage = null;
    private int panelWidth = 600;
    private int panelHeight = 600;

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
    GameBackend gameBackend;
    BlockingQueue<CommandRequest> commandQueue;

    public MainDraw(GameBackend gameBackend, BlockingQueue<CommandRequest> commandQueue) {
        boxTimer = 0.0;
        animationTask = new AnimationTask(this);
        clock = new Timer(100, animationTask);
        clock.start();
        this.gameBackend = gameBackend;
        this.commandQueue = commandQueue;
        gameBackend.attach(this);

        if(dbImage == null) {
            dbImage = createImage(this.panelWidth, this.panelHeight);
            return;
        }
        dbg = dbImage.getGraphics();
    }

//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//   }

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

        if(dbImage == null) {
            dbImage = createImage(this.panelWidth, this.panelHeight);
            return;
        }
        dbg = dbImage.getGraphics();

        GameState gs = gameBackend.getGameState();

        dbg.setColor(Color.WHITE);
        dbg.fillRect(0, 0, this.panelWidth, this.panelHeight);

        Color boxColor = Color.getHSBColor((float) boxTimer, 1.0f, 0.5f);
        dbg.setColor(boxColor);
        dbg.drawRect(gs.x, gs.y, 50, 50);
        dbg.fillRect(gs.x, gs.y, 50, 50);
        if (gs.arrow > 0) {
            dbg.setColor(Color.BLUE);
            dbg.drawRect(gs.x + 20, gs.y + 50 + gs.arrow, 10, 10);
            dbg.fillRect(gs.x + 20, gs.y + 50 + gs.arrow, 10, 10);
        }

    }

    public void processKey(KeyEvent e) {
        try {
            if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                commandQueue.put(new Move(10, 0));
            else if (e.getKeyCode() == KeyEvent.VK_LEFT)
                commandQueue.put(new Move(-10, 0));
            else if (e.getKeyCode() == KeyEvent.VK_DOWN)
                commandQueue.put(new Move(0, 10));
            else if (e.getKeyCode() == KeyEvent.VK_UP)
                commandQueue.put(new Move(0, -10));
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