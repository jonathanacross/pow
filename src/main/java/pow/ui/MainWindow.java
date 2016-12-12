package pow.ui;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class MainWindow extends JFrame implements KeyListener {
    private MainDraw draw;

    public void keyPressed(KeyEvent e) {
        draw.processKey(e);
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public MainWindow() {
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setTitle("Pearls of Wisdom");
        setResizable(true);
        setSize(MainDraw.PANEL_WIDTH, MainDraw.PANEL_HEIGHT);
        setMinimumSize(new Dimension(MainDraw.PANEL_WIDTH, MainDraw.PANEL_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        draw = new MainDraw();
        getContentPane().add(draw);
        pack();

        setVisible(true);
    }
}