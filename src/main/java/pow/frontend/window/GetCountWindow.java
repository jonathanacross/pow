package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.table.*;
import pow.util.MathUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.List;

public class GetCountWindow extends AbstractWindow {

    private String countString;
    private final String imageName;
    private final List<String> messages;
    private final int maxNum;
    private final IntConsumer callback;

    public GetCountWindow(WindowDim dim, GameBackend backend, Frontend frontend, String imageName,
                          List<String> messages, int maxNum, IntConsumer callback) {
        super(dim, true, backend, frontend);
        this.countString = "";
        this.imageName = imageName;
        this.messages = messages;
        this.maxNum = maxNum;
        this.callback = callback;
        Table layout = getLayoutTable();
        this.resize(new WindowDim(dim.x, dim.y, dim.width,  layout.getHeight() + 2*Style.MARGIN));
    }

    @Override
    public void processKey(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            frontend.close();
            return;
        }
        if (keyCode == KeyEvent.VK_ENTER && !countString.isEmpty()) {
            int count = Integer.parseInt(countString);
            if (count == 0) {
                frontend.close();  // let the player escape out
                return;
            }
            count = MathUtils.clamp(count, 1, maxNum);
            frontend.close();
            callback.accept(count);
            return;
        }

        char c = e.getKeyChar();
        if (Character.isDigit(c)) {
            countString = countString + c;
            frontend.setDirty(true);
        } else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
            if (countString.length() > 0) {
                countString = countString.substring(0, countString.length() - 1);
                frontend.setDirty(true);
            }
        }
    }

    public Table getLayoutTable() {
        Font font = Style.getDefaultFont();
        TableBuilder topRowBuilder = new TableBuilder();
        topRowBuilder.addRow(Arrays.asList(
                new ImageCell(imageName, State.NORMAL),
                new TextCell(messages, State.NORMAL, font)
        ));
        topRowBuilder.setHSpacing(Style.MARGIN);
        Table topRow = topRowBuilder.build();

        // main layout
        TableBuilder builder = new TableBuilder();
        builder.addRow(Arrays.asList(
                topRow
        ));
        builder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("> " + countString), State.NORMAL, font)
        ));
        builder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("Press [esc] to cancel."), State.NORMAL, font)
        ));
        builder.setVSpacing(Style.MARGIN);
        return builder.build();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        getLayoutTable().draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
