package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;

import java.awt.*;
import java.awt.event.KeyEvent;

public class MapWindow extends AbstractWindow {

    private static final int TILE_SIZE = 4;

    private static final Color MONSTER_COLOR = Color.MAGENTA;
    private static final Color PLAYER_COLOR = Color.YELLOW;
    private static final Color PET_COLOR = new Color(153, 192, 255);

    public MapWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
    }

    @Override
    public void drawContents(Graphics graphics) {

        GameState gs = backend.getGameState();
        MapView mapView = new MapView(width, height, TILE_SIZE, backend.getGameState());

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        // draw the map
        for (int y = mapView.rowMin; y <= mapView.rowMax; y++) {
            for (int x = mapView.colMin; x <= mapView.colMax; x++) {
                // only draw squares we've seen
                if (! gs.world.currentMap.map[x][y].seen) {
                    continue;
                }

                DungeonSquare square = gs.world.currentMap.map[x][y];
                mapView.drawBlock(graphics, ImageController.getColor(square.terrain.image), x, y);
//                mapView.drawTile(graphics, square.terrain.image, x, y);
                if (square.feature != null) {
                    mapView.drawBlock(graphics, ImageController.getColor(square.feature.image), x, y);
//                    mapView.drawTile(graphics, square.feature.image, x, y);
                }
            }
        }

        // draw monsters, player, pets
        for (Actor actor : gs.world.currentMap.actors) {
            if (! gs.player.canSee(gs, actor.loc)) {
                continue;
            }

            if (actor == gs.player) {
                mapView.drawBlock(graphics, PLAYER_COLOR, actor.loc.x, actor.loc.y);
            } else if (actor == gs.pet) {
                mapView.drawBlock(graphics, PET_COLOR, actor.loc.x, actor.loc.y);
            } else {
                mapView.drawBlock(graphics, MONSTER_COLOR, actor.loc.x, actor.loc.y);
            }
        }
    }
}
