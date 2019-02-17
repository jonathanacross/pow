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

    private static List<Widget> getRow(String key, String value, Font font) {
        List<Widget> row = new ArrayList<>();
        row.add(new TextBox(Arrays.asList(key), State.NORMAL, font));
        row.add(new TextBox(Arrays.asList(value), State.NORMAL, font));
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
        Table table = new Table();

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

        List<Widget> header = new ArrayList<>();
        header.add(new Tile(monster.image, State.NORMAL));
        header.add(new TextBox(Arrays.asList(TextUtils.singular(monster.name)), State.NORMAL, font));
        table.addRow(header);
        String statsLine =
                "Str: " + monster.strength +
                        "  Dex: " + monster.dexterity +
                        "  Int: " + monster.intelligence +
                        "  Con: " + monster.constitution;

        for (String line : descriptionLines) {
            table.addRow(getRow(line, "", font));
        }
        table.addRow(getRow("", "", font));
        table.addRow(getRow(statsLine, "", font));
        for (String line : spellLines) {
            table.addRow(getRow(line, "", font));
        }
        table.addRow(getRow("", "", font));
        table.addRow(getRow("HP:        ", healthValue, font));
        table.addRow(getRow("MP:        ", manaValue, font));
        table.addRow(getRow("", "", font));
        table.addRow(getRow("Exp:       ", "" + monster.experience, font));
        table.addRow(getRow("Level:     ", "" + monster.level, font));
        table.addRow(getRow("", "", font));
        table.addRow(getRow("Attack:    ", "" + monster.primaryAttack, font));
        table.addRow(getRow("Defense:   ", "" + monster.defense, font));
        table.addRow(getRow("Speed:     ", "" + monster.speed, font));
        table.addRow(getRow("", "", font));
        table.addRow(getRow(hitYou, "", font));
        table.addRow(getRow(youHit, "", font));
        if (player.hasBowEquipped()) {
            table.addRow(getRow(bowHit, "", font));
        }

        table.setColWidths(Arrays.asList(80, textWidth - 80));
        table.setDrawHeaderLine(true);
        table.autosize();

        table.draw(graphics, position.x, position.y);
    }
}
