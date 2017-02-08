package pow.frontend.window;

import pow.backend.GameState;
import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonFeature;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.ItemList;
import pow.backend.dungeon.gen.FeatureData;
import pow.frontend.effect.GlyphLoc;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.frontend.utils.Targeting;
import pow.util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Function;

public class GameMainLayer extends AbstractWindow {

    private GameWindow parent;

    public GameMainLayer(GameWindow parent) {
        super(parent.dim, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
    }

    private void tryPickup(GameState gs) {
        Point playerLoc = gs.player.loc;
        ItemList items = gs.getCurrentMap().map[playerLoc.x][playerLoc.y].items;

        if (items.size() == 0) {
            backend.logMessage("Nothing here to pick up.");
        } else if (items.size() == 1) {
            // no ambiguity, just pick up the one item
            backend.tellPlayer(new PickUp(gs.player, 0, items.items.get(0).count));
        } else {
            // ask the user to pick which item
            frontend.open(
                    new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Pick up which item?",
                            items.items, (DungeonItem item) -> true,
                            (int itemNum) -> {
                                backend.tellPlayer(new PickUp(gs.player, itemNum, items.items.get(itemNum).count));
                            }));
        }
    }

    private void showInventory(GameState gs) {
        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Inventory:",
                        gs.player.inventory.items, (DungeonItem item) -> true, (int x) -> {
                }));
    }

    private int countLegalItems(List<DungeonItem> items, Function<DungeonItem, Boolean> isLegal) {
        int numLegal = 0;
        for (DungeonItem item : items) {
            if (isLegal.apply(item)) {
                numLegal++;
            }
        }
        return numLegal;
    }

    private void tryDrop(GameState gs) {
        Function<DungeonItem, Boolean> droppable = (DungeonItem item) -> true;
        if (countLegalItems(gs.player.inventory.items, droppable) == 0) {
            backend.logMessage("You have nothing to drop.");
            return;
        }

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Drop which item?",
                        gs.player.inventory.items, droppable,
                        (int itemNum) -> {
                            backend.tellPlayer(new Drop(gs.player, itemNum, gs.player.inventory.items.get(itemNum).count));
                        }));
    }

    private void tryQuaff(GameState gs) {
        Function<DungeonItem, Boolean> quaffable = (DungeonItem item) -> item.flags.potion;
        if (countLegalItems(gs.player.inventory.items, quaffable) == 0) {
            backend.logMessage("You have no potions to quaff.");
            return;
        }

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Quaff which potion?",
                        gs.player.inventory.items, quaffable,
                        (int itemNum) -> {
                            backend.tellPlayer(new Quaff(gs.player, itemNum));
                        }));
    }

    private void tryWear(GameState gs) {
        Function<DungeonItem, Boolean> wearable = (DungeonItem item) -> item.slot != DungeonItem.Slot.NONE;
        if (countLegalItems(gs.player.inventory.items, wearable) == 0) {
            backend.logMessage("You have nothing you can equip/wear.");
            return;
        }

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Wear which item?",
                        gs.player.inventory.items, wearable,
                        (int itemNum) -> {
                            backend.tellPlayer(new Wear(gs.player, itemNum));
                        }));
    }

    private void tryTakeOff(GameState gs) {
        Function<DungeonItem, Boolean> removable = (DungeonItem item) -> true;
        if (countLegalItems(gs.player.equipment, removable) == 0) {
            backend.logMessage("You have nothing you can take off.");
            return;
        }

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Take off which item?",
                        gs.player.equipment, removable,
                        (int itemNum) -> {
                            backend.tellPlayer(new TakeOff(gs.player, itemNum));
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
            //case FIRE: backend.tellPlayer(new FireRocket(gs.player)); break;
            case SAVE: backend.tellPlayer(new Save()); break;
            case LOOK: startLooking(gs); break;
            case CLOSE_DOOR: tryCloseDoor(gs); break;
            case TARGET: startMonsterTargeting(gs); break;
            case TARGET_FLOOR: startFloorTargeting(gs); break;
            case INVENTORY: showInventory(gs); break;
            case DROP: tryDrop(gs); break;
            case GET: tryPickup(gs); break;
            case FIRE: tryFire(gs); break;
            case PLAYER_INFO: frontend.open(frontend.playerInfoWindow); break;
            case QUAFF: tryQuaff(gs); break;
            case WEAR: tryWear(gs); break;
            case TAKE_OFF: tryTakeOff(gs); break;
            case HELP: frontend.open(frontend.helpWindow); break;
            case DEBUG_INCR_CHAR_LEVEL: backend.tellPlayer(new DebugAction(DebugAction.What.INCREASE_CHAR_LEVEL)); break;
            case DEBUG_HEAL_CHAR: backend.tellPlayer(new DebugAction(DebugAction.What.HEAL)); break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        GameState gs = backend.getGameState();
        MapView mapView = new MapView(dim.width, dim.height, ImageController.TILE_SIZE, backend.getGameState());

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        // draw the map
        for (int y = mapView.rowMin; y <= mapView.rowMax; y++) {
            for (int x = mapView.colMin; x <= mapView.colMax; x++) {
                DungeonSquare square = gs.getCurrentMap().map[x][y];
                if (!gs.getCurrentMap().map[x][y].seen) {
                    continue;
                }
                mapView.drawTile(graphics, square.terrain.image, x, y);
                if (square.feature != null) {
                    mapView.drawTile(graphics, square.feature.image, x, y);
                }
                if (square.items != null) {
                    for (DungeonItem item : square.items.items) {
                        mapView.drawTile(graphics, item.image, x, y);
                    }
                }
            }
        }

        // draw monsters, player, pets
        for (Actor actor : gs.getCurrentMap().actors) {
            if (gs.player.canSee(gs, actor.loc)) {
                mapView.drawTile(graphics, actor.image, actor.loc.x, actor.loc.y);
            }
        }

        // draw player targets
        if (gs.player.floorTarget != null) {
            mapView.drawCircle(graphics, Color.RED, gs.player.floorTarget.x, gs.player.floorTarget.y);
        } else if (gs.player.monsterTarget != null) {
            mapView.drawCircle(graphics, Color.RED, gs.player.monsterTarget.loc.x, gs.player.monsterTarget.loc.y);
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
                if (!gs.getCurrentMap().map[x][y].seen) {
                    continue;
                }
                int maxDarkness = 220;
                double darknessD = 1.0 - (gs.getCurrentMap().map[x][y].brightness / (double) gs.getCurrentMap().MAX_BRIGHTNESS);
                // Assign max darkness if we can't see it; alternatively, we could paint
                // in gray, or something.  Or just not show it at all?
                if (!gs.player.canSee(gs, new Point(x, y))) {
                    darknessD = 1;
                }
                int darkness = (int) Math.round(maxDarkness * darknessD);
                mapView.makeShadow(graphics, x, y, darkness);
            }
        }
    }

    private void closeDoor(GameState gameState, Point p) {
        DungeonSquare square = gameState.getCurrentMap().map[p.x][p.y];
        String closedDoorId = square.feature.actionParams.name;
        DungeonFeature closedDoor = FeatureData.getFeature(closedDoorId);
        backend.tellPlayer(new ModifyFeature(gameState.player, p, closedDoor));
    }

    private void tryCloseDoor(GameState gameState) {
        List<Point> targetableSquares = Targeting.getCloseDoorTargets(gameState);

        if (targetableSquares.isEmpty()) {
            backend.logMessage("no doors here you can close.");
            return;
        } else if (targetableSquares.size() == 1) {
            // only one door to close.  Just close it
            closeDoor(gameState, targetableSquares.get(0));
        } else {
            // several doors; prompt user to pick which one
            parent.addLayer(new GameTargetLayer(parent, targetableSquares, GameTargetLayer.TargetMode.CLOSE_DOOR,
                    (Point p) -> { closeDoor(gameState, p); }));
        }
    }

    private void tryFire(GameState gameState) {
        Player player = gameState.player;

        // make sure the player has a bow and arrows
        if (!player.hasBowEquipped()) {
            backend.logMessage("you do not have a bow equipped.");
            return;
        }
        if (player.findArrows() == null) {
            backend.logMessage("you do not have any arrows.");
            return;
        }

        // make sure there's a target
        Point target;
        if (player.floorTarget != null) {
            target = player.floorTarget;
        }
        else if (player.monsterTarget != null) {
            target = player.monsterTarget.loc;
        } else {
            backend.logMessage("no target selected.");
            return;
        }

        backend.tellPlayer(new FireArrow(gameState.player, target));
    }

    private void startLooking(GameState gameState) {
        MapView mapView = new MapView(dim.width, dim.height, ImageController.TILE_SIZE, gameState);
        List<Point> targetableSquares = Targeting.getLookTargets(gameState, mapView);
        parent.addLayer(new GameTargetLayer(parent, targetableSquares, GameTargetLayer.TargetMode.LOOK, Point -> {}));
    }

    private void startMonsterTargeting(GameState gameState) {
        MapView mapView = new MapView(dim.width, dim.height, ImageController.TILE_SIZE, gameState);
        List<Point> targetableSquares = Targeting.getMonsterTargets(gameState, mapView);
        if (targetableSquares.isEmpty()) {
            backend.logMessage("no monsters to target");
            return;
        }
        parent.addLayer(new GameTargetLayer(parent, targetableSquares, GameTargetLayer.TargetMode.TARGET,
                (Point p) -> {
                    Actor m = gameState.getCurrentMap().actorAt(p.x, p.y);
                    gameState.player.monsterTarget = null;
                    gameState.player.floorTarget = null;
                    if (!m.friendly) {
                        gameState.player.monsterTarget = m;
                    }
                }
        ));
    }

    private void startFloorTargeting(GameState gameState) {
        MapView mapView = new MapView(dim.width, dim.height, ImageController.TILE_SIZE, gameState);
        List<Point> targetableSquares = Targeting.getFloorTargets(gameState, mapView);
        if (targetableSquares.isEmpty()) {
            backend.logMessage("you can't see anything!");
            return;
        }
        parent.addLayer(new GameTargetLayer(parent, targetableSquares, GameTargetLayer.TargetMode.TARGET,
                (Point p) -> {
                    gameState.player.monsterTarget = null;
                    gameState.player.floorTarget = p;
                }
        ));
    }
}
