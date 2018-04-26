package pow.frontend.window;

import pow.backend.GameBackend;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

public class PortalChoiceWindow extends AbstractWindow {

    private final String message;
    private final List<String> areaIds;
    private final Consumer<String> callback;

    public PortalChoiceWindow(int x, int y, GameBackend backend, Frontend frontend,
                              String message,
                              List<String> areaIds,
                              Consumer<String> callback) {
        super( new WindowDim(x, y, 300, 50 + FONT_SIZE * areaIds.size()),
                true, backend, frontend);
        this.message = message;
        this.areaIds = areaIds;
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
            if (areaNumber >= 0 && areaNumber < areaIds.size()) {
                this.callback.accept(areaIds.get(areaNumber));
                frontend.close();
            }
        }
    }

    private static final int MARGIN = 10;
    private static final int FONT_SIZE = 12;

    @Override
    public void drawContents(Graphics graphics) {
        String currMessage = this.message;

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        graphics.drawString(currMessage, MARGIN, MARGIN + FONT_SIZE);

        int y = 45;
        int idx = 0;
        for (String areaId : areaIds) {
            String label = (char) ((int) 'a' + idx) + ")";
            graphics.drawString(label, MARGIN, y);
            graphics.drawString(areaId, MARGIN + 20, y);
            idx++;
            y += FONT_SIZE;
        }
    }
}
