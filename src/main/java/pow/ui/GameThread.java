package pow.ui;

import pow.frontend.Frontend;
import pow.util.DebugLogger;
import pow.util.Observer;

import java.awt.event.KeyEvent;
import java.util.Queue;

// This class encapsulates the thread where the game actually runs.
// This runs in a separate thread than the main java UI thread so
// that the UI will remain responsive, and so that we don't lose
// any key events.
//
// The main game event loop is in the run method here, and the entire
// game frontend (which in turn contains the backend) is a member
// variable of this class.
//
// To make sure the game renders immediately, this has a reference
// to the MainDraw (via the 'observer' member).
//
// Conversely, the MainDraw class, part of the java UI, maintains a
// threadsafe queue of keypresses, which is injected into this class
// so that we can process them.
public class GameThread implements Runnable {
    private final Frontend gameFrontend;
    private final Queue<KeyEvent> queue;
    private final Observer observer;

    private static final int ANIMATION_DELAY_MILLIS = 5;


    public GameThread(Frontend gameFrontend, Queue<KeyEvent> queue, Observer observer) {
        this.gameFrontend = gameFrontend;
        this.queue = queue;
        this.observer = observer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                KeyEvent keyEvent = queue.poll();
                if (keyEvent != null) {
                    gameFrontend.addKeyEvent(keyEvent);
                }

                gameFrontend.update();
                if (gameFrontend.isDirty()) {
                    observer.update();
                    gameFrontend.setDirty(false);
                }

                Thread.sleep(ANIMATION_DELAY_MILLIS);
            }
        } catch (InterruptedException ex) {
            // Not sure what the best thing to do is here..
            // currently just exiting so it will be obvious if this happens.
            DebugLogger.fatal(ex);
        }
    }

}
