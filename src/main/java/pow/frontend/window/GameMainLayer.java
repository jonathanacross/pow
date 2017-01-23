package pow.frontend.window;

import pow.backend.GameState;
import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.ItemList;
import pow.frontend.effect.GlyphLoc;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class GameMainLayer extends AbstractWindow {

    private GameWindow parent;

    public GameMainLayer(GameWindow parent) {
        super(parent.x, parent.y, parent.width, parent.height, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
    }

    private void tryPickup(GameState gs) {
        Point playerLoc = gs.player.loc;
        ItemList items = gs.world.currentMap.map[playerLoc.x][playerLoc.y].items;
        // TODO: Currently, we rely on the backend to tell what is valid.  This
        // is guaranteed to work correctly, but may be a little frustrating,
        // e.g., if there are multiple items, but only one item that the user
        // can pick up., Or if the player is completely full and there are
        // multiple items..
        if (items.size() == 1) {
            // no ambiguity, just pick up the one item
            backend.tellPlayer(new PickUp(gs.player, 0, items.size()));
        }
        else {
            // ask the user to pick which item
            frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Pick up which item?",
                        items.items, (DungeonItem item) -> false,
                        (int itemNum) -> {backend.tellPlayer(new PickUp(gs.player, itemNum, items.size())); }));
        }
    }

    private void showInventory(GameState gs) {
        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Inventory:",
                        gs.player.inventory.items, (DungeonItem item) -> false, (int x) -> {
                }));
    }

    private void tryDrop(GameState gs) {
        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Drop which item?",
                        gs.player.inventory.items, (DungeonItem item) -> false,
                        (int itemNum) -> {
                            backend.tellPlayer(new Drop(gs.player, itemNum, gs.player.inventory.items.get(itemNum).count));
                        }));
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
            case UP_STAIRS: backend.tellPlayer(new TakeStairs(gs.player, true)); break;
            case DOWN_STAIRS: backend.tellPlayer(new TakeStairs(gs.player, false)); break;
            case REST: backend.tellPlayer(new Move(gs.player, 0, 0)); break;
            case FIRE: backend.tellPlayer(new FireRocket(gs.player)); break;
            case SAVE: backend.tellPlayer(new Save()); break;
            case LOOK: startLooking(); break;
            case INVENTORY: showInventory(gs); break;
            case DROP: tryDrop(gs); break;
            case GET: tryPickup(gs); break;
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
                DungeonSquare square = gs.world.currentMap.map[x][y];
                if (!gs.world.currentMap.map[x][y].seen) {
                    continue;
                }
                mapView.drawTile(graphics, square.terrain.image, x, y);
                if (square.feature != null) {
                    mapView.drawTile(graphics, square.feature.image, x, y);
                }
                if (square.items != null) {
                    for (DungeonItem item: square.items.items) {
                        mapView.drawTile(graphics, item.image, x, y);
                    }
                }
            }
        }

        // draw monsters, player, pets
        for (Actor actor : gs.world.currentMap.actors) {
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
                if (!gs.world.currentMap.map[x][y].seen) {
                    continue;
                }
                int maxDarkness = 220;
                double darknessD = 1.0 - (gs.world.currentMap.map[x][y].brightness / (double) gs.world.currentMap.MAX_BRIGHTNESS);
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
