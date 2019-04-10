package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.actors.Knowledge;
import pow.backend.actors.Player;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.MonsterDisplay;
import pow.frontend.widget.*;
import pow.util.MathUtils;
import pow.util.TextUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KnowledgeWindow extends AbstractWindow {

    private ScrollBar scrollBar;
    private final List<Knowledge.MonsterSummary> monsterSummary;

    public KnowledgeWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend,
                           java.util.List<Knowledge.MonsterSummary> monsterSummary) {
        super(dim, visible, backend, frontend);

        this.monsterSummary = monsterSummary;
        int numViewableMonsters = ((dim.height - 75) / (2*Style.TILE_SIZE)) * 2;
        int sbHeight = numViewableMonsters * Style.TILE_SIZE;
        this.scrollBar = new ScrollBar(sbHeight, 1, monsterSummary.size(), 1);
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
            default:
                frontend.close();
                break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setColor(Color.WHITE);

        Font font = Style.getDefaultFont();
        graphics.setFont(font);
        graphics.drawString("Monster Knowledge", Style.SMALL_MARGIN, Style.SMALL_MARGIN + Style.getFontSize());

        // compute the number of monsters we can show given the window size
        // Note that the number must be even.
        int numViewableMonsters = ((dim.height - 75) / (2*Style.TILE_SIZE)) * 2;

        int minIndex = 0;
        int maxIndex = monsterSummary.size();
        int selectIndex = scrollBar.getPosition();
        if (monsterSummary.size() > numViewableMonsters) {
            int radius = numViewableMonsters/2;
            int centerIndex = MathUtils.clamp(selectIndex, radius, monsterSummary.size() - radius);
            minIndex = Math.max(0, centerIndex - radius);
            maxIndex = Math.min(monsterSummary.size(), centerIndex + radius);
        }

        Table table = new Table();
        table.addRow(Arrays.asList(
                new TableCell(new Space()),
                new TableCell(new TextBox(Collections.singletonList("Name"), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList("Killed"), State.NORMAL, font))
        ));

        for (int index = minIndex; index < maxIndex; index++) {
            Knowledge.MonsterSummary ms = monsterSummary.get(index);
            State state = index == selectIndex ? State.SELECTED : State.NORMAL;
            table.addRow(Arrays.asList(
                    new TableCell(new Tile(ms.image, state)),
                    new TableCell(new TextBox(Collections.singletonList(TextUtils.singular(ms.name)), state, font)),
                    new TableCell(new TextBox(Collections.singletonList(String.valueOf(ms.numKilled)), state, font))
            ));
        }
        table.setDrawHeaderLine(true);
        table.setColWidths(Arrays.asList(Style.TILE_SIZE + Style.SMALL_MARGIN, 180, 50));
        table.autosize();
        table.draw(graphics, Style.SMALL_MARGIN, Style.SMALL_MARGIN + 3*Style.getFontSize());

        scrollBar.draw(graphics, 295, 65);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Press up/down to scroll through monsters, any other key to close.", Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);

        if (!monsterSummary.isEmpty()) {
            Player player = backend.getGameState().party.player;
            MonsterDisplay.drawMonsterInfo(
                    graphics,
                    monsterSummary.get(selectIndex),
                    player,
                    true,
                    320,
                    new pow.util.Point(320,40));
        }
    }
}
