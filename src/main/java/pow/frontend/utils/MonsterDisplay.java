package pow.frontend.utils;

import pow.backend.action.AttackUtils;
import pow.backend.actors.Actor;
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
            Actor actor,
            Actor player, // needed only to show hit percentages
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
        List<String> descriptionLines = ImageUtils.wrapText(actor.description, textMetrics, textWidth);

        List<String> lines = new ArrayList<>();
        lines.add(TextUtils.singular(actor.name));
        lines.add("");
        lines.addAll(descriptionLines);
        lines.add("");
        lines.add("Str:    " + actor.baseStats.strength);
        lines.add("Dex:    " + actor.baseStats.dexterity);
        lines.add("Int:    " + actor.baseStats.intelligence);
        lines.add("Con:    " + actor.baseStats.constitution);
        lines.add("");
        if (showCurrentHealth) {
            lines.add("HP:     " + actor.getHealth() + "/" + actor.getMaxHealth());
            lines.add("MP:     " + actor.getMana() + "/" + actor.getMaxMana());
        } else {
            lines.add("HP:     " + actor.getMaxHealth());
            lines.add("MP:     " + actor.getMaxMana());
        }
        lines.add("");
        lines.add("Level:  " + actor.level);
        lines.add("Attack: " + actor.getPrimaryAttack());
        lines.add("Def:    " + actor.getDefense());
        lines.add("Speed:  " + actor.getSpeed());
        lines.add("Exp.:   " + actor.experience);
        lines.add("");
        lines.add("Can hit you " + toPercentString(AttackUtils.hitProb(actor.getPrimaryAttack().plusToHit, player.getDefense())) + "% of the time");
        lines.add("You can hit " + toPercentString(AttackUtils.hitProb(player.getPrimaryAttack().plusToHit, actor.getDefense())) + "% of the time");

        ImageController.drawTile(graphics, actor.image, position.x + margin, position.y + margin);

        graphics.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(lines.get(i), position.x + tileSize + 2*margin, position.y + margin + (i+1)*fontSize);
        }
    }
}
