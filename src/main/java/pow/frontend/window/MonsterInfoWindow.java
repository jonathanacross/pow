package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.action.AttackUtils;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.ImageUtils;
import pow.frontend.WindowDim;
import pow.frontend.utils.MonsterDisplay;
import pow.util.Point;
import pow.util.TextUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class MonsterInfoWindow extends AbstractWindow {

    private Actor actor;

    public MonsterInfoWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        this.actor = null;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @Override
    public void processKey(KeyEvent e) { }

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

        // actual drawing here
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        MonsterDisplay.drawMonsterInfo(
                graphics,
                actor,
                player,
                true,
                dim.width,
                new Point(0,0));

//        // figure out the description; it's a multi-line mess
//        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
//        int textWidth = dim.width - 3*MARGIN - TILE_SIZE;
//
//        graphics.setFont(font);
//        FontMetrics textMetrics = graphics.getFontMetrics(font);
//        List<String> descriptionLines = ImageUtils.wrapText(actor.description, textMetrics, textWidth);
//
//        List<String> lines = new ArrayList<>();
//        lines.add(TextUtils.format(actor.name, 1, false));
//        lines.addAll(descriptionLines);
//        lines.add("");
//        lines.add("Str:    " + actor.baseStats.strength);
//        lines.add("Dex:    " + actor.baseStats.dexterity);
//        lines.add("Int:    " + actor.baseStats.intelligence);
//        lines.add("Con:    " + actor.baseStats.constitution);
//        lines.add("");
//        lines.add("HP:     " + actor.getHealth() + "/" + actor.getMaxHealth());
//        lines.add("MP:     " + actor.getMana() + "/" + actor.getMaxMana());
//        lines.add("");
//        lines.add("Level:  " + actor.level);
//        lines.add("Attack: " + actor.getPrimaryAttack());
//        lines.add("Def:    " + actor.getDefense());
//        lines.add("Speed:  " + actor.getSpeed());
//        lines.add("Exp.:   " + actor.experience);
//        lines.add("");
//        lines.add("Can hit you " + toPercentString(AttackUtils.hitProb(actor.getPrimaryAttack().plusToHit, player.getDefense())) + "% of the time");
//        lines.add("You can hit " + toPercentString(AttackUtils.hitProb(player.getPrimaryAttack().plusToHit, actor.getDefense())) + "% of the time");
//
//
//        ImageController.drawTile(graphics, actor.image, MARGIN, MARGIN);
//
//        graphics.setColor(Color.WHITE);
//        for (int i = 0; i < lines.size(); i++) {
//            graphics.drawString(lines.get(i), TILE_SIZE + 2*MARGIN, MARGIN + (i+1)*FONT_SIZE);
//        }
    }
}
