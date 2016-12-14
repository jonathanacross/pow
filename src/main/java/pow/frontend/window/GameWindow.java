package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.command.FireRocket;
import pow.backend.command.Move;
import pow.backend.command.Save;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.Monster;
import pow.frontend.Frontend;
import pow.frontend.effect.GlyphLoc;
import pow.frontend.utils.ImageController;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GameWindow extends AbstractWindow {

    public GameWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
        setTileSize(ImageController.TILE_SIZE);
    }

    @Override
    public void processKey(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_L:
                backend.commandQueue.add(new Move(1, 0));
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_H:
                backend.commandQueue.add(new Move(-1, 0));
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_J:
                backend.commandQueue.add(new Move(0, 1));
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_K:
                backend.commandQueue.add(new Move(0, -1));
                break;
            case KeyEvent.VK_Y:
                backend.commandQueue.add(new Move(-1, -1));
                break;
            case KeyEvent.VK_U:
                backend.commandQueue.add(new Move(1, -1));
                break;
            case KeyEvent.VK_B:
                backend.commandQueue.add(new Move(-1, 1));
                break;
            case KeyEvent.VK_N:
                backend.commandQueue.add(new Move(1, 1));
                break;
            case KeyEvent.VK_F:
                backend.commandQueue.add(new FireRocket());
                break;
            case KeyEvent.VK_S:
                backend.commandQueue.add(new Save());
                break;
        }
    }

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

    private void drawTile(Graphics graphics, String tileName, int x, int y) {
        ImageController.drawTile(graphics, tileName, x * tileSize + windowShiftX, y * tileSize + windowShiftY);
    }

    private int tileSize;
    private int windowShiftX;
    private int windowShiftY;
    private int xRadius;
    private int yRadius;

    @Override
    public void drawContents(Graphics graphics) {
        GameState gs = backend.getGameState();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);

        int colMin = Math.max(0, gs.player.x - xRadius);
        int colMax = Math.min(gs.map.width - 1, gs.player.x + xRadius);
        int rowMin = Math.max(0, gs.player.y - yRadius);
        int rowMax = Math.min(gs.map.height - 1, gs.player.y + yRadius);

        int cameraDx = -gs.player.x + xRadius;
        int cameraDy = -gs.player.y + yRadius;

        Font f = new Font("Courier New", Font.PLAIN, this.tileSize);
        graphics.setFont(f);

        // draw the map
        for (int y = rowMin; y <= rowMax; y++) {
            for (int x = colMin; x <= colMax; x++) {
                DungeonSquare square = gs.map.map[x][y];
                drawTile(graphics, square.terrain.image, x + cameraDx, y + cameraDy);
                if (square.feature != null) {
                    drawTile(graphics, square.feature.image, x + cameraDx, y + cameraDy);
                }
            }
        }

        // draw monsters
        for (Monster monster : gs.map.monsters) {
            drawTile(graphics, monster.image, monster.x + cameraDx, monster.y + cameraDy);
        }

        // draw the player
        drawTile(graphics, gs.player.image, gs.player.x + cameraDx, gs.player.y + cameraDy);

        // draw any pets
        if (gs.pet != null) {
            drawTile(graphics, gs.pet.image, gs.pet.x + cameraDx, gs.pet.y + cameraDy);
        }

        // draw effects
        if (!frontend.getEffects().isEmpty()) {
            for (GlyphLoc glyphLoc : frontend.getEffects().get(0).render()) {
                drawTile(graphics, glyphLoc.getImageName(), glyphLoc.getX() + cameraDx, glyphLoc.getY() + cameraDy);
            }
        }
    }
}
