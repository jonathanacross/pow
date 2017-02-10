package pow.ui;

import javax.swing.SwingUtilities;

public class PoW {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}