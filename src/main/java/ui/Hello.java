package ui;

import javax.swing.SwingUtilities;

public class Hello {

//    public static class OpenListener implements ActionListener {
//
//        private Component parent;
//        private GameBackend backend;
//
//        public OpenListener(Component parent, GameBackend backend) {
//            this.parent = parent;
//            this.backend = backend;
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            JFileChooser c = new JFileChooser();
//            int rVal = c.showOpenDialog(parent);
//            if (rVal == JFileChooser.APPROVE_OPTION) {
//                File file = c.getSelectedFile();
//
//                try (
//                        InputStream is = new FileInputStream(file);
//                        InputStream bis = new BufferedInputStream(is);
//                        ObjectInput input = new ObjectInputStream(bis);
//                ) {
//                    GameState gs = (GameState) input.readObject();
//                    System.out.println(gs.x);
//                    System.out.println(gs.y);
//                    backend.load(gs);
//                } catch (ClassNotFoundException ex) {
//                    ex.printStackTrace();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//    }
//
//    public static class SaveListener implements ActionListener {
//
//        private Component parent;
//        private GameBackend backend;
//
//        public SaveListener(Component parent, GameBackend backend) {
//            this.parent = parent;
//            this.backend = backend;
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            JFileChooser c = new JFileChooser();
//            int rVal = c.showSaveDialog(parent);
//            if (rVal == JFileChooser.APPROVE_OPTION) {
//                File file = c.getSelectedFile();
//
//                try (
//                        OutputStream os = new FileOutputStream(file);
//                        OutputStream bos = new BufferedOutputStream(os);
//                        ObjectOutput output = new ObjectOutputStream(bos)
//                ) {
//                    GameState state = backend.getGameState();
//                    System.out.println("currstate: " + state.x + " " + state.y);
//                    output.writeObject(state);
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//    }

    public static void main(String[] args) {

//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow();
            }
        });
    }
}