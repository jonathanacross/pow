package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.utils.SaveUtils;
import pow.frontend.WindowDim;
import pow.frontend.utils.KeyUtils;
import pow.frontend.widget.*;
import pow.util.DebugLogger;
import pow.util.MathUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InvalidClassException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OpenGameWindow extends AbstractWindow {

    private List<File> files;
    private ScrollBar scrollBar;

    private static final int NUM_FILES_TO_SHOW = 12;

    public OpenGameWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        refreshFileList();
        Table layoutTable = getLayout();
        int height = 2*Style.MARGIN + layoutTable.getHeight();
        int width = 2*Style.MARGIN + layoutTable.getWidth();
        this.resize(frontend.layout.center(width, height));
    }

    public void refreshFileList() {
        this.files = SaveUtils.findSaveFiles();
        this.scrollBar = new ScrollBar(getFileListHeight(), 1, files.size(), 1);
    }

    @Override
    public void processKey(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_K:
                scrollBar.scrollUp();
                frontend.setDirty(true);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_J:
                scrollBar.scrollDown();
                frontend.setDirty(true);
                break;
            case KeyEvent.VK_D:
                if (KeyUtils.hasShift(e) && !files.isEmpty()) {
                    int selectIndex = scrollBar.getPosition();
                    if (!files.get(selectIndex).delete()) {
                        System.out.println("could not delete " + files.get(selectIndex));
                    }
                    files.remove(selectIndex);
                    refreshFileList();
                    frontend.setDirty(true);
                }
                break;
            case KeyEvent.VK_N:
                frontend.setState(Frontend.State.CREATE_CHAR);
                break;
            case KeyEvent.VK_ENTER:
                if (! files.isEmpty()) {
                    try {
                        int selectIndex = scrollBar.getPosition();
                        GameState state = SaveUtils.readFromFile(files.get(selectIndex));
                        backend.load(state);
                        frontend.setState(Frontend.State.GAME);
                    } catch (InvalidClassException ex) {
                        // May happen if the GameState has a different serialVersionUID if
                        // E.g., from code updates.  Alert the user that we can't open this.
                        frontend.open(new NotificationWindow(true, this.backend, this.frontend,
                                "This file was created with an older version of PoW, and cannot be opened."));
                    } catch (Exception ex) {
                        DebugLogger.fatal(ex);
                    }
                }
                break;
        }
    }

    private int getFileListHeight() {
        Font font = Style.getDefaultFont();
        Table table = new Table();
        for (int i = 0; i < NUM_FILES_TO_SHOW; i++) {
            table.addRow(Arrays.asList(
                    new TableCell(new TextBox(Collections.singletonList(""), State.NORMAL, font))
            ));
        }
        table.autosize();
        return table.getHeight();
    }

    private Table getFileListTable() {
        Font font = Style.getDefaultFont();

        int selectIndex = scrollBar.getPosition();
        int minIndex = 0;
        int maxIndex = files.size();
        if (files.size() > NUM_FILES_TO_SHOW) {
            int radius = NUM_FILES_TO_SHOW/2;
            int centerIndex = MathUtils.clamp(selectIndex, radius, files.size() - radius);
            minIndex = Math.max(0, centerIndex - radius);
            maxIndex = Math.min(files.size(), centerIndex + radius);
        }

        Table table = new Table();
        for (int i = minIndex; i < maxIndex; i++) {
            String fileMarker = i == selectIndex ? ">" : " ";
            State state = i == selectIndex ? State.SELECTED : State.NORMAL;
            table.addRow(Arrays.asList(
                    new TableCell(new TextBox(Collections.singletonList(fileMarker), state, font)),
                    new TableCell(new TextBox(Collections.singletonList(files.get(i).getName()), state, font))
            ));
        }
        for (int i = maxIndex; i < NUM_FILES_TO_SHOW; i++) {
            table.addRow(Arrays.asList(
                    new TableCell(new TextBox(Collections.singletonList(" "), State.NORMAL, font)),
                    new TableCell(new TextBox(Collections.singletonList(" "), State.NORMAL, font))
            ));
        }
        table.setHSpacing(Style.MARGIN);
        table.autosize();

        return table;
    }

    private Table getLayout() {
        Font font = Style.getDefaultFont();

        Table table = new Table();
        table.addColumn(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList("Select your game."), State.NORMAL, font)),
                new TableCell(getFileListTable()),
                new TableCell(new TextBox(Collections.singletonList("Press up/down, [enter] to select, D to delete, n for new game"), State.NORMAL, font))
        ));
        table.setVSpacing(Style.MARGIN);
        table.autosize();

        return table;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Table layout = getLayout();
        layout.draw(graphics, Style.MARGIN, Style.MARGIN);
        scrollBar.draw(graphics, dim.width - Style.MARGIN, 2*Style.MARGIN+Style.getFontSize());
    }
}
