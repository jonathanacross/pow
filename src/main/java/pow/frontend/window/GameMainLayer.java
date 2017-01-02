package pow.frontend.window;

import pow.backend.GameState;
import pow.backend.action.FireRocket;
import pow.backend.action.Move;
import pow.backend.action.Save;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.frontend.effect.GlyphLoc;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

public class GameMainLayer extends AbstractWindow {

    private GameWindow parent;

    public GameMainLayer(GameWindow parent) {
        super(parent.x, parent.y, parent.width, parent.height, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
    }

    @Override
    public void processKey(KeyEvent e) {
        GameState gs = backend.getGameState();

        KeyInput input = KeyUtils.getKeyInput(e);
        switch (input) {
            case EAST: backend.tellPlayer(new Move(gs.player, 1, 0)); break;
            case WEST: backend.tellPlayer(new Move(gs.player, -1, 0)); break;
            case SOUTH: backend.tellPlayer(new Move(gs.player, 0, 1)); break;
            case NORTH: backend.tellPlayer(new Move(gs.player, 0, -1)); break;
            case NORTH_WEST: backend.tellPlayer(new Move(gs.player, -1, -1)); break;
            case NORTH_EAST: backend.tellPlayer(new Move(gs.player, 1, -1)); break;
            case SOUTH_WEST: backend.tellPlayer(new Move(gs.player, -1, 1)); break;
            case SOUTH_EAST: backend.tellPlayer(new Move(gs.player, 1, 1)); break;
            case REST: backend.tellPlayer(new Move(gs.player, 0, 0)); break;
            case FIRE: backend.tellPlayer(new FireRocket(gs.player)); break;
            case SAVE: backend.tellPlayer(new Save()); break;
            case LOOK: startLooking(); break;
            case PLAYER_INFO: frontend.open(frontend.playerInfoWindow); break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        GameState gs = backend.getGameState();
        MapView mapView = new MapView(width, height, ImageController.TILE_SIZE, backend.getGameState());

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        // draw the map
        for (int y = mapView.rowMin; y <= mapView.rowMax; y++) {
            for (int x = mapView.colMin; x <= mapView.colMax; x++) {
                DungeonSquare square = gs.map.map[x][y];
                if (!gs.map.map[x][y].seen) {
                    continue;
                }
//                if (!gs.player.canSee(gs, new Point(x,y))) {
//                    continue;
//                }
                mapView.drawTile(graphics, square.terrain.image, x, y);
                if (square.feature != null) {
                    mapView.drawTile(graphics, square.feature.image, x, y);
                }
            }
        }

        // draw monsters, player, pets
        for (Actor actor : gs.map.actors) {
            if (gs.player.canSee(gs, actor.loc)) {
                mapView.drawTile(graphics, actor.image, actor.loc.x, actor.loc.y);
            }
        }

        // draw effects
        if (!frontend.getEffects().isEmpty()) {
            for (GlyphLoc glyphLoc : frontend.getEffects().get(0).render()) {
                if (gs.player.canSee(gs, glyphLoc.loc)) {
                    mapView.drawTile(graphics, glyphLoc.imageName, glyphLoc.loc.x, glyphLoc.loc.y);
                }
            }
        }

        // add shadow
        for (int y = mapView.rowMin; y <= mapView.rowMax; y++) {
            for (int x = mapView.colMin; x <= mapView.colMax; x++) {
                if (!gs.map.map[x][y].seen) {
                    continue;
                }
                int maxDarkness = 220;
                double darknessD = 1.0 - (gs.map.map[x][y].brightness / (double) gs.map.MAX_BRIGHTNESS);
                // Assign max darkness if we can't see it; alternatively, we could paint
                // in gray, or something.  Or just not show it at all?
                if (!gs.player.canSee(gs, new Point(x,y))) {
                    darknessD = 1;
                }
                int darkness = (int) Math.round(maxDarkness * darknessD);
                mapView.makeShadow(graphics, x, y, darkness);
            }
        }
    }

    private void startLooking() {
        parent.addLayer(new GameTargetLayer(parent));
    }
}
