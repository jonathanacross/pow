package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;

import java.awt.*;
import java.awt.event.KeyEvent;
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

    public PortalChoiceWindow(int x, int y, GameBackend backend, Frontend frontend,
                              String message,
                              List<AreaNameAndId> areas,
                              Consumer<String> callback) {
        super( new WindowDim(x, y, 300, 70 + Style.FONT_SIZE * areas.size()),
                true, backend, frontend);
        this.message = message;
        this.areas = areas;
        this.callback = callback;
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

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);

        graphics.drawString(this.message, Style.SMALL_MARGIN, Style.SMALL_MARGIN + Style.FONT_SIZE);

        int y = 45;
        int idx = 0;
        for (AreaNameAndId areaId : areas) {
            String label = (char) ((int) 'a' + idx) + ")";
            graphics.drawString(label, Style.SMALL_MARGIN, y);
            graphics.drawString(areaId.name, Style.SMALL_MARGIN + 20, y);
            idx++;
            y += Style.FONT_SIZE;
        }

        graphics.setColor(Color.WHITE);
        graphics.drawString("Select an area or press [esc] to cancel.", Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }
}
