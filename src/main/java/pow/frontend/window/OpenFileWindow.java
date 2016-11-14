package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.frontend.Frontend;
import pow.frontend.save.SaveUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

// TODO: rename to OpenGameWindow for consistency
public class OpenFileWindow extends AbstractWindow {

    private List<File> files;
    private int selectIndex = 0;

    public OpenFileWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
        refreshFileList();
    }

    public void refreshFileList() {
        this.files = SaveUtils.findSaveFiles();
        this.selectIndex = 0;
    }

    @Override
    public void processKey(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_K:
                if (selectIndex > 0) {
                    selectIndex--;
                    frontend.setDirty(true);
                }
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_J:
                if (selectIndex < files.size() - 1) {
                    selectIndex++;
                    frontend.setDirty(true);
                }
                break;
            case KeyEvent.VK_D:
                if (! files.isEmpty()) {
                    files.get(selectIndex).delete();
                    files.remove(selectIndex);
                    frontend.setDirty(true);
                }
                break;
            case KeyEvent.VK_N:
                frontend.setState(Frontend.State.CREATE_CHAR);
                break;
            case KeyEvent.VK_ENTER:
                GameState state = SaveUtils.readFromFile(files.get(selectIndex));
                backend.load(state);
                frontend.setState(Frontend.State.GAME);
                break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        int squareSize = 18;
        Font font = new Font("Courier", Font.PLAIN, squareSize);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Select your game.", 30, 30);

        graphics.setFont(new Font("Courier", Font.PLAIN, 12));
        graphics.drawString("up/down to select, d to delete, n for new game", 30, 50);

        graphics.setFont(font);
        int y = 90;
        int idx = 0;
        for (File f: files) {
            if (idx == selectIndex) {
                graphics.setColor(Color.YELLOW);
                graphics.drawString(">", 10, y);
            } else {
                graphics.setColor(Color.WHITE);
            }
            graphics.drawString(f.getName(), 30, y);
            idx++;
            y += 20;
        }
    }
}
