package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.table.EmptyCell;
import pow.frontend.utils.table.ImageCell;
import pow.frontend.utils.table.Table;
import pow.frontend.utils.table.TableBuilder;
import pow.frontend.utils.table.TextCell;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;

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
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Font font = Style.getDefaultFont();
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);

        GameState gs = backend.getGameState();

        int y = Style.SMALL_MARGIN + Style.getFontSize();
        graphics.drawString("Who do you want to control?", Style.SMALL_MARGIN, y);
        y += Style.getFontSize();

        TableBuilder tableBuilder = new TableBuilder();

        tableBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("a)"), TextCell.Style.NORMAL, font),
                new ImageCell(gs.party.player.image, false),
                new TextCell(Arrays.asList(gs.party.player.name), TextCell.Style.NORMAL, font),
                new EmptyCell()
                ));
        tableBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("b)"), TextCell.Style.NORMAL, font),
                new ImageCell(gs.party.pet.image, false),
                new TextCell(Arrays.asList(gs.party.pet.name), TextCell.Style.NORMAL, font),
                new EmptyCell()
        ));
        tableBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("c)"), TextCell.Style.NORMAL, font),
                new ImageCell(gs.party.player.image, false),
                new ImageCell(gs.party.pet.image, false),
                new TextCell(Arrays.asList("both"), TextCell.Style.NORMAL, font)
        ));

        tableBuilder.setColWidths(Arrays.asList(20, Style.TILE_SIZE + Style.SMALL_MARGIN, Style.TILE_SIZE + Style.SMALL_MARGIN, 40));
        Table table = tableBuilder.build();
        table.draw(graphics, Style.SMALL_MARGIN, y);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Press [esc] to cancel.", Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }
}
