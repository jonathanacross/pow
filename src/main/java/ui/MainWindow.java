package ui; /**
 * Created by jonathan on 9/23/16.
 */

import game.CommandRequest;
import game.GameBackend;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.BlockingQueue;


public class MainWindow extends JFrame implements KeyListener {
    private MainDraw draw;

    public void keyPressed(KeyEvent e) {
        draw.processKey(e);
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public MainWindow(GameBackend gameBackend, BlockingQueue<CommandRequest> commandQueue) {

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setTitle("Pearls of Wisdom");
        setResizable(false);
        setSize(600, 600);
        setMinimumSize(new Dimension(600, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        draw = new MainDraw(gameBackend, commandQueue);
        getContentPane().add(draw);
        pack();

        JMenuBar menuBar = initMenuBar(gameBackend);
        setJMenuBar(menuBar);

        setVisible(true);
    }

    private JMenuBar initMenuBar(GameBackend gameBackend) {
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
            gameBackend.newGame();
        });
        openItem.addActionListener(new Hello.OpenListener(this, gameBackend));
        saveItem.addActionListener(new Hello.SaveListener(this, gameBackend));
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

}
