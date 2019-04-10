package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.frontend.widget.State;
import pow.frontend.widget.Table;
import pow.frontend.widget.TableCell;
import pow.frontend.widget.TextBox;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;

public class ConfirmWindow extends AbstractWindow {

    private final Table tableLayout;
    private final Runnable action;

    public ConfirmWindow(boolean visible, GameBackend backend, Frontend frontend,
                         String message, String okayText, String cancelText, Runnable action) {
        super(new WindowDim(0, 0, 0, 0), visible, backend, frontend);
        this.tableLayout = getTableLayout(message, okayText, cancelText);
        this.action = action;
        this.resize(frontend.layout.center(tableLayout.getWidth() + 2*Style.MARGIN,
                tableLayout.getHeight() + 2*Style.MARGIN));
    }

    @Override
    public void processKey(KeyEvent e) {
        KeyInput input = KeyUtils.getKeyInput(e);
        switch (input) {
            case OKAY:
                frontend.close();
                action.run();
                break;
            case CANCEL:
                frontend.close();
                break;
            default:
                break;
        }
    }

    private Table getTableLayout(String message, String okayText, String cancelText) {
        Font font = Style.getDefaultFont();

        // build the inner option list
        Table optionTable = new Table();
        optionTable.addRow(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList("[esc]"), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(cancelText), State.NORMAL, font))
        ));
        optionTable.addRow(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList("[enter]"), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(okayText), State.NORMAL, font))
        ));
        optionTable.setHSpacing(Style.MARGIN);
        optionTable.autosize();

        // build the main layout
        Table layout = new Table();
        layout.addColumn(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList(message), State.NORMAL, font)),
                new TableCell(optionTable)
        ));
        layout.setVSpacing(Style.MARGIN);
        layout.autosize();
        return layout;

    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        tableLayout.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
