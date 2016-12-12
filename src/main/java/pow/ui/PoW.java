package pow.ui;

import javax.swing.SwingUtilities;

public class PoW {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow();
            }
        });
    }
}