package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.table.EmptyCell;
import pow.frontend.utils.table.Table;
import pow.frontend.utils.table.TableBuilder;
import pow.frontend.utils.table.TextCell;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
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
        TableBuilder placeListBuilder = new TableBuilder();
        for (int i = 0; i < areas.size(); i++) {
            String label = (char) ((int) 'a' + i) + ")";
            String areaName = areas.get(i).name;
            placeListBuilder.addRow(Arrays.asList(
                    new TextCell(Arrays.asList(label), TextCell.Style.NORMAL, font),
                    new TextCell(Arrays.asList(areaName), TextCell.Style.NORMAL, font)
            ));
        }
        placeListBuilder.setHSpacing(Style.MARGIN);
        Table placeList = placeListBuilder.build();

        // build the outer layout
        TableBuilder builder = new TableBuilder();
        builder.addRow(Arrays.asList(
                new TextCell(Arrays.asList(message), TextCell.Style.NORMAL, font)
        ));
        builder.addRow(Arrays.asList(
                placeList
        ));
        builder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("Select an area or press [esc] to cancel."), TextCell.Style.NORMAL, font)
        ));

        builder.setVSpacing(Style.MARGIN);
        return builder.build();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        layoutTable.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
