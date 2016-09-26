package ui;

import game.CommandRequest;
import game.GameBackend;
import game.GameState;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Hello {

    public static class OpenListener implements ActionListener {

        private Component parent;
        private GameBackend backend;

        public OpenListener(Component parent, GameBackend backend) {
            this.parent = parent;
            this.backend = backend;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            int rVal = c.showOpenDialog(parent);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                File file = c.getSelectedFile();

                try (
                        InputStream is = new FileInputStream(file);
                        InputStream bis = new BufferedInputStream(is);
                        ObjectInput input = new ObjectInputStream(bis);
                ) {
                    GameState gs = (GameState) input.readObject();
                    System.out.println(gs.x);
                    System.out.println(gs.y);
                    backend.load(gs);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static class SaveListener implements ActionListener {

        private Component parent;
        private GameBackend backend;

        public SaveListener(Component parent, GameBackend backend) {
            this.parent = parent;
            this.backend = backend;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            int rVal = c.showSaveDialog(parent);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                File file = c.getSelectedFile();

                try (
                        OutputStream os = new FileOutputStream(file);
                        OutputStream bos = new BufferedOutputStream(os);
                        ObjectOutput output = new ObjectOutputStream(bos)
                ) {
                    GameState state = backend.getGameState();
                    System.out.println("currstate: " + state.x + " " + state.y);
                    output.writeObject(state);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        BlockingQueue<CommandRequest> commandQueue = new LinkedBlockingDeque<>();
        GameBackend gameBackend = new GameBackend();
        BackendThread backend = new BackendThread(gameBackend, commandQueue);

        (new Thread(backend)).start();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow(gameBackend, commandQueue);
            }
        });
    }
}