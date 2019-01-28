package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.utils.SaveUtils;
import pow.frontend.WindowDim;
import pow.frontend.utils.KeyUtils;
import pow.util.DebugLogger;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InvalidClassException;
import java.util.List;

public class OpenGameWindow extends AbstractWindow {

    private List<File> files;
    private int selectIndex = 0;

    public OpenGameWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
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
                if (KeyUtils.hasShift(e) && !files.isEmpty()) {
                    if (!files.get(selectIndex).delete()) {
                        System.out.println("could not delete " + files.get(selectIndex));
                    }
                    files.remove(selectIndex);
                    frontend.setDirty(true);
                }
                break;
            case KeyEvent.VK_N:
                frontend.setState(Frontend.State.CREATE_CHAR);
                break;
            case KeyEvent.VK_ENTER:
                if (! files.isEmpty()) {
                    try {
                        GameState state = SaveUtils.readFromFile(files.get(selectIndex));
                        backend.load(state);
                        frontend.setState(Frontend.State.GAME);
                    } catch (InvalidClassException ex) {
                        // May happen if the GameState has a different serialVersionUID if
                        // E.g., from code updates.  Alert the user that we can't open this.
                        WindowDim dim = WindowDim.center(550, 40, frontend.width, frontend.height);
                        frontend.open(new NotificationWindow(dim, true, this.backend, this.frontend,
                                "This file was created with an older version of PoW, and cannot be opened."));
                    } catch (Exception ex) {
                        DebugLogger.fatal(ex);
                    }
                }
                break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);
        graphics.drawString("Select your game.", Style.MARGIN, Style.MARGIN + Style.FONT_SIZE);

        int y = 60;
        int idx = 0;
        for (File f: files) {
            if (idx == selectIndex) {
                graphics.setColor(Color.YELLOW);
                graphics.drawString(">", Style.MARGIN, y);
            } else {
                graphics.setColor(Color.WHITE);
            }
            graphics.drawString(f.getName(), Style.MARGIN + 20, y);
            idx++;
            y += Style.FONT_SIZE;
        }

        graphics.setColor(Color.WHITE);
        graphics.drawString("Press up/down, [enter] to select, D to delete, n for new game", Style.MARGIN, dim.height - Style.MARGIN);
    }
}
