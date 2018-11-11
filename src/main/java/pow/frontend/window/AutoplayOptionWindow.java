package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;

import java.awt.*;
import java.awt.event.KeyEvent;

public class AutoplayOptionWindow extends AbstractWindow {

    enum HumanControlSelection {
        PLAYER,
        PET,
        BOTH
    }

    private void setAutoplay(HumanControlSelection selection) {
        GameState gs = backend.getGameState();
        switch (selection) {
            case PLAYER:
                gs.party.player.setAutoplay(gs, false);
                gs.party.pet.setAutoplay(gs, true);
                break;
            case PET:
                gs.party.player.setAutoplay(gs, true);
                gs.party.pet.setAutoplay(gs, false);
                break;
            case BOTH:
                gs.party.player.setAutoplay(gs, false);
                gs.party.pet.setAutoplay(gs, false);
        }
    }

    public AutoplayOptionWindow(WindowDim dim, GameBackend backend, Frontend frontend) {
        super(dim, true, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case KeyEvent.VK_ESCAPE:
                frontend.close();
                break;
            case KeyEvent.VK_A:
                setAutoplay(HumanControlSelection.PLAYER);
                frontend.close();
                break;
            case KeyEvent.VK_B:
                setAutoplay(HumanControlSelection.PET);
                frontend.close();
                break;
            case KeyEvent.VK_C:
                setAutoplay(HumanControlSelection.BOTH);
                frontend.close();
                break;
        }
    }

    private static final int MARGIN = 10;
    private static final int FONT_SIZE = 12;
    private static final int TILE_SIZE = 32;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        GameState gs = backend.getGameState();

        int textOffset = (FONT_SIZE + TILE_SIZE) / 2;

        int y = MARGIN + FONT_SIZE;
        graphics.drawString("Who do you want to control?", MARGIN, y);
        y += FONT_SIZE;

        graphics.drawString("a) ", MARGIN, y + textOffset);
        graphics.drawString(gs.party.player.name, 70, y + textOffset);
        ImageController.drawTile(graphics, gs.party.player.image, MARGIN + 20, y);
        y += TILE_SIZE;

        graphics.drawString("b) ", MARGIN, y + textOffset);
        graphics.drawString(gs.party.pet.name, 70, y + textOffset);
        ImageController.drawTile(graphics, gs.party.pet.image, MARGIN + 20, y);
        y += TILE_SIZE;

        graphics.drawString("c) ", MARGIN, y + textOffset);
        graphics.drawString("both", 70 + TILE_SIZE, y + textOffset);
        ImageController.drawTile(graphics, gs.party.player.image, MARGIN + 20, y);
        ImageController.drawTile(graphics, gs.party.pet.image, MARGIN + 20 + TILE_SIZE, y);
    }
}
