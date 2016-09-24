package ui; /**
 * Created by jonathan on 9/23/16.
 */

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;


public class MainFrame extends JFrame implements KeyListener {
    public MainDraw draw;

    public void keyPressed(KeyEvent e) {
        draw.processKey(e);
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public MainFrame() {
        this.draw = new MainDraw();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }
}
