package pow.frontend.window;

import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.frontend.Style;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.frontend.utils.Targeting;
import pow.util.Bresenham;
import pow.util.Direction;
import pow.util.Point;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameTargetLayer extends AbstractWindow {

    public enum TargetMode {
        LOOK,
        TARGET,
        CLOSE_DOOR
    }

    private final GameWindow parent;
    private final List<Point> targetableSquares;
    private final MapView mapView;
    private final TargetMode mode;
    private final Consumer<Point> callback;

    private int targetIdx;
    private String lookMessage;
    private String helpMessage;

    public GameTargetLayer(GameWindow parent, List<Point> targetableSquares, TargetMode mode, Consumer<Point> callback) {
        super(parent.dim, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
        this.targetableSquares = targetableSquares;
        this.targetIdx = 0;  // start with the first point in 'targetableSquares'
        this.mode = mode;
        this.callback = callback;
        GameState gs = backend.getGameState();
        mapView = new MapView(dim.width, dim.height - parent.MESSAGE_BAR_HEIGHT, ImageController.TILE_SIZE, gs);

        lookMessage = "";
        helpMessage = "";
        update();
    }

    @Override
    public void processKey(KeyEvent e) {
        KeyInput input = KeyUtils.getKeyInput(e);
        switch (input) {
            case EAST: moveCursor(1, 0); break;
            case WEST: moveCursor(-1, 0); break;
            case SOUTH: moveCursor(0, 1); break;
            case NORTH: moveCursor(0, -1); break;
            case NORTH_WEST: moveCursor(-1, -1); break;
            case NORTH_EAST: moveCursor(1, -1); break;
            case SOUTH_WEST: moveCursor(-1, 1); break;
            case SOUTH_EAST: moveCursor(1, 1); break;
            case CYCLE: cycleCursor(); break;
            case CANCEL: stopLooking(); break;
            case OKAY:
            case LOOK:
            case CLOSE_DOOR:
            case TARGET:
            case TARGET_FLOOR:
                stopLooking();
                Point cursorPosition = targetableSquares.get(targetIdx);
                callback.accept(cursorPosition);
                break;
            default: break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        Point cursorPosition = targetableSquares.get(targetIdx);
        mapView.frameRect(graphics, Color.YELLOW, cursorPosition.x, cursorPosition.y);
        if (mode == TargetMode.TARGET) {

            GameState gs = backend.getGameState();
            int radius = gs.party.selectedActor.viewRadius;
            if (!cursorPosition.equals(gs.party.selectedActor.loc)) {
                List<Point> ray = Bresenham.makeRay(gs.party.selectedActor.loc, cursorPosition, radius + 1);
                for (Point p : ray) {
                    if (!gs.party.selectedActor.canSeeLocation(gs, p)) break;
                    if (!mapView.isVisible(p.x, p.y)) break;
                    mapView.drawCircle(graphics, Color.GREEN, p.x, p.y);
                    if (gs.getCurrentMap().map[p.x][p.y].blockAir()) break;
                }
            }
        }

        graphics.setFont(Style.getDefaultFont());
        graphics.setColor(Color.WHITE);
        graphics.drawString(lookMessage, Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN - Style.FONT_SIZE);
        graphics.drawString(helpMessage, Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }

    private void moveCursor(int dx, int dy) {
        Direction dir = Direction.getDir(dx, dy);
        this.targetIdx = Targeting.pickTarget(targetIdx, dir, targetableSquares);
        update();
    }

    private void cycleCursor() {
        this.targetIdx = (this.targetIdx + 1) % targetableSquares.size();
        update();
    }

    private void update() {
        Point cursorPosition = targetableSquares.get(targetIdx);
        GameState gs = backend.getGameState();

        helpMessage = getHelpMessage();
        lookMessage = getLookMessage();
        Actor selectedActor = backend.getGameState().getCurrentMap().actorAt(cursorPosition.x, cursorPosition.y);
        // even if there's an actor there, don't show it if the player can't see it
        if (selectedActor != null) {
            if (!gs.party.selectedActor.canSeeLocation(gs, selectedActor.loc) || !gs.party.selectedActor.canSeeActor(selectedActor)) {
                selectedActor = null;
            }
        }
        frontend.monsterInfoWindow.setActor(selectedActor);
        frontend.monsterInfoWindow.visible = selectedActor != null;
        frontend.setDirty(true);
    }

    private void stopLooking() {
        frontend.monsterInfoWindow.setActor(null);
        frontend.monsterInfoWindow.visible = false;
        parent.removeLayer();
    }

    private String featureOrTerrain(DungeonSquare square) {
        if (square.feature != null) {
            return TextUtils.format(square.feature.name, 1, false);
        }
        return TextUtils.format(square.terrain.name, 1, false);
    }

    private String getHelpMessage() {
        switch (mode) {
            case LOOK:
                return "Press a direction or [space] to look at a location, x/[enter]/[esc] to cancel.";
            case CLOSE_DOOR:
                return "Press a direction or [space] to select a door, c/[enter] to close, [esc] to cancel.";
            case TARGET:
                return "Press a direction or [space] to select a target, t/[enter] to accept, [esc] to cancel.";
            default:
                return "";
        }
    }

    private String getLookMessage() {
        Point cursorPosition = targetableSquares.get(targetIdx);
        int x = cursorPosition.x;
        int y = cursorPosition.y;
        GameState gs = backend.getGameState();
        DungeonSquare square = gs.getCurrentMap().map[x][y];

        if (gs.party.selectedActor.canSeeLocation(gs, cursorPosition)) {
            StringBuilder sb = new StringBuilder();
            sb.append("you see ");

            List<String> interestingThings = new ArrayList<>();
            Actor actor = gs.getCurrentMap().actorAt(x,y);
            if (actor != null && gs.party.selectedActor.canSeeActor(actor)) {
                interestingThings.add(TextUtils.format(actor.name, 1, false));
            }
            if (square.items != null) {
                for (int i = 0; i < square.items.size(); i++) {
                    interestingThings.add(square.items.items.get(i).stringWithInfo());
                }
            }
            if (!interestingThings.isEmpty()) {
                sb.append(TextUtils.formatList(interestingThings));
                sb.append(" on ");
            }

            sb.append(featureOrTerrain(square));
            return sb.toString();
        }
        else {
            if (square.seen) {
                return featureOrTerrain(square);
            } else {
                return "";  // skip squares the player can't see
            }
        }
    }
}
