package pow.ui;

import pow.frontend.Frontend;
import pow.util.Observer;

import java.awt.event.KeyEvent;
import java.util.Queue;

public class GameThread implements Runnable {
    public Frontend gameFrontend;
    private Queue<KeyEvent> queue;
    private Observer observer;


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

                Thread.sleep(50);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
