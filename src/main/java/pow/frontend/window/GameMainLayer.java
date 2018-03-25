package pow.frontend.window;

import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.SpellParams;
import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.behavior.RunBehavior;
import pow.backend.dungeon.*;
import pow.backend.dungeon.gen.FeatureData;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.frontend.utils.Targeting;
import pow.util.Point;
import pow.util.Direction;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Function;

public class GameMainLayer extends AbstractWindow {

    private final GameWindow parent;

    public GameMainLayer(GameWindow parent) {
        super(parent.dim, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
    }

    private void tryPickup(GameState gs) {
        Point playerLoc = gs.player.loc;
        ItemList items = gs.getCurrentMap().map[playerLoc.x][playerLoc.y].items;

        if (items.size() == 0) {
            backend.logMessage("Nothing here to pick up.", MessageLog.MessageType.USER_ERROR);
        } else if (items.size() == 1) {
            // no ambiguity, just pick up the one item
            backend.tellPlayer(new PickUp(gs.player, 0, items.items.get(0).count));
        } else {
            // ask the user to pick which item
            frontend.open(
                    new ItemChoiceWindow(632, 25, this.backend, this.frontend,
                            "Pick up which item?", null,
                            items.items, null, (DungeonItem item) -> true,
                            (ItemChoiceWindow.ItemChoice choice) ->
                                    backend.tellPlayer(new PickUp(gs.player, choice.itemIdx, items.items.get(choice.itemIdx).count))));
        }
    }

    private void showInventory(GameState gs) {
        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Inventory:", null,
                        gs.player.inventory.items, null, (DungeonItem item) -> true,
                        (ItemChoiceWindow.ItemChoice choice) -> { }));
    }

    private void showKnowledge(GameState gs) {
        frontend.open(
                new KnowledgeWindow(new WindowDim(210, 5, 672, 672), true, this.backend, this.frontend,
                        gs.player.knowledge.getMonsterSummary()));
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
            backend.logMessage("You have nothing to drop.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Drop which item?",
                        null,
                        gs.player.inventory.items, null, droppable,
                        (ItemChoiceWindow.ItemChoice choice) -> backend.tellPlayer(new Drop(gs.player, choice.itemIdx,
                                gs.player.inventory.items.get(choice.itemIdx).count))));
    }


    private void tryQuaff(GameState gs) {
        Point loc = gs.player.loc;
        ItemList inventoryItems = gs.player.inventory;
        ItemList floorItems = gs.getCurrentMap().map[loc.x][loc.y].items;
        Function<DungeonItem, Boolean> quaffable = (DungeonItem item) -> item.flags.potion;

        boolean doInventory = countLegalItems(inventoryItems.items, quaffable) > 0;
        boolean doFloor = floorItems != null && countLegalItems(floorItems.items, quaffable) > 0;

        if (!doFloor && !doInventory) {
            backend.logMessage("There are no potions to quaff here.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        final String message = doFloor && doInventory ? "Quaff which potion? (Press tab to show floor.)" : "Quaff which potion?";
        final String altMessage = doFloor && doInventory ? "Quaff which potion? (Press tab to show inventory.)" : null;
        final ItemList mainItemList = doInventory ? inventoryItems : floorItems;
        final ItemList altItemList = doFloor && doInventory ? floorItems : null;
        final List<DungeonItem> altItems = doFloor && doInventory ? floorItems.items : null;

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, message, altMessage,
                        mainItemList.items, altItems, quaffable,
                        (ItemChoiceWindow.ItemChoice choice) ->
                                backend.tellPlayer(new Quaff(
                                        gs.player,
                                        choice.useSecondList ? altItemList : mainItemList,
                                        choice.itemIdx))));
    }

    private void tryWear(GameState gs) {
        Point loc = gs.player.loc;
        ItemList inventoryItems = gs.player.inventory;
        ItemList floorItems = gs.getCurrentMap().map[loc.x][loc.y].items;
        Function<DungeonItem, Boolean> wearable = (DungeonItem item) -> item.slot != DungeonItem.Slot.NONE;

        boolean doInventory = countLegalItems(inventoryItems.items, wearable) > 0;
        boolean doFloor = floorItems != null && countLegalItems(floorItems.items, wearable) > 0;

        // easy case -- nothing to wear
        if (!doFloor && !doInventory) {
            backend.logMessage("There is nothing you can equip/wear.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        // Figure out how to display to the user --
        // if there's only wearables on floor or in inventory, just show those as options,
        // else, allow user to swap back and forth between them.
        final String message = doFloor && doInventory ? "Wear which item? (Press tab to show floor.)" : "Wear which item?";
        final String altMessage = doFloor && doInventory ? "Wear which item? (Press tab to show inventory.)" : null;
        final ItemList mainItemList = doInventory ? inventoryItems : floorItems;
        final ItemList altItemList = doFloor && doInventory ? floorItems : null;
        final List<DungeonItem> altItems = doFloor && doInventory ? floorItems.items : null;

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, message, altMessage,
                        mainItemList.items, altItems, wearable,
                        (ItemChoiceWindow.ItemChoice choice) ->
                                backend.tellPlayer(new Wear(
                                        gs.player,
                                        choice.useSecondList ? altItemList : mainItemList,
                                        choice.itemIdx))));
    }

    private void tryTakeOff(GameState gs) {
        Function<DungeonItem, Boolean> removable = (DungeonItem item) -> true;
        if (countLegalItems(gs.player.equipment, removable) == 0) {
            backend.logMessage("You have nothing you can take off.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Take off which item?", null,
                        gs.player.equipment, null, removable,
                        (ItemChoiceWindow.ItemChoice choice) -> backend.tellPlayer(new TakeOff(gs.player, choice.itemIdx))));
    }

    private void tryCastSpell(GameState gs) {
        frontend.open(
                new SpellChoiceWindow(332, 100, this.backend, this.frontend,
                        "Cast which spell?",
                        gs.player.spells,
                        (Integer choice) -> {
                            SpellParams params = gs.player.spells.get(choice);
                            Point target = gs.player.getTarget();
                            if (target == null && params.requiresTarget) {
                                backend.logMessage("no target selected.", MessageLog.MessageType.USER_ERROR);
                                return;
                            }
                            backend.tellPlayer(
                                    SpellParams.buildAction(params, gs.player, target));
                        })
        );
    }

    private void tryShowMap(GameState gs) {
        if (gs.player.hasMap()) {
            frontend.open(frontend.worldMapWindow);
        } else {
            backend.logMessage("You don't have a map.", MessageLog.MessageType.USER_ERROR);
        }
    }

    @Override
    public void processKey(KeyEvent e) {
        GameState gs = backend.getGameState();

        KeyInput input = KeyUtils.getKeyInput(e);
        switch (input) {
            case EAST: backend.tellPlayer(new MoveRequest(gs.player, 1, 0)); break;
            case WEST: backend.tellPlayer(new MoveRequest(gs.player, -1, 0)); break;
            case SOUTH: backend.tellPlayer(new MoveRequest(gs.player, 0, 1)); break;
            case NORTH: backend.tellPlayer(new MoveRequest(gs.player, 0, -1)); break;
            case NORTH_WEST: backend.tellPlayer(new MoveRequest(gs.player, -1, -1)); break;
            case NORTH_EAST: backend.tellPlayer(new MoveRequest(gs.player, 1, -1)); break;
            case SOUTH_WEST: backend.tellPlayer(new MoveRequest(gs.player, -1, 1)); break;
            case SOUTH_EAST: backend.tellPlayer(new MoveRequest(gs.player, 1, 1)); break;

            case RUN_EAST: backend.tellPlayer(new RunBehavior(gs.player, Direction.E)); break;
            case RUN_WEST: backend.tellPlayer(new RunBehavior(gs.player, Direction.W)); break;
            case RUN_SOUTH: backend.tellPlayer(new RunBehavior(gs.player, Direction.S)); break;
            case RUN_NORTH: backend.tellPlayer(new RunBehavior(gs.player, Direction.N)); break;
            case RUN_NORTH_WEST: backend.tellPlayer(new RunBehavior(gs.player, Direction.NW)); break;
            case RUN_NORTH_EAST: backend.tellPlayer(new RunBehavior(gs.player, Direction.NE)); break;
            case RUN_SOUTH_WEST: backend.tellPlayer(new RunBehavior(gs.player, Direction.SW)); break;
            case RUN_SOUTH_EAST: backend.tellPlayer(new RunBehavior(gs.player, Direction.SE)); break;

            case REST: backend.tellPlayer(new Move(gs.player, 0, 0)); break;
            case SAVE: backend.tellPlayer(new Save()); break;
            case LOOK: startLooking(gs); break;
            case CLOSE_DOOR: tryCloseDoor(gs); break;
            case TARGET: startMonsterTargeting(gs); break;
            case TARGET_FLOOR: startFloorTargeting(gs); break;
            case INVENTORY: showInventory(gs); break;
            case DROP: tryDrop(gs); break;
            case GET: tryPickup(gs); break;
            case FIRE: tryFire(gs); break;
            case MAGIC: tryCastSpell(gs); break;
            case PLAYER_INFO: frontend.open(frontend.playerInfoWindow); break;
            case SHOW_WORLD_MAP: tryShowMap(gs); break;
            case KNOWLEDGE: showKnowledge(gs); break;
            case QUAFF: tryQuaff(gs); break;
            case WEAR: tryWear(gs); break;
            case TAKE_OFF: tryTakeOff(gs); break;
            case HELP: frontend.open(frontend.helpWindow); break;
            case DEBUG_INCR_CHAR_LEVEL: backend.tellPlayer(new DebugAction(DebugAction.What.INCREASE_CHAR_LEVEL)); break;
            case DEBUG_HEAL_CHAR: backend.tellPlayer(new DebugAction(DebugAction.What.HEAL)); break;
            default: break;
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
                mapView.drawTile(graphics, square.terrain.image, x, y, ImageController.DrawMode.NORMAL);
                if (square.feature != null) {
                    mapView.drawTile(graphics, square.feature.image, x, y, ImageController.DrawMode.NORMAL);
                }
                for (DungeonItem item : square.items.items) {
                    mapView.drawTile(graphics, item.image, x, y, ImageController.DrawMode.NORMAL);
                }
            }
        }

        // draw monsters, player, pets
        for (Actor actor : gs.getCurrentMap().actors) {
            if (gs.player.canSeeLocation(gs, actor.loc) && gs.player.canSeeActor(actor)) {
                ImageController.DrawMode drawMode = actor.invisible ? ImageController.DrawMode.TRANSPARENT : ImageController.DrawMode.NORMAL;
                mapView.drawTile(graphics, actor.image, actor.loc.x, actor.loc.y, drawMode);
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
            DungeonEffect effect = frontend.getEffects().get(0);
            for (DungeonEffect.ImageLoc imageLoc : effect.imageLocs) {
                if (gs.player.canSeeLocation(gs, imageLoc.loc)) {
                    mapView.drawTile(graphics, imageLoc.imageName, imageLoc.loc.x, imageLoc.loc.y, ImageController.DrawMode.NORMAL);
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
                double darknessD = 1.0 - (gs.getCurrentMap().map[x][y].brightness / (double) GameMap.MAX_BRIGHTNESS);
                // Assign max darkness if we can't see it; alternatively, we could paint
                // in gray, or something.  Or just not show it at all?
                if (!gs.player.canSeeLocation(gs, new Point(x, y))) {
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
            backend.logMessage("no doors here you can close.", MessageLog.MessageType.USER_ERROR);
        } else if (targetableSquares.size() == 1) {
            // only one door to close.  Just close it
            closeDoor(gameState, targetableSquares.get(0));
        } else {
            // several doors; prompt user to pick which one
            parent.addLayer(new GameTargetLayer(parent, targetableSquares, GameTargetLayer.TargetMode.CLOSE_DOOR,
                    (Point p) -> closeDoor(gameState, p)));
        }
    }

    private void tryFire(GameState gameState) {
        Player player = gameState.player;

        // make sure the player has a bow and arrows
        if (!player.hasBowEquipped()) {
            backend.logMessage("you do not have a bow equipped.", MessageLog.MessageType.USER_ERROR);
            return;
        }
        if (player.findArrows() == null) {
            backend.logMessage("you do not have any arrows.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        // make sure there's a target
        Point target = player.getTarget();
        if (target == null) {
            backend.logMessage("no target selected.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        backend.tellPlayer(new Arrow(gameState.player, target, gameState.player.getSecondaryAttack()));
    }

    private void startLooking(GameState gameState) {
        MapView mapView = new MapView(dim.width, dim.height, ImageController.TILE_SIZE, gameState);
        List<Point> targetableSquares = Targeting.getLookTargets(gameState, mapView);
        parent.addLayer(new GameTargetLayer(parent, targetableSquares, GameTargetLayer.TargetMode.LOOK, Point -> {}));
    }

    private void startMonsterTargeting(GameState gameState) {
        List<Point> targetableSquares = Targeting.getMonsterFOVTargets(gameState);
        if (targetableSquares.isEmpty()) {
            backend.logMessage("no monsters to target", MessageLog.MessageType.USER_ERROR);
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
            backend.logMessage("you can't see anything!", MessageLog.MessageType.USER_ERROR);
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
