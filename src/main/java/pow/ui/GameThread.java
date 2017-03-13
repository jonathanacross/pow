package pow.ui;

import pow.frontend.Frontend;
import pow.util.DebugLogger;
import pow.util.Observer;

import java.awt.event.KeyEvent;
import java.util.Queue;

public class GameThread implements Runnable {
    private final Frontend gameFrontend;
    private final Queue<KeyEvent> queue;
    private final Observer observer;

    private static final int ANIMATION_DELAY_MILLIS = 10;


    public GameThread(Frontend gameFrontend, Queue<KeyEvent> queue, Observer observer) {
        this.gameFrontend = gameFrontend;
        this.queue = queue;
        this.observer = observer;
    }

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
