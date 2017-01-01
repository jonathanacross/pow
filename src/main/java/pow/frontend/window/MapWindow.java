package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.frontend.Frontend;

import java.awt.*;
import java.awt.event.KeyEvent;

public class MapWindow extends AbstractWindow {

    private static final int TILE_SIZE = 4;

    private static final Color WALL_COLOR = Color.DARK_GRAY;
    private static final Color FLOOR_COLOR = Color.GRAY;
    private static final Color STAIR_COLOR = Color.WHITE;
    private static final Color MONSTER_COLOR = Color.RED;
    private static final Color PLAYER_COLOR = Color.YELLOW;
    private static final Color PET_COLOR = new Color(66, 134, 244);

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
                if (! gs.map.map[x][y].seen) {
                    continue;
                }

                DungeonSquare square = gs.map.map[x][y];
                // TODO: add flags to features so this isn't hardcoded; it's probably
                // better to draw only blocks for the map, no tiles.
//                Color c = square.terrain.flags.blockGround ? WALL_COLOR : FLOOR_COLOR;
//                drawBlock(graphics, c, x, y);
                mapView.drawTile(graphics, square.terrain.image, x, y);
                if (square.feature != null) {
//                    drawBlock(graphics, STAIR_COLOR, x, y);
                    mapView.drawTile(graphics, square.feature.image, x, y);
                }
            }
        }

        // draw monsters, player, pets
        for (Actor actor : gs.map.actors) {
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
