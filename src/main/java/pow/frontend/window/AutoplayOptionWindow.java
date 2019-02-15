package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.table.*;

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

    private Table layoutTable;

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

    public AutoplayOptionWindow(GameBackend backend, Frontend frontend) {
        super(new WindowDim(0, 0, 0, 0), true, backend, frontend);

        this.layoutTable = getLayoutTable();
        this.resize(frontend.layout.center(layoutTable.getWidth() + 2*Style.MARGIN,
                layoutTable.getHeight() + 2*Style.MARGIN));
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

    private Table getLayoutTable() {
        Font font = Style.getDefaultFont();
        GameState gs = backend.getGameState();

        // build the inner list of options
        TableBuilder listBuilder = new TableBuilder();
        listBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("a)"), State.NORMAL, font),
                new ImageCell(gs.party.player.image, State.NORMAL),
                new EmptyCell(),
                new TextCell(Arrays.asList(gs.party.player.name), State.NORMAL, font)
        ));
        listBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("b)"), State.NORMAL, font),
                new ImageCell(gs.party.pet.image, State.NORMAL),
                new EmptyCell(),
                new TextCell(Arrays.asList(gs.party.pet.name), State.NORMAL, font)
        ));
        listBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("c)"), State.NORMAL, font),
                new ImageCell(gs.party.player.image, State.NORMAL),
                new ImageCell(gs.party.pet.image, State.NORMAL),
                new TextCell(Arrays.asList("both"), State.NORMAL, font)
        ));
        listBuilder.setHSpacing(Style.MARGIN);
        Table list = listBuilder.build();

        // build the outer layout
        TableBuilder tableBuilder = new TableBuilder();
        tableBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("Who do you want to control?"), State.NORMAL, font)
        ));
        tableBuilder.addRow(Arrays.asList(
                list
        ));
        tableBuilder.addRow(Arrays.asList(
                new TextCell(Arrays.asList("Press [esc] to cancel."), State.NORMAL, font)
        ));
        tableBuilder.setVSpacing(Style.MARGIN);

        return tableBuilder.build();
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        layoutTable.draw(graphics, Style.MARGIN, Style.MARGIN);
    }
}
