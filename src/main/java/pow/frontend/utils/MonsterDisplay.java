package pow.frontend.utils;

import pow.backend.SpellParams;
import pow.backend.utils.AttackUtils;
import pow.backend.actors.Player;
import pow.backend.actors.Knowledge;
import pow.frontend.Style;
import pow.frontend.utils.table.*;
import pow.util.Point;
import pow.util.TextUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MonsterDisplay {

    private static String toPercentString(double d) {
        return String.format("%1$,.1f", d * 100);
    }

    private static List<String> getSpellNames(List<SpellParams> spells) {
        List<String> strings = new ArrayList<>(spells.size());
        for (SpellParams spell : spells) {
            strings.add(spell.name);
        }
        return strings;
    }

    private static List<Cell> getRow(String key, String value, Font font) {
        List<Cell> row = new ArrayList<>();
        row.add(new TextCell(Arrays.asList(key), State.NORMAL, font));
        row.add(new TextCell(Arrays.asList(value), State.NORMAL, font));
        return row;
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
        FontMetrics textMetrics = graphics.getFontMetrics(Style.getDefaultFont());
        int textWidth = width;
        TableBuilder tableBuilder = new TableBuilder();

        String healthValue = showCurrentHealth
                ? monster.health + "/" + monster.maxHealth
                : String.valueOf(monster.maxHealth);
        String manaValue = showCurrentHealth
                ? monster.mana + "/" + monster.maxMana
                : String.valueOf(monster.maxMana);

        String hitYou = "Can hit you " + toPercentString(AttackUtils.hitProb(monster.primaryAttack.plusToHit, player.getDefense())) + "% of the time";
        String youHit = "You can hit " + toPercentString(AttackUtils.hitProb(player.getPrimaryAttack().plusToHit, monster.defense)) + "% of the time (melee)";
        String bowHit = "You can hit " + toPercentString(AttackUtils.hitProb(player.getSecondaryAttack().plusToHit, monster.defense)) + "% of the time (bow)";

        List<String> descriptionLines = ImageUtils.wrapText(monster.description, textMetrics, textWidth);
        List<String> spellLines = monster.spells.isEmpty()
                ? Collections.emptyList()
                : ImageUtils.wrapText("Can cast " + TextUtils.formatList(getSpellNames(monster.spells)) + ".", textMetrics, textWidth);

        List<Cell> header = new ArrayList<>();
        header.add(new ImageCell(monster.image, State.NORMAL));
        header.add(new TextCell(Arrays.asList(TextUtils.singular(monster.name)), State.NORMAL, font));
        tableBuilder.addRow(header);
        String statsLine =
                "Str: " + monster.strength +
                        "  Dex: " + monster.dexterity +
                        "  Int: " + monster.intelligence +
                        "  Con: " + monster.constitution;

        for (String line : descriptionLines) {
            tableBuilder.addRow(getRow(line, "", font));
        }
        tableBuilder.addRow(getRow("", "", font));
        tableBuilder.addRow(getRow(statsLine, "", font));
        for (String line : spellLines) {
            tableBuilder.addRow(getRow(line, "", font));
        }
        tableBuilder.addRow(getRow("", "", font));
        tableBuilder.addRow(getRow("HP:        ", healthValue, font));
        tableBuilder.addRow(getRow("MP:        ", manaValue, font));
        tableBuilder.addRow(getRow("", "", font));
        tableBuilder.addRow(getRow("Exp:       ", "" + monster.experience, font));
        tableBuilder.addRow(getRow("Level:     ", "" + monster.level, font));
        tableBuilder.addRow(getRow("", "", font));
        tableBuilder.addRow(getRow("Attack:    ", "" + monster.primaryAttack, font));
        tableBuilder.addRow(getRow("Defense:   ", "" + monster.defense, font));
        tableBuilder.addRow(getRow("Speed:     ", "" + monster.speed, font));
        tableBuilder.addRow(getRow("", "", font));
        tableBuilder.addRow(getRow(hitYou, "", font));
        tableBuilder.addRow(getRow(youHit, "", font));
        if (player.hasBowEquipped()) {
            tableBuilder.addRow(getRow(bowHit, "", font));
        }

        tableBuilder.setColWidths(Arrays.asList(80, textWidth - 80));
        tableBuilder.setDrawHeaderLine(true);

        Table table = tableBuilder.build();
        table.draw(graphics, position.x, position.y);
    }
}
