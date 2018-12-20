package pow.frontend.window;

import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.MessageLog;
import pow.backend.SpellParams;
import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.actors.ai.MonsterDanger;
import pow.backend.actors.ai.PetAi;
import pow.backend.behavior.RunBehavior;
import pow.backend.dungeon.*;
import pow.backend.dungeon.gen.FeatureData;
import pow.frontend.WindowDim;
import pow.frontend.utils.*;
import pow.util.Direction;
import pow.util.Point;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GameMainLayer extends AbstractWindow {

    private final GameWindow parent;
    private boolean showPetAi;   // not part of gamestate since just debugging
    private boolean showPlayerAi;

    public GameMainLayer(GameWindow parent) {
        super(parent.dim, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
        this.showPetAi = false;
        this.showPlayerAi = false;
    }

    private void togglePetAi() {
        showPetAi = !showPetAi;
        showPlayerAi = false;
        frontend.setDirty(true);
    }
    private void togglePlayerAi() {
        showPetAi = false;
        showPlayerAi = !showPlayerAi;
        frontend.setDirty(true);
    }

    private void tryPickup(GameState gs) {
        if (gs.party.isPetSelected()) {
            backend.logMessage(gs.party.pet.getNoun() + " cannot carry items.", MessageLog.MessageType.USER_ERROR);
            return;
        }
        Point loc = gs.party.selectedActor.loc;
        ItemList items = gs.getCurrentMap().map[loc.x][loc.y].items;

        switch (items.size()) {
            case 0:
                backend.logMessage("Nothing here to pick up.", MessageLog.MessageType.USER_ERROR);
                break;
            case 1:
                // no ambiguity, just pick up the one item
                backend.tellSelectedActor(new PickUp(gs.party.selectedActor, 0, items.items.get(0).count));
                break;
            default:
                // ask the user to pick which item
                frontend.open(
                        new ItemChoiceWindow(632, 25, this.backend, this.frontend,
                                "Pick up which item?", null,
                                items.items, null, (DungeonItem item) -> true,
                                (ItemChoiceWindow.ItemChoice choice) ->
                                        backend.tellSelectedActor(new PickUp(gs.party.selectedActor, choice.itemIdx, items.items.get(choice.itemIdx).count))));
                break;
        }
    }

    private void showGround(GameState gs) {
        if (gs.party.isPetSelected()) {
            backend.logMessage(gs.party.pet.getNoun() + " cannot carry items.", MessageLog.MessageType.USER_ERROR);
            return;
        }
        Point loc = gs.party.selectedActor.loc;
        ItemList items = gs.getCurrentMap().map[loc.x][loc.y].items;
        frontend.open(new ItemActionWindow(300, 15, this.backend, this.frontend, "Ground:",
                items, ItemActions.ItemLocation.GROUND));
    }

    private void showInventory(GameState gs) {
        if (gs.party.isPetSelected()) {
            backend.logMessage(gs.party.pet.getNoun() + " cannot carry items.", MessageLog.MessageType.USER_ERROR);
            return;
        }
        frontend.open(new ItemActionWindow(300, 15, this.backend, this.frontend, "Inventory:",
                       gs.party.selectedActor.inventory, ItemActions.ItemLocation.INVENTORY));
    }

    private void showEquipment(GameState gs) {
        if (gs.party.isPetSelected()) {
            backend.logMessage(gs.party.pet.getNoun() + " cannot wear/wield items.", MessageLog.MessageType.USER_ERROR);
            return;
        }
        frontend.open(new ItemActionWindow(300, 15, this.backend, this.frontend, "Equipment:",
                gs.party.selectedActor.equipment, ItemActions.ItemLocation.EQUIPMENT));
    }

    private void showPetInventory(GameState gs) {
        backend.logMessage(gs.party.pet.getNoun() + " cannot carry items.", MessageLog.MessageType.USER_ERROR);
        frontend.open(new ItemActionWindow(300, 15, this.backend, this.frontend, "Pet:",
                gs.party.pet.inventory, ItemActions.ItemLocation.PET));
    }

    private void showKnowledge(GameState gs) {
        frontend.open(
                new KnowledgeWindow(new WindowDim(210, 5, 672, 672), true, this.backend, this.frontend,
                        gs.party.knowledge.getMonsterSummary()));
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
        if (gs.party.isPetSelected()) {
            backend.logMessage(gs.party.pet.getNoun() + " has no items.", MessageLog.MessageType.USER_ERROR);
            return;
        }
        Function<DungeonItem, Boolean> droppable = (DungeonItem item) -> true;
        if (countLegalItems(gs.party.selectedActor.inventory.items, droppable) == 0) {
            backend.logMessage(gs.party.selectedActor.getNoun() + " has no items.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Drop which item?",
                        null,
                        gs.party.selectedActor.inventory.items, null, droppable,
                        (ItemChoiceWindow.ItemChoice choice) -> backend.tellSelectedActor(new Drop(gs.party.selectedActor, choice.itemIdx,
                                gs.party.selectedActor.inventory.items.get(choice.itemIdx).count))));
    }


    private void tryQuaff(GameState gs) {
        if (gs.party.isPetSelected()) {
            backend.logMessage(gs.party.pet.getNoun() + " cannot quaff potions.", MessageLog.MessageType.USER_ERROR);
            return;
        }
        Point loc = gs.party.selectedActor.loc;
        ItemList inventoryItems = gs.party.selectedActor.inventory;
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
                                backend.tellSelectedActor(new Quaff(
                                        gs.party.selectedActor,
                                        choice.useSecondList ? altItemList : mainItemList,
                                        choice.itemIdx))));
    }

    private void tryWear(GameState gs) {
        if (gs.party.isPetSelected()) {
            backend.logMessage(gs.party.pet.getNoun() + " cannot equip/wear items.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        Point loc = gs.party.selectedActor.loc;
        ItemList inventoryItems = gs.party.selectedActor.inventory;
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
                                backend.tellSelectedActor(new Wear(
                                        gs.party.selectedActor,
                                        choice.useSecondList ? altItemList : mainItemList,
                                        choice.itemIdx))));
    }

    private void tryTakeOff(GameState gs) {
        if (gs.party.isPetSelected()) {
            backend.logMessage(gs.party.pet.getNoun() + " has no items equipped.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        Function<DungeonItem, Boolean> removable = (DungeonItem item) -> true;
        if (countLegalItems(gs.party.selectedActor.equipment.items, removable) == 0) {
            backend.logMessage("You have nothing you can take off.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        frontend.open(
                new ItemChoiceWindow(632, 25, this.backend, this.frontend, "Take off which item?", null,
                        gs.party.selectedActor.equipment.items, null, removable,
                        (ItemChoiceWindow.ItemChoice choice) -> backend.tellSelectedActor(new TakeOff(gs.party.selectedActor, choice.itemIdx))));
    }

    private void tryCastSpell(GameState gs) {
        frontend.open(
                new SpellChoiceWindow(332, 100, this.backend, this.frontend,
                        "Cast which spell?",
                        gs.party.selectedActor.spells,
                        (Integer choice) -> {
                            SpellParams params = gs.party.selectedActor.spells.get(choice);
                            Point target = gs.party.selectedActor.getTarget();
                            if (target == null && params.requiresTarget) {
                                backend.logMessage("no target selected.", MessageLog.MessageType.USER_ERROR);
                                return;
                            }
                            backend.tellSelectedActor(
                                    SpellParams.buildAction(params, gs.party.selectedActor, target));
                        })
        );
    }

    private void tryShowMap(GameState gs) {
        if (gs.party.artifacts.hasMap()) {
            frontend.open(frontend.worldMapWindow);
        } else {
            backend.logMessage("You don't have a map.", MessageLog.MessageType.USER_ERROR);
        }
    }

    private void tryAutoplayOptions(GameState gs) {
        if (gs.party.pet != null) {
            frontend.open(frontend.autoplayOptionWindow);
        } else {
            backend.logMessage("You don't have a pet; no autoplay available.", MessageLog.MessageType.USER_ERROR);
        }
    }

    @Override
    public void processKey(KeyEvent e) {
        GameState gs = backend.getGameState();

        KeyInput input = KeyUtils.getKeyInput(e);
        switch (input) {
            case EAST: backend.tellSelectedActor(new MoveRequest(gs.party.selectedActor, 1, 0)); break;
            case WEST: backend.tellSelectedActor(new MoveRequest(gs.party.selectedActor, -1, 0)); break;
            case SOUTH: backend.tellSelectedActor(new MoveRequest(gs.party.selectedActor, 0, 1)); break;
            case NORTH: backend.tellSelectedActor(new MoveRequest(gs.party.selectedActor, 0, -1)); break;
            case NORTH_WEST: backend.tellSelectedActor(new MoveRequest(gs.party.selectedActor, -1, -1)); break;
            case NORTH_EAST: backend.tellSelectedActor(new MoveRequest(gs.party.selectedActor, 1, -1)); break;
            case SOUTH_WEST: backend.tellSelectedActor(new MoveRequest(gs.party.selectedActor, -1, 1)); break;
            case SOUTH_EAST: backend.tellSelectedActor(new MoveRequest(gs.party.selectedActor, 1, 1)); break;

            case RUN_EAST: backend.tellSelectedActor(new RunBehavior(gs.party.selectedActor, Direction.E)); break;
            case RUN_WEST: backend.tellSelectedActor(new RunBehavior(gs.party.selectedActor, Direction.W)); break;
            case RUN_SOUTH: backend.tellSelectedActor(new RunBehavior(gs.party.selectedActor, Direction.S)); break;
            case RUN_NORTH: backend.tellSelectedActor(new RunBehavior(gs.party.selectedActor, Direction.N)); break;
            case RUN_NORTH_WEST: backend.tellSelectedActor(new RunBehavior(gs.party.selectedActor, Direction.NW)); break;
            case RUN_NORTH_EAST: backend.tellSelectedActor(new RunBehavior(gs.party.selectedActor, Direction.NE)); break;
            case RUN_SOUTH_WEST: backend.tellSelectedActor(new RunBehavior(gs.party.selectedActor, Direction.SW)); break;
            case RUN_SOUTH_EAST: backend.tellSelectedActor(new RunBehavior(gs.party.selectedActor, Direction.SE)); break;

            case REST: backend.tellSelectedActor(new Move(gs.party.selectedActor, 0, 0)); break;
            case SAVE: backend.tellSelectedActor(new Save()); break;
            case LOOK: startLooking(gs); break;
            case CLOSE_DOOR: tryCloseDoor(gs); break;
            case TARGET: startMonsterTargeting(gs); break;
            case TARGET_FLOOR: startFloorTargeting(gs); break;
            case INVENTORY: showInventory(gs); break;
            case GROUND: showGround(gs); break;
            case EQUIPMENT: showEquipment(gs); break;
            //case PET: showPetInventory(gs); break;
            case AUTO_PLAY: tryAutoplayOptions(gs); break;
            //case SELECT_CHARACTER: backend.tellSelectedActor(new SelectNextCharacter()); break;
            //case DROP: tryDrop(gs); break;
            case GET: tryPickup(gs); break;
            case FIRE: tryFire(gs); break;
            case MAGIC: tryCastSpell(gs); break;
            case PLAYER_INFO: frontend.open(frontend.playerInfoWindow); break;
            case SHOW_WORLD_MAP: tryShowMap(gs); break;
            case KNOWLEDGE: showKnowledge(gs); break;
            //case QUAFF: tryQuaff(gs); break;
            //case WEAR: tryWear(gs); break;
            //case TAKE_OFF: tryTakeOff(gs); break;
            case HELP: frontend.open(frontend.helpWindow); break;
            case DEBUG_INCR_CHAR_LEVEL: backend.tellSelectedActor(new DebugAction(DebugAction.What.INCREASE_CHAR_LEVEL)); break;
            case DEBUG_HEAL_CHAR: backend.tellSelectedActor(new DebugAction(DebugAction.What.HEAL)); break;
            case DEBUG_SHOW_PET_AI: togglePetAi(); break;
            case DEBUG_SHOW_PLAYER_AI: togglePlayerAi(); break;
            default: break;
        }
    }

    private static final Map<MonsterDanger.Danger, Color> dangerColors;
    private static final Color friendlyColor;
    static {
        int alpha = 80;
        dangerColors = new HashMap<>();
        dangerColors.put(MonsterDanger.Danger.SAFE, new Color(0, 255, 0, alpha));  // green
        dangerColors.put(MonsterDanger.Danger.NORMAL, new Color(255, 255, 0, alpha)); // yellow
        dangerColors.put(MonsterDanger.Danger.UNSAFE, new Color(255, 153, 0, alpha));  // orange
        dangerColors.put(MonsterDanger.Danger.DANGEROUS, new Color(255, 0, 0, alpha)); // red
        dangerColors.put(MonsterDanger.Danger.DEADLY, new Color(204, 0, 204, alpha)); // magenta/purple
        friendlyColor = new Color(0, 153, 255, alpha);
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
                if (square.feature != null && gs.party.selectedActor.canSeeFeature(square.feature)) {
                    ImageController.DrawMode drawMode = square.feature.flags.trap ? ImageController.DrawMode.TRANSPARENT : ImageController.DrawMode.NORMAL;
                    mapView.drawTile(graphics, square.feature.image, x, y, drawMode);
                }
                for (DungeonItem item : square.items.items) {
                    mapView.drawTile(graphics, item.image, x, y, ImageController.DrawMode.NORMAL);
                }
            }
        }

        // draw monsters, player, pets
        Player pet = PetAi.getOtherPartyActor(gs.party.selectedActor, gs);
        Actor petTarget = null;
        if (pet != null) {
            petTarget = PetAi.getPrimaryTarget(pet, gs);
        }
        for (Actor actor : gs.getCurrentMap().actors) {
            if (gs.party.selectedActor.canSeeLocation(gs, actor.loc) && gs.party.selectedActor.canSeeActor(actor)) {
                if (this.showPetAi && pet != null) {
                    // show how dangerous this is to the pet.
                    if (actor != pet) {
                        Color dangerColor =
                                actor.friendly ? friendlyColor :
                                        dangerColors.get(MonsterDanger.getDanger(pet, actor));
                        mapView.drawBlock(graphics, dangerColor, actor.loc.x, actor.loc.y);
                    }
                    // show if the actor is a primary target of pet
                    if (petTarget != null && petTarget == actor) {
                        mapView.frameRoundRect(graphics, Color.ORANGE, actor.loc.x, actor.loc.y);
                    }
                } else if (this.showPlayerAi) {
                    // show how dangerous this is to the player.
                    if (actor != gs.party.selectedActor) {
                        Color dangerColor =
                                actor.friendly ? friendlyColor :
                                        dangerColors.get(MonsterDanger.getDanger(gs.party.selectedActor, actor));
                        mapView.drawBlock(graphics, dangerColor, actor.loc.x, actor.loc.y);
                    }
                }

                ImageController.DrawMode drawMode = actor.invisible ? ImageController.DrawMode.TRANSPARENT : ImageController.DrawMode.NORMAL;
                mapView.drawTile(graphics, actor.image, actor.loc.x, actor.loc.y, drawMode);
            }
        }

        // draw player targets
        Point target = gs.party.selectedActor.getTarget();
        if (target != null) {
            mapView.drawCircle(graphics, Color.RED, target.x, target.y);
        }

        // draw effects
        for (DungeonEffect effect : gs.getCurrentMap().effects) {
            for (DungeonEffect.ImageLoc imageLoc : effect.imageLocs) {
                if (gs.party.selectedActor.canSeeLocation(gs, imageLoc.loc)) {
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
                if (!gs.party.selectedActor.canSeeLocation(gs, new Point(x, y))) {
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
        backend.tellSelectedActor(new ModifyFeature(gameState.party.selectedActor, p, closedDoor));
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
        if (gameState.party.isPetSelected()) {
            backend.logMessage(gameState.party.pet.getNoun() + " cannot fire arrows.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        Player actor = gameState.party.selectedActor;

        // make sure the player has a bow and arrows
        if (!actor.hasBowEquipped()) {
            backend.logMessage(actor.getNoun() + "does not have a bow equipped.", MessageLog.MessageType.USER_ERROR);
            return;
        }
        if (actor.findArrows() == null) {
            backend.logMessage(actor.getNoun() + "does not have any arrows.", MessageLog.MessageType.USER_ERROR);
            return;
        }

        // make sure there's a target
        Point target = actor.getTarget();
        if (target == null) {
            backend.logMessage(actor.getNoun() + " does not have a target selected.", MessageLog.MessageType.USER_ERROR);
            return;
        }


        backend.tellSelectedActor(new Arrow(gameState.party.selectedActor, target, gameState.party.selectedActor.getSecondaryAttack()));
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
                    if (!m.friendly) {
                        gameState.party.selectedActor.target.setMonster(m);
                    } else {
                        gameState.party.selectedActor.target.clear();
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
                    gameState.party.selectedActor.target.setFloor(p);
                }
        ));
    }
}
