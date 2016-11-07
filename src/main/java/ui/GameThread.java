package ui;

import game.GameBackend;
import game.frontend.Frontend;
import util.Observer;

import java.awt.event.KeyEvent;
import java.util.Queue;

public class GameThread implements Runnable {
    public Frontend gameFrontend;
    public GameBackend gameBackend;
    private Queue<KeyEvent> queue;
    private Observer observer;


    public GameThread(Frontend gameFrontend, GameBackend gameBackend, Queue<KeyEvent> queue, Observer observer) {
        this.gameBackend = gameBackend;
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

                // TODO: should this be moved inside processKey?
                // now.. process any stuff in the backend
                gameBackend.processCommand();

                // force redraw..
                observer.update();
                //gameBackend.notifyUpdate();

                Thread.sleep(100);

                //request.process(gameBackend);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
