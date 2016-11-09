package ui;

import game.GameBackend;
import game.GameState;
import game.frontend.Frontend;
import util.Observer;

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
                    // will send things to the backend..
                    gameFrontend.processKey(keyEvent);
                }

                // force redraw..
                observer.update();
                //gameBackend.notifyUpdate();

                Thread.sleep(50);

                //request.process(gameBackend);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
