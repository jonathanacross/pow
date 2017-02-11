package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.save.SaveUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

public class CreateCharWindow extends AbstractWindow {

    private String name;

    public CreateCharWindow(WindowDim dim, GameBackend backend, Frontend frontend) {
        super(dim, true, backend, frontend);
        resetName();
    }

    public void resetName() {
        name = "";
    }


    private void startNewGame() {
        backend.newGame(name);
        frontend.setState(Frontend.State.GAME);
    }

    // Start the game if
    // (1) it's a new character name, or
    // (2) an existing character name and user has confirmed they want to overwrite.
    private void tryToStartNewGame(String name) {
        // see if there's already a character with this name
        List<File> existingFiles = SaveUtils.findSaveFiles();
        boolean alreadyExists = false;
        for (File f : existingFiles) {
            if (f.getName().equals(name)) {
                alreadyExists = true;
            }
        }

        if (alreadyExists) {
            WindowDim dim = WindowDim.center(600, 120, frontend.width, frontend.height);
            frontend.open(new ConfirmWindow(dim, true, this.backend, this.frontend,
                    "The character '" + name + "' already exists.  Do you want to overwrite it?",
                    "Overwrite", "Cancel",
                    this::startNewGame));
        } else {
            startNewGame();
        }
    }

    @Override
    public void processKey(KeyEvent e) {
        char c = e.getKeyChar();
        if (Character.isLetterOrDigit(c) || c == ' ') {
            name = name + c;
            frontend.setDirty(true);
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
            if (name.length() > 0) {
                name = name.substring(0, name.length() - 1);
                frontend.setDirty(true);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            name = name.trim();
            if (!name.isEmpty()) {
                tryToStartNewGame(name);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            frontend.setState(Frontend.State.OPEN_GAME);
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        int squareSize = 18;
        Font f = new Font("Courier", Font.PLAIN, squareSize);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Enter the name of your character.", 30, 50);

        graphics.drawString(name, 30, 70);
    }
}
