package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.frontend.widget.State;
import pow.frontend.widget.Table;
import pow.frontend.widget.TableBuilder;
import pow.frontend.widget.TextBox;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;

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

    public Table getTableLayout(String message, String okayText, String cancelText) {
        Font font = Style.getDefaultFont();

        // build the inner option list
        TableBuilder optionTableBuilder = new TableBuilder();
        optionTableBuilder.addRow(Arrays.asList(
                new TextBox(Arrays.asList("[esc]"), State.NORMAL, font),
                new TextBox(Arrays.asList(cancelText), State.NORMAL, font)
        ));
        optionTableBuilder.addRow(Arrays.asList(
                new TextBox(Arrays.asList("[enter]"), State.NORMAL, font),
                new TextBox(Arrays.asList(okayText), State.NORMAL, font)
        ));
        optionTableBuilder.setHSpacing(Style.MARGIN);
        Table options = optionTableBuilder.build();

        // build the main layout
        TableBuilder builder = new TableBuilder();
        builder.addRow(Arrays.asList(
                new TextBox(Arrays.asList(message), State.NORMAL, font)
        ));
        builder.addRow(Arrays.asList(
                options
        ));
        builder.setVSpacing(Style.MARGIN);
        return builder.build();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        tableLayout.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
