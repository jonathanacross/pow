package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;

public class StatusWindow extends AbstractWindow {

    public StatusWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
    }

    final private int BAR_WIDTH = 130;

    private Color darkenColor(Color orig, double percent) {
        int r = (int) Math.round(orig.getRed() * percent);
        int g = (int) Math.round(orig.getGreen() * percent);
        int b = (int) Math.round(orig.getBlue() * percent);
        return new Color(r,g,b);
    }

    private void drawBar(Graphics graphics, int x, int y, int width, int height, Color c, int curr, int max) {
        int filledWidth = (int) Math.round(width * (double) curr / max);

        Color saveColor = graphics.getColor();

        Color empty = darkenColor(c, 0.2);
        Color full = darkenColor(c, 0.7);

        graphics.setColor(empty);
        graphics.fillRect(x, y, width, height);

        graphics.setColor(full);
        graphics.fillRect(x, y, filledWidth, height);
        graphics.drawRect(x, y, width, height);
        graphics.setColor(saveColor);
    }

    private void drawActorSummary(Graphics graphics, Actor a, int x, int y, boolean showExact, boolean selected) {
        int textX = x + Style.TILE_SIZE + Style.SMALL_MARGIN;

        // draw bars
        Color healthColor = Color.RED;
        Color manaColor = Color.BLUE;
        drawBar(graphics, textX, y + Style.FONT_SIZE, BAR_WIDTH, Style.FONT_SIZE-1, healthColor, a.getHealth(), a.getMaxHealth());
        if (a.getMaxMana() > 0) {
            drawBar(graphics, textX, y + 2 * Style.FONT_SIZE, BAR_WIDTH, Style.FONT_SIZE - 1, manaColor, a.getMana(), a.getMaxMana());
        }

        // draw character
        ImageController.drawTile(graphics, a.image, x, y);
        if (selected) {
            graphics.setColor(Color.YELLOW);
            graphics.drawRect(x, y, Style.TILE_SIZE - 1, Style.TILE_SIZE - 1);
        }

        // draw the text
        graphics.setColor(Color.WHITE);
        graphics.drawString(TextUtils.format(a.name, 1, false), textX, y + Style.FONT_SIZE);
        if (showExact) {
            graphics.drawString("HP:" + a.getHealth() + "/" + a.getMaxHealth(), textX, y + 2*Style.FONT_SIZE);
        }
        if (showExact && a.getMaxMana() > 0) {
            graphics.drawString("MP:" + a.getMana() + "/" + a.getMaxMana(), textX, y + 3*Style.FONT_SIZE);
        }

    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);
        GameState gs = backend.getGameState();

        int y = 10;
        int textX = Style.TILE_SIZE + 2*Style.SMALL_MARGIN;
        boolean selectPlayer = gs.party.selectedActor == gs.party.player;
        boolean selectPet = gs.party.selectedActor == gs.party.pet;
        drawActorSummary(graphics, gs.party.player, Style.SMALL_MARGIN, y, true, selectPlayer); y += 4*Style.FONT_SIZE;
        graphics.drawString("Exp:       " + gs.party.player.experience, textX, y); y += Style.FONT_SIZE;
        graphics.drawString("Exp next:  " + gs.party.player.getExpToNextLevel(), textX, y); y += Style.FONT_SIZE;
        graphics.drawString("Level:     " + gs.party.player.level, textX, y); y += Style.FONT_SIZE;
        graphics.drawString("Gold:      " + gs.party.player.gold, textX, y); y += Style.FONT_SIZE;

        if (gs.party.pet != null) {
            y += 5;
            drawActorSummary(graphics, gs.party.pet, Style.SMALL_MARGIN, y, true, selectPet); y += 4*Style.FONT_SIZE;
            graphics.drawString("Exp:       " + gs.party.pet.experience, textX, y); y += Style.FONT_SIZE;
            graphics.drawString("Exp next:  " + gs.party.pet.getExpToNextLevel(), textX, y); y += Style.FONT_SIZE;
            graphics.drawString("Level:     " + gs.party.pet.level, textX, y); y += Style.FONT_SIZE;
        }

        y += 5;
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawLine(Style.SMALL_MARGIN, y, dim.width - Style.SMALL_MARGIN, y);
        graphics.setColor(Color.WHITE);
        y += Style.FONT_SIZE;

        for (Actor a: gs.getCurrentMap().actors) {
            if (gs.party.containsActor(a)) continue;
            if (!gs.party.player.canSeeLocation(gs, a.loc)) continue;
            drawActorSummary(graphics, a, Style.SMALL_MARGIN, y, false, false); y += 40;
        }
    }
}
