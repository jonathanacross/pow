package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.widget.State;
import pow.frontend.widget.Table;
import pow.frontend.widget.TableCell;
import pow.frontend.widget.TextBox;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PortalChoiceWindow extends AbstractWindow {

    public static class AreaNameAndId {
        public final String name;
        public final String id;

        public AreaNameAndId(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }

    private final String message;
    private final List<AreaNameAndId> areas;
    private final Consumer<String> callback;
    private final Table layoutTable;

    public PortalChoiceWindow(GameBackend backend, Frontend frontend,
                              String message,
                              List<AreaNameAndId> areas,
                              Consumer<String> callback) {
        super(new WindowDim(0, 0, 0, 0), true, backend, frontend);
        this.message = message;
        this.areas = areas;
        this.callback = callback;
        this.layoutTable = getLayoutTable();
        this.resize(frontend.layout.center(layoutTable.getWidth() + 2*Style.MARGIN,
                layoutTable.getHeight() + 2*Style.MARGIN));
    }

    @Override
    public void processKey(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            // Even if we escape, need to indicate to the backend that we're
            // not in a portal anymore.
            this.callback.accept("");
            frontend.close();
            return;
        }

        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            int areaNumber = keyCode - KeyEvent.VK_A;
            if (areaNumber < areas.size()) {
                this.callback.accept(areas.get(areaNumber).id);
                frontend.close();
            }
        }
    }

    private Table getLayoutTable() {
        Font font = Style.getDefaultFont();

        // build the list of places
        Table placeList = new Table();
        for (int i = 0; i < areas.size(); i++) {
            String label = (char) ((int) 'a' + i) + ")";
            String areaName = areas.get(i).name;
            placeList.addRow(Arrays.asList(
                    new TableCell(new TextBox(Collections.singletonList(label), State.NORMAL, font)),
                    new TableCell(new TextBox(Collections.singletonList(areaName), State.NORMAL, font))
            ));
        }
        placeList.setHSpacing(Style.MARGIN);
        placeList.autosize();

        // build the outer layout
        Table layout = new Table();
        layout.addColumn(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList(message), State.NORMAL, font)),
                new TableCell(placeList),
                new TableCell(new TextBox(Collections.singletonList("Select an area or press [esc] to cancel."), State.NORMAL, font))
        ));

        layout.setVSpacing(Style.MARGIN);
        layout.autosize();
        return layout;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        layoutTable.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
