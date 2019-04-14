package pow.frontend.utils;

import pow.backend.SpellParams;
import pow.backend.utils.AttackUtils;
import pow.backend.actors.Player;
import pow.backend.actors.Knowledge;
import pow.frontend.Style;
import pow.frontend.widget.*;
import pow.util.Point;
import pow.util.TextUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MonsterDisplay {

    private static String toPercentString(double d) {
        return String.format("%1$,.1f", d * 100);
    }

    private static List<TableCell> getRow(String key, String value, Font font) {
        return Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList(key), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(value), State.NORMAL, font))
        );
    }

    public static void drawMonsterInfo(
            Graphics graphics,
            Knowledge.MonsterSummary monster,
            Player player, // needed to calculate and show hit percentages
            boolean showCurrentHealth,
            int width,
            Point position
    ) {
        Font font = Style.getDefaultFont();

        // Inner table showing stats
        String healthValue = showCurrentHealth
                ? monster.health + "/" + monster.maxHealth
                : String.valueOf(monster.maxHealth);
        String manaValue = showCurrentHealth
                ? monster.mana + "/" + monster.maxMana
                : String.valueOf(monster.maxMana);
        Table statsTable = new Table();
        statsTable.addRow(getRow("HP:        ", healthValue, font));
        statsTable.addRow(getRow("MP:        ", manaValue, font));
        statsTable.addRow(getRow("", "", font));
        statsTable.addRow(getRow("Exp:       ", "" + monster.experience, font));
        statsTable.addRow(getRow("Level:     ", "" + monster.level, font));
        statsTable.addRow(getRow("", "", font));
        statsTable.addRow(getRow("Attack:    ", "" + monster.primaryAttack, font));
        statsTable.addRow(getRow("Defense:   ", "" + monster.defense, font));
        statsTable.addRow(getRow("Speed:     ", "" + monster.speed, font));
        statsTable.setHSpacing(Style.MARGIN);
        statsTable.autosize();

        // header showing icon and name
        Table header = new Table();
        header.addRow(Arrays.asList(
                new TableCell(new Tile(monster.image, State.NORMAL)),
                new TableCell(new TextBox(Collections.singletonList(TextUtils.singular(monster.name)), State.NORMAL, font), TableCell.VertAlign.BOTTOM)
        ));
        header.setHSpacing(Style.MARGIN);
        header.autosize();

        // Main layout
        String statsLine =
                "Str: " + monster.strength +
                        "  Dex: " + monster.dexterity +
                        "  Int: " + monster.intelligence +
                        "  Con: " + monster.constitution;
        String hitYou = "Can hit you " + toPercentString(AttackUtils.hitProb(monster.primaryAttack.plusToHit, player.getDefense())) + "% of the time";
        String youHit = "You can hit " + toPercentString(AttackUtils.hitProb(player.getPrimaryAttack().plusToHit, monster.defense)) + "% of the time (melee)";
        String bowHit = "You can hit " + toPercentString(AttackUtils.hitProb(player.getSecondaryAttack().plusToHit, monster.defense)) + "% of the time (bow)";
        Table layout = new Table();
        layout.addColumn(Arrays.asList(
                new TableCell(header),
                new TableCell(new TextBox(Collections.singletonList(""), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(monster.description), State.NORMAL, font, width)),
                new TableCell(new TextBox(Collections.singletonList(""), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(statsLine), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(""), State.NORMAL, font)),
                new TableCell(statsTable),
                new TableCell(new TextBox(Collections.singletonList(""), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(hitYou), State.NORMAL, font)),
                new TableCell(new TextBox(Collections.singletonList(youHit), State.NORMAL, font))
        ));
        if (player.hasBowEquipped()) {
            layout.addRow(Collections.singletonList(
                    new TableCell(new TextBox(Collections.singletonList(bowHit), State.NORMAL, font))
            ));
        }
        layout.autosize();

        layout.draw(graphics, position.x, position.y);
    }
}
