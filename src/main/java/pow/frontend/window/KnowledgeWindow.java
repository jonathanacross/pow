package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.actors.Knowledge;
import pow.backend.actors.Player;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.MonsterDisplay;
import pow.util.MathUtils;
import pow.util.TextUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;

public class KnowledgeWindow extends AbstractWindow {

    private int selectIndex;
    private final List<Knowledge.MonsterSummary> monsterSummary;

    public KnowledgeWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend,
                           java.util.List<Knowledge.MonsterSummary> monsterSummary) {
        super(dim, visible, backend, frontend);
        this.monsterSummary = monsterSummary;
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
                if (selectIndex < monsterSummary.size() - 1) {
                    selectIndex++;
                    frontend.setDirty(true);
                }
                break;
            default:
                frontend.close();
                break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setColor(Color.WHITE);

        graphics.setFont(Style.getLargeFont());
        graphics.drawString("Monster Knowledge", Style.SMALL_MARGIN, Style.SMALL_MARGIN + Style.FONT_SIZE);

        final int nameX = 70;
        final int killedX = 220;
        final int fontOffsetY = (Style.TILE_SIZE + Style.FONT_SIZE)/2;


        int y = 50;
        graphics.setFont(Style.getDefaultFont());
        graphics.drawString("Name", nameX, y);
        graphics.drawString("Killed", killedX, y);

        int numViewableMonsters = 18; // must be even
        int minIndex = 0;
        int maxIndex = monsterSummary.size();
        if (monsterSummary.size() > numViewableMonsters) {
            int radius = numViewableMonsters/2;
            int centerIndex = MathUtils.clamp(selectIndex, radius, monsterSummary.size() - radius);
            minIndex = Math.max(0, centerIndex - radius);
            maxIndex = Math.min(monsterSummary.size(), centerIndex + radius);
        }

        y = 60;
        for (int index = minIndex; index < maxIndex; index++) {
            Knowledge.MonsterSummary ms = monsterSummary.get(index);
            ImageController.drawTile(graphics, ms.image, Style.SMALL_MARGIN, y);
            if (index == selectIndex) {
                graphics.setColor(Color.YELLOW);
            } else {
                graphics.setColor(Color.WHITE);
            }
            graphics.drawString(TextUtils.singular(ms.name), nameX, y + fontOffsetY);
            graphics.drawString(String.valueOf(ms.numKilled), killedX, y + fontOffsetY);
            y += Style.TILE_SIZE;
        }

        // draw scrollbar
        int sbTop = 60;
        int sbLeft = 295;
        int sbHeight = numViewableMonsters * Style.TILE_SIZE;
        int centerTop = sbHeight * minIndex / monsterSummary.size();
        int centerBottom = sbHeight * maxIndex / monsterSummary.size();
        graphics.setColor(Color.GRAY);
        graphics.drawLine(sbLeft, sbTop, sbLeft, sbTop + sbHeight);
        graphics.drawRect(sbLeft - 3, centerTop + sbTop, 6, centerBottom - centerTop);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Press up/down to scroll through monsters, any other key to close.", Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);

        if (!monsterSummary.isEmpty()) {
            Player player = backend.getGameState().party.player;
            MonsterDisplay.drawMonsterInfo(
                    graphics,
                    monsterSummary.get(selectIndex),
                    player,
                    true,
                    300,
                    new pow.util.Point(310,30));
        }
    }
}
