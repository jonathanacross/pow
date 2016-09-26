package ui;

import game.CommandRequest;
import game.GameBackend;

import java.util.concurrent.BlockingQueue;

/**
 * Created by jonathan on 9/25/16.
 */
public class BackendThread implements Runnable {
    public GameBackend gameBackend;
    private final BlockingQueue<CommandRequest> queue;

    public BackendThread(GameBackend gameBackend, BlockingQueue<CommandRequest> queue) {
        this.gameBackend = gameBackend;
        this.queue = queue;
    }

    public void run() {
        try {
            while (true) {
                CommandRequest request = queue.take();
                request.process(gameBackend);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
