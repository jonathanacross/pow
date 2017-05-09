package pow.frontend.utils;

import pow.backend.action.AttackUtils;
import pow.backend.actors.Player;
import pow.backend.actors.Knowledge;
import pow.util.Point;
import pow.util.TextUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MonsterDisplay {

    private static String toPercentString(double d) {
        return String.format("%1$,.1f", d * 100);
    }

    public static void drawMonsterInfo(
            Graphics graphics,
            Knowledge.MonsterSummary monster,
            Player player, // needed to calculate and show hit percentages
            boolean showCurrentHealth,
            int width,
            Point position
    ) {
        final int fontSize = 12;
        final int tileSize = 32;
        final int margin = 10;

        // figure out the description; it's a multi-line mess
        Font font = new Font("Courier", Font.PLAIN, fontSize);
        int textWidth = width - 3*margin - tileSize;

        graphics.setFont(font);
        FontMetrics textMetrics = graphics.getFontMetrics(font);
        List<String> descriptionLines = ImageUtils.wrapText(monster.description, textMetrics, textWidth);

        List<String> lines = new ArrayList<>();
        lines.add(TextUtils.singular(monster.name));
        lines.add("");
        lines.addAll(descriptionLines);
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
