package pow.ui;

import pow.frontend.Frontend;
import pow.util.DebugLogger;
import pow.util.Observer;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainDraw extends JPanel implements Observer, ComponentListener {

    private Image dbImage = null;
    public static final int PANEL_WIDTH = 890;
    public static final int PANEL_HEIGHT = 630;

    Frontend frontend;
    Queue<KeyEvent> keyEventQueue;

    public MainDraw() {
        keyEventQueue = new ConcurrentLinkedQueue<>();
        frontend = new Frontend(PANEL_WIDTH, PANEL_HEIGHT);
        GameThread gameThread = new GameThread(frontend, keyEventQueue, this);
        (new Thread(gameThread)).start();
        addComponentListener(this);
   }

   @Override
   public void paint(Graphics graphics) {
       // Theoretically shouldn't have to override this, but doing this
       // removes flashing when the window is resized.
       try {
           if (graphics != null && dbImage != null) {
               graphics.drawImage(dbImage, 0, 0, null);
           }
       } catch (Exception e) { DebugLogger.info("Graphics context error: " + e); }
   }

    private void paintScreen() {
        Graphics g;
        try {
            g = this.getGraphics();
            if (g != null && dbImage != null) {
                g.drawImage(dbImage, 0, 0, null);
                g.dispose();
            }
        } catch (Exception e) { DebugLogger.info("Graphics context error: " + e); }
    }

    public void render() {
        if (dbImage == null) {
            dbImage = createImage(getWidth(), getHeight());
        }
        if (dbImage != null) {
            Graphics graphics = dbImage.getGraphics();
            frontend.draw(graphics);
        }
    }

    public void processKey(KeyEvent e) {
        keyEventQueue.add(e);
    }

    // called when backend changes
    public void update() {
        render();
        paintScreen();
    }

    public void componentHidden(ComponentEvent event) {
    }

    public void componentMoved(ComponentEvent event) {
    }

    public void componentResized(ComponentEvent event) {
        dbImage = null;  // force render to reallocate the image buffer
        frontend.resize(getWidth(), getHeight());
        render();
        paintScreen();
    }

    public void componentShown(ComponentEvent event) {
    }
}
