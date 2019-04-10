package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.widget.*;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;

public class StatusWindow extends AbstractWindow {

    public StatusWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
    }

    private static final int BAR_WIDTH = 135;
    private static final Color HEALTH_BAR_COLOR = new Color(178, 0, 0);
    private static final Color MANA_BAR_COLOR = new Color(0, 0, 178);

    private Widget getStats(Player p, boolean includeGold, Font font) {
        Table table = new Table();
        table.addRow(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList("Exp:"), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(String.valueOf(p.experience)), State.NORMAL, font))
        ));
        table.addRow(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList("Exp next:"), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(String.valueOf(p.getExpToNextLevel())), State.NORMAL, font))
        ));
        table.addRow(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList("Level:"), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(String.valueOf(p.level)), State.NORMAL, font))
        ));
        if (includeGold) {
            table.addRow(Arrays.asList(
                    new TableCell(new TextBox(Collections.singletonList("Gold:"), State.NORMAL, font)),
                    new TableCell(new TextBox(Collections.singletonList(String.valueOf(p.gold)), State.NORMAL, font))
            ));
        }
        table.setHSpacing(Style.MARGIN);
        table.autosize();
        return table;
    }

    private Table getActorSummary(Actor a, boolean showExact, boolean selected, Widget stats, Font font) {
        Table rightCol = new Table();
        int healhBarFilledWidth = (int) Math.round(BAR_WIDTH * (double) a.getHealth() /a.getMaxHealth());
        int manaBarFilledWidth = (int) Math.round(BAR_WIDTH * (double) a.getMana() /a.getMaxMana());
        String healthBarText = showExact ? "HP: " + a.getHealth() + "/" + a.getMaxHealth() : "";
        String manaBarText = showExact ? "MP: " + a.getMana() + "/" + a.getMaxMana() : "";
        rightCol.addColumn(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList(TextUtils.format(a.name, 1, false)), State.NORMAL, font)),
                new TableCell(new Bar(BAR_WIDTH, healhBarFilledWidth, HEALTH_BAR_COLOR, healthBarText, font)),
                new TableCell(new Bar(BAR_WIDTH, manaBarFilledWidth, MANA_BAR_COLOR, manaBarText, font)),
                new TableCell(stats)
        ));
        rightCol.autosize();

        Table table = new Table();
        table.addRow(Arrays.asList(
                new TableCell(new Tile(a.image, selected ? State.SELECTED : State.NORMAL), TableCell.VertAlign.TOP),
                new TableCell(rightCol, TableCell.VertAlign.TOP)
        ));
        table.setHSpacing(Style.SMALL_MARGIN);
        table.autosize();

        return table;
    }

    private Table getPartyLayout() {
        GameState gs = backend.getGameState();
        Font font = Style.getDefaultFont();

        Table layout = new Table();
        boolean selectPlayer = gs.party.selectedActor == gs.party.player;
        layout.addRow(Collections.singletonList(
                new TableCell(getActorSummary(gs.party.player, true, selectPlayer,
                        getStats(gs.party.player, true, font), font))
        ));
        if (gs.party.pet != null) {
            boolean selectPet = gs.party.selectedActor == gs.party.pet;
            layout.addRow(Collections.singletonList(
                    new TableCell(getActorSummary(gs.party.pet, true, selectPet,
                            getStats(gs.party.pet, false, font), font))
            ));
        }
        layout.setVSpacing(Style.MARGIN);
        layout.autosize();

        return layout;
    }

    private Table getMonsterLayout() {
        GameState gs = backend.getGameState();
        Font font = Style.getDefaultFont();

        Table layout = new Table();
        for (Actor a: gs.getCurrentMap().actors) {
            if (gs.party.containsActor(a)) continue;
            if (!gs.party.player.canSeeLocation(gs, a.loc)) continue;
            layout.addRow(Collections.singletonList(
                    new TableCell(getActorSummary(a, false, false, new Space(), font))
            ));
        }
        layout.setVSpacing(Style.MARGIN);
        layout.autosize();

        return layout;
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Table partyLayout = getPartyLayout();
        partyLayout.draw(graphics, Style.SMALL_MARGIN, Style.SMALL_MARGIN);

        graphics.setColor(Style.SEPARATOR_LINE_COLOR);
        int y = Style.SMALL_MARGIN + Style.MARGIN + partyLayout.getHeight();
        graphics.drawLine(Style.SMALL_MARGIN, y, dim.width - Style.SMALL_MARGIN, y);

        Table monsterLayout = getMonsterLayout();
        monsterLayout.draw(graphics, Style.SMALL_MARGIN, y + Style.MARGIN);
    }
}
