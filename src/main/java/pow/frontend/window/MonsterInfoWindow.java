package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.action.Attack;
import pow.backend.actors.Actor;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.ImageUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class MonsterInfoWindow extends AbstractWindow {

    Actor actor;

    public MonsterInfoWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
        this.actor = null;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @Override
    public void processKey(KeyEvent e) {
    }

    final private int TILE_SIZE = 32;
    final private int MARGIN = 10;
    final private int FONT_SIZE = 12;

    private String toPercentString(double d) {
        return String.format("%1$,.1f", d * 100);
    }

    @Override
    public void drawContents(Graphics graphics) {
        if (actor == null) {
            return;
        }

        Actor player = backend.getGameState().player;

        // figure out the description; it's a multi-line mess
        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        int textWidth = width - 3*MARGIN - TILE_SIZE;

        graphics.setFont(font);
        FontMetrics textMetrics = graphics.getFontMetrics(font);
        List<String> descriptionLines = ImageUtils.wrapText(actor.description, textMetrics, textWidth);

        List<String> lines = new ArrayList<>();
        lines.add(actor.name);
        lines.addAll(descriptionLines);
        lines.add("");
        lines.add("HP:     " + actor.health + "/" + actor.maxHealth);
        lines.add("MP:     " + actor.mana + "/" + actor.maxMana);
        lines.add("");
        lines.add("Attack: " + actor.attack);
        lines.add("Def:    " + actor.defense);
        lines.add("Speed:  " + actor.speed);
        lines.add("Exp.:   " + actor.experience);
        lines.add("");
        lines.add("Can hit you " + toPercentString(Attack.hitProb(actor.attack.plusToHit, player.defense)) + "% of the time");
        lines.add("You can hit " + toPercentString(Attack.hitProb(player.attack.plusToHit, actor.defense)) + "% of the time");

        // actual drawing here
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        ImageController.drawTile(graphics, actor.image, MARGIN, MARGIN);

        graphics.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(lines.get(i), TILE_SIZE + 2*MARGIN, MARGIN + (i+1)*FONT_SIZE);
        }
    }
}
