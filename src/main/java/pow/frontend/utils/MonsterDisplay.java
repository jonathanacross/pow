package pow.frontend.utils;

import pow.backend.SpellParams;
import pow.backend.utils.AttackUtils;
import pow.backend.actors.Player;
import pow.backend.actors.Knowledge;
import pow.frontend.Style;
import pow.util.Point;
import pow.util.TextUtils;

import java.awt.*;
import java.util.ArrayList;
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

    public static void drawMonsterInfo(
            Graphics graphics,
            Knowledge.MonsterSummary monster,
            Player player, // needed to calculate and show hit percentages
            boolean showCurrentHealth,
            int width,
            Point position
    ) {
        final int fontSize = Style.FONT_SIZE;
        final int tileSize = Style.TILE_SIZE;
        final int margin = Style.SMALL_MARGIN;

        // figure out the description; it's a multi-line mess
        int textWidth = width - 3*margin - tileSize;

        graphics.setFont(Style.getDefaultFont());
        FontMetrics textMetrics = graphics.getFontMetrics(Style.getDefaultFont());
        List<String> descriptionLines = ImageUtils.wrapText(monster.description, textMetrics, textWidth);
        List<String> spellLines = monster.spells.isEmpty()
                ? Collections.emptyList()
                : ImageUtils.wrapText("Can cast " + TextUtils.formatList(getSpellNames(monster.spells)) + ".", textMetrics, textWidth);

        List<String> lines = new ArrayList<>();
        lines.add(TextUtils.singular(monster.name));
        lines.add("");
        lines.addAll(descriptionLines);
        lines.addAll(spellLines);
        lines.add("");
        lines.add("Str:    " + monster.strength);
        lines.add("Dex:    " + monster.dexterity);
        lines.add("Int:    " + monster.intelligence);
        lines.add("Con:    " + monster.constitution);
        lines.add("");
        if (showCurrentHealth) {
            lines.add("HP:     " + monster.health + "/" + monster.maxHealth);
            lines.add("MP:     " + monster.mana + "/" + monster.maxMana);
        } else {
            lines.add("HP:     " + monster.maxHealth);
            lines.add("MP:     " + monster.maxMana);
        }
        lines.add("");
        lines.add("Level:  " + monster.level);
        lines.add("Attack: " + monster.primaryAttack);
        lines.add("Def:    " + monster.defense);
        lines.add("Speed:  " + monster.speed);
        lines.add("Exp.:   " + monster.experience);
        lines.add("");
        lines.add("Can hit you " + toPercentString(AttackUtils.hitProb(monster.primaryAttack.plusToHit, player.getDefense())) + "% of the time");
        lines.add("You can hit " + toPercentString(AttackUtils.hitProb(player.getPrimaryAttack().plusToHit, monster.defense)) + "% of the time (melee)");
        if (player.hasBowEquipped()) {
            lines.add("You can hit " + toPercentString(AttackUtils.hitProb(player.getSecondaryAttack().plusToHit, monster.defense)) + "% of the time (bow)");
        }

        ImageController.drawTile(graphics, monster.image, position.x + margin, position.y + margin);

        graphics.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(lines.get(i), position.x + tileSize + 2*margin, position.y + margin + (i+1)*fontSize);
        }
    }
}
