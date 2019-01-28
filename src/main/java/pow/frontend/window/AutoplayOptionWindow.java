package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;

import java.awt.*;
import java.awt.event.KeyEvent;

public class AutoplayOptionWindow extends AbstractWindow {

    enum HumanControlSelection {
        NEITHER,
        PLAYER,
        PET,
        BOTH
    }

    private void setAutoplay(HumanControlSelection selection) {
        GameState gs = backend.getGameState();
        switch (selection) {
            case NEITHER:
                gs.party.player.setAutoplay(gs, true);
                gs.party.pet.setAutoplay(gs, true);
                System.out.println("full autoplay.");
                break;
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
                break;
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
            case KeyEvent.VK_Z: // hidden option
                setAutoplay(HumanControlSelection.NEITHER);
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

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);

        GameState gs = backend.getGameState();

        int textOffset = (Style.FONT_SIZE + Style.TILE_SIZE) / 2;

        int y = Style.SMALL_MARGIN + Style.FONT_SIZE;
        graphics.drawString("Who do you want to control?", Style.SMALL_MARGIN, y);
        y += Style.FONT_SIZE;

        graphics.drawString("a) ", Style.SMALL_MARGIN, y + textOffset);
        graphics.drawString(gs.party.player.name, 70, y + textOffset);
        ImageController.drawTile(graphics, gs.party.player.image, Style.SMALL_MARGIN + 20, y);
        y += Style.TILE_SIZE;

        graphics.drawString("b) ", Style.SMALL_MARGIN, y + textOffset);
        graphics.drawString(gs.party.pet.name, 70, y + textOffset);
        ImageController.drawTile(graphics, gs.party.pet.image, Style.SMALL_MARGIN + 20, y);
        y += Style.TILE_SIZE;

        graphics.drawString("c) ", Style.SMALL_MARGIN, y + textOffset);
        graphics.drawString("both", 70 + Style.TILE_SIZE, y + textOffset);
        ImageController.drawTile(graphics, gs.party.player.image, Style.SMALL_MARGIN + 20, y);
        ImageController.drawTile(graphics, gs.party.pet.image, Style.SMALL_MARGIN + 20 + Style.TILE_SIZE, y);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Press [esc] to cancel.", Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }
}
