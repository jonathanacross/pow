package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.action.Attack;
import pow.backend.actors.Actor;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;
import pow.util.DebugLogger;

import java.awt.Color;
import java.awt.Font;
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

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        ImageController.drawTile(graphics, actor.image, MARGIN, MARGIN);

        Actor player = backend.getGameState().player;

        List<String> lines = new ArrayList<>();
        lines.add(actor.name);
        lines.add(actor.description);
        lines.add("");
        lines.add("HP:     " + actor.health + "/" + actor.maxHealth);
        lines.add("MP:     ");
        lines.add("");
        lines.add("Attack: " + actor.attackDamage);
        lines.add("Dex:    " + actor.dexterity);
        lines.add("Def:    " + actor.defense);
        lines.add("");
        lines.add("Can hit you " + toPercentString(Attack.hitProb(actor.dexterity, player.defense)) + "% of the time");
        lines.add("You can hit " + toPercentString(Attack.hitProb(player.dexterity, actor.defense)) + "% of the time");

        Font f = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(lines.get(i), TILE_SIZE + 2*MARGIN, MARGIN + (i+1)*FONT_SIZE);
        }

    }
}
