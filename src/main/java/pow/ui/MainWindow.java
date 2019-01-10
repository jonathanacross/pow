package pow.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;


public class MainWindow extends JFrame implements KeyListener, MouseMotionListener {
    private final MainDraw draw;

    private static Cursor blankCursor;
    static {
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        draw.processKey(e);
        this.setCursor(blankCursor);
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    public MainWindow() {
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setTitle("Pearls of Wisdom");
        setResizable(true);
        setSize(MainDraw.PANEL_WIDTH, MainDraw.PANEL_HEIGHT);
        setMinimumSize(new Dimension(MainDraw.PANEL_WIDTH, MainDraw.PANEL_HEIGHT));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        draw = new MainDraw();
        getContentPane().add(draw);
        addMouseMotionListener(this);
        pack();

        setVisible(true);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.setCursor(Cursor.getDefaultCursor());
    }
}
