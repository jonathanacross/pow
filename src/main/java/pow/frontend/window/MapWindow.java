package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;
import pow.frontend.WindowDim;

import java.awt.*;
import java.awt.event.KeyEvent;

public class MapWindow extends AbstractWindow {

    final private int MARGIN = 10;
    final private int FONT_SIZE = 14;
    private static final int TILE_SIZE = 4;

    private static final Color MONSTER_COLOR = Color.MAGENTA;
    private static final Color PLAYER_COLOR = Color.YELLOW;
    private static final Color PET_COLOR = new Color(153, 192, 255);

    public MapWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
    }

    @Override
    public void drawContents(Graphics graphics) {

        GameState gs = backend.getGameState();
        MapView mapView = new MapView(dim.width, dim.height, TILE_SIZE, backend.getGameState());

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        // draw the map
        for (int y = mapView.rowMin; y <= mapView.rowMax; y++) {
            for (int x = mapView.colMin; x <= mapView.colMax; x++) {
                // only draw squares we've seen
                if (! gs.getCurrentMap().map[x][y].seen) {
                    continue;
                }

                DungeonSquare square = gs.getCurrentMap().map[x][y];
                if (square.feature != null && gs.party.player.canSeeFeature(square.feature)) {
                    mapView.drawTile(graphics, square.feature.image, x, y, ImageController.DrawMode.COLOR_BLOCK);
                } else {
                    mapView.drawTile(graphics, square.terrain.image, x, y, ImageController.DrawMode.COLOR_BLOCK);
                }
            }
        }

        // draw monsters, player, pets
        for (Actor actor : gs.getCurrentMap().actors) {
            if (! gs.party.player.canSeeLocation(gs, actor.loc)) {
                continue;
            }

            if (! gs.party.player.canSeeActor(actor)) {
                continue;
            }

            if (actor == gs.party.player) {
                mapView.drawBlock(graphics, PLAYER_COLOR, actor.loc.x, actor.loc.y);
            } else if (actor == gs.party.pet) {
                mapView.drawBlock(graphics, PET_COLOR, actor.loc.x, actor.loc.y);
            } else {
                mapView.drawBlock(graphics, MONSTER_COLOR, actor.loc.x, actor.loc.y);
            }
        }

        // show what map we're on
        Font font = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(font);
        // cheap way of drawing an outline
        graphics.setColor(Color.BLACK);
        graphics.drawString(gs.getCurrentMap().name, MARGIN - 1, MARGIN + FONT_SIZE - 1);
        graphics.drawString(gs.getCurrentMap().name, MARGIN - 1, MARGIN + FONT_SIZE + 1);
        graphics.drawString(gs.getCurrentMap().name, MARGIN + 1, MARGIN + FONT_SIZE - 1);
        graphics.drawString(gs.getCurrentMap().name, MARGIN + 1, MARGIN + FONT_SIZE + 1);
        graphics.setColor(Color.WHITE);
        graphics.drawString(gs.getCurrentMap().name, MARGIN, MARGIN + FONT_SIZE);
    }
}
