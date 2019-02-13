package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.actors.Knowledge;
import pow.backend.actors.Player;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.MonsterDisplay;
import pow.frontend.utils.table.Cell;
import pow.frontend.utils.table.EmptyCell;
import pow.frontend.utils.table.ImageCell;
import pow.frontend.utils.table.Table;
import pow.frontend.utils.table.TableBuilder;
import pow.frontend.utils.table.TextCell;
import pow.util.MathUtils;
import pow.util.TextUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
        if (monsterSummary.size() > numViewableMonsters) {
            int radius = numViewableMonsters/2;
            int centerIndex = MathUtils.clamp(selectIndex, radius, monsterSummary.size() - radius);
            minIndex = Math.max(0, centerIndex - radius);
            maxIndex = Math.min(monsterSummary.size(), centerIndex + radius);
        }

        TableBuilder builder = new TableBuilder();
        builder.addRow(Arrays.asList(
                new EmptyCell(),
                new TextCell(Arrays.asList("Name"), TextCell.Style.NORMAL, font),
                new TextCell(Arrays.asList("Killed"), TextCell.Style.NORMAL, font)
        ));

        for (int index = minIndex; index < maxIndex; index++) {
            Knowledge.MonsterSummary ms = monsterSummary.get(index);
            TextCell.Style style = index == selectIndex
                    ? TextCell.Style.SELECTED
                    : TextCell.Style.NORMAL;
            builder.addRow(Arrays.asList(
                    new ImageCell(ms.image, false),
                    new TextCell(Arrays.asList(TextUtils.singular(ms.name)), style, font),
                    new TextCell(Arrays.asList(String.valueOf(ms.numKilled)), style, font)
            ));
        }
        builder.setDrawHeaderLine(true);
        builder.setColWidths(Arrays.asList(Style.TILE_SIZE + Style.SMALL_MARGIN, 180, 50));
        Table monsterTable = builder.build();
        monsterTable.draw(graphics, Style.SMALL_MARGIN, Style.SMALL_MARGIN + 3*Style.getFontSize());

        // draw scrollbar
        int sbTop = 65;
        int sbLeft = 295;
        if (monsterSummary.size() > 0) {
            int sbHeight = numViewableMonsters * Style.TILE_SIZE;
            int centerTop = sbHeight * minIndex / monsterSummary.size();
            int centerBottom = sbHeight * maxIndex / monsterSummary.size();
            graphics.setColor(Style.SEPARATOR_LINE_COLOR);
            graphics.drawLine(sbLeft, sbTop, sbLeft, sbTop + sbHeight);
            graphics.drawRect(sbLeft - 3, centerTop + sbTop, 6, centerBottom - centerTop);
        }

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
