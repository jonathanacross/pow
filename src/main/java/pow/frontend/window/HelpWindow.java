package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.HelpController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.frontend.widget.ScrollBar;
import pow.frontend.widget.Table;

import java.awt.*;
import java.awt.event.KeyEvent;

public class HelpWindow extends AbstractWindow {

    private final Table helpTable;
    private final ScrollBar scrollBar;

    public HelpWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        helpTable = HelpController.getHelpTable(dim.width - 3 * Style.MARGIN);
        scrollBar = new ScrollBar(getScrollBarHeight(), getViewSize(),
                helpTable.getHeight(), 20);
    }

    private int getScrollBarHeight() { return dim.height - 2*Style.MARGIN; }
    private int getViewSize() { return dim.height - 2*Style.MARGIN; }

    @Override
    public void processKey(KeyEvent e) {
        KeyInput input = KeyUtils.getKeyInput(e);
        switch (input) {
            case NORTH:
                scrollBar.scrollUp();
                break;
            case SOUTH:
                scrollBar.scrollDown();
                break;
            default:
                frontend.close();
                break;
        }
        frontend.setDirty(true);
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);

        scrollBar.adjustDimensions(getScrollBarHeight(), getViewSize(), helpTable.getHeight());
        helpTable.draw(graphics, Style.MARGIN, Style.MARGIN - scrollBar.getPosition());
        scrollBar.draw(graphics, dim.width - Style.MARGIN, Style.MARGIN);
    }
}
