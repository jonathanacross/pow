package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;

import java.awt.*;
import java.awt.event.KeyEvent;

// TODO: there's a fair amount of common drawing code here with GameWindow.  See if I can combine
public class MapWindow extends AbstractWindow {

    private int tileSize;
    private int windowShiftX;
    private int windowShiftY;
    private int xRadius;
    private int yRadius;

    private static int TILE_SIZE = 4;

    // Used to figure out how much we can show on the map.
    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;

        // compute how many rows/columns to show
        this.xRadius = (int) Math.ceil(0.5 * ((double) width / tileSize - 1));
        this.yRadius = (int) Math.ceil(0.5 * ((double) height / tileSize - 1));

        // how much to shift the tiles to display centered
        this.windowShiftX = (width - (2 * xRadius + 1) * tileSize) / 2;
        this.windowShiftY = (height - (2 * yRadius + 1) * tileSize) / 2;
    }

    public MapWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
        setTileSize(TILE_SIZE);
    }

    @Override
    public void processKey(KeyEvent e) {
    }

    private void drawTile(Graphics graphics, String tileName, int x, int y) {
        ImageController.drawTile(graphics, tileName, x * tileSize + windowShiftX, y * tileSize + windowShiftY, false, tileSize);
    }

    private void drawBlock(Graphics graphics, Color color, int x, int y) {
        graphics.setColor(color);
        graphics.fillRect(x*tileSize, y*tileSize, tileSize, tileSize);
    }

    private static final Color WALL_COLOR = Color.DARK_GRAY;
    private static final Color FLOOR_COLOR = Color.GRAY;
    private static final Color STAIR_COLOR = Color.WHITE;
    private static final Color MONSTER_COLOR = Color.RED;
    private static final Color PLAYER_COLOR = Color.YELLOW;
    private static final Color PET_COLOR = new Color(66, 134, 244);


    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        GameState gs = backend.getGameState();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);

        int camCenterX = Math.min(Math.max(xRadius, gs.player.loc.x), gs.map.width - 1 - xRadius);
        int camCenterY = Math.min(Math.max(yRadius, gs.player.loc.y), gs.map.height - 1 - yRadius);

        int colMin = Math.max(0, camCenterX - xRadius);
        int colMax = Math.min(gs.map.width - 1, camCenterX + xRadius);
        int rowMin = Math.max(0, camCenterY - xRadius);
        int rowMax = Math.min(gs.map.height - 1, camCenterY + xRadius);

        int cameraDx = -(colMin + colMax) / 2 + xRadius;
        int cameraDy = -(rowMin + rowMax) / 2 + yRadius;

        Font f = new Font("Courier New", Font.PLAIN, this.tileSize);
        graphics.setFont(f);

        // draw the map
        for (int y = rowMin; y <= rowMax; y++) {
            for (int x = colMin; x <= colMax; x++) {
                // only draw squares we've seen
                if (! gs.map.seen[x][y]) {
                    continue;
                }

                DungeonSquare square = gs.map.map[x][y];
                // TODO: add flags to features so this isn't hardcoded; it's probably
                // better to draw only blocks for the map, no tiles.
//                Color c = square.terrain.flags.blockGround ? WALL_COLOR : FLOOR_COLOR;
//                drawBlock(graphics, c, x + cameraDx, y + cameraDy);
                drawTile(graphics, square.terrain.image, x + cameraDx, y + cameraDy);
                if (square.feature != null) {
//                    drawBlock(graphics, STAIR_COLOR, x + cameraDx, y + cameraDy);
                    drawTile(graphics, square.feature.image, x + cameraDx, y + cameraDy);
                }
            }
        }

        // draw monsters, player, pets
        for (Actor actor : gs.map.actors) {
            if (! gs.player.canSee(gs, actor.loc)) {
                continue;
            }

            if (actor == gs.player) {
                drawBlock(graphics, PLAYER_COLOR, actor.loc.x + cameraDx, actor.loc.y + cameraDy);
            } else if (actor == gs.pet) {
                drawBlock(graphics, PET_COLOR, actor.loc.x + cameraDx, actor.loc.y + cameraDy);
            } else {
                drawBlock(graphics, MONSTER_COLOR, actor.loc.x + cameraDx, actor.loc.y + cameraDy);
            }
        }
    }
}
