package ui;

import game.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Hello {

    public static class OpenListener implements ActionListener {

        private Component parent;

        public OpenListener(Component parent) {
            this.parent = parent;
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
                    GameState.load(gs);
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

        public SaveListener(Component parent) {
            this.parent = parent;
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
                    System.out.println("currstate: " + GameState.getInstance().x + " " + GameState.getInstance().y);
                    output.writeObject(GameState.getInstance());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static JMenuBar initMenuBar(Component parent) {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem quitItem = new JMenuItem("Quit");
        menuBar.add(fileMenu);
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(quitItem);
        newItem.addActionListener((ActionEvent event) -> {
            GameState.newGame();
        });
        openItem.addActionListener(new OpenListener(parent));
        saveItem.addActionListener(new SaveListener(parent));
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        quitItem.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem helpItem = new JMenuItem("Help");
        menuBar.add(helpMenu);
        helpMenu.add(aboutItem);
        helpMenu.add(helpItem);

        return menuBar;
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame frame = new MainFrame();
                frame.setTitle("Pearls of Wisdom");
                frame.setResizable(false);
                frame.setSize(600, 600);
                frame.setMinimumSize(new Dimension(600, 600));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(frame.draw);
                frame.pack();

                JMenuBar menuBar = initMenuBar(frame);
                frame.setJMenuBar(menuBar);

                frame.setVisible(true);
            }
        });
    }
}