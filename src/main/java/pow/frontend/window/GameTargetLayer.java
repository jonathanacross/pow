package pow.frontend.window;

import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.frontend.utils.targeting.TargetingUtils;
import pow.util.MathUtils;
import pow.util.Point;
import pow.util.TextUtils;
import pow.util.direction.Direction;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class GameTargetLayer extends AbstractWindow {

    public enum TargetMode {
        LOOK;
    }

    private GameWindow parent;
    private int targetIdx;
    private List<Point> targetableSquares;
    MapView mapView;

    public GameTargetLayer(GameWindow parent, List<Point> targetableSquares, TargetMode mode) {
        super(parent.x, parent.y, parent.width, parent.height, parent.visible, parent.backend, parent.frontend);
        this.parent = parent;
        this.targetableSquares = targetableSquares;
        this.targetIdx = 0;  // start with the first point in 'targetableSquares'
        GameState gs = backend.getGameState();
        mapView = new MapView(width, height, ImageController.TILE_SIZE, gs);

        frontend.messages.push("");
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
            case OKAY:
            case CANCEL:
            case LOOK: stopLooking(); break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        Point cursorPosition = targetableSquares.get(targetIdx);
        mapView.frameRect(graphics, Color.YELLOW, cursorPosition.x, cursorPosition.y);
    }

    private void moveCursor(int dx, int dy) {
        Direction dir = new Direction(dx, dy);
        this.targetIdx = TargetingUtils.pickTarget(targetIdx, dir, targetableSquares);
        update();
    }

    private void cycleCursor() {
        this.targetIdx = (this.targetIdx + 1) % targetableSquares.size();
        update();
    }

    private void update() {
        Point cursorPosition = targetableSquares.get(targetIdx);

        frontend.messages.pop();
        frontend.messages.push(makeMessage());
        Actor selectedActor = backend.getGameState().getCurrentMap().actorAt(cursorPosition.x, cursorPosition.y);
        frontend.monsterInfoWindow.setActor(selectedActor);
        frontend.monsterInfoWindow.visible = selectedActor != null;
        frontend.setDirty(true);
    }

    private void stopLooking() {
        frontend.messages.pop();
        frontend.monsterInfoWindow.setActor(null);
        frontend.monsterInfoWindow.visible = false;
        parent.removeLayer();
    }

    // makes a list of things into an English list:
    // {a} -> a
    // {a,b} -> a and b
    // {a,b,c} -> a, b, and c
    // {a,b,c,d} -> a, b, c, and d
    private String makeListString(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            String join;
            if (i == 0) {
                join = "";
            } else if (i < items.size() - 1) {
                join = ", ";
            } else {
                if (items.size() == 2) {
                    join = " and ";
                } else {
                    join = ", and ";
                }
            }
            sb.append(join);
            sb.append(items.get(i));
        }
        return sb.toString();
    }

    private String featureOrTerrain(DungeonSquare square) {
        if (square.feature != null) {
            return TextUtils.format(square.feature.name, 1, false);
        }
        return TextUtils.format(square.terrain.name, 1, false);
    }

    private String makeMessage() {
        Point cursorPosition = targetableSquares.get(targetIdx);
        int x = cursorPosition.x;
        int y = cursorPosition.y;
        GameState gs = backend.getGameState();
        DungeonSquare square = gs.getCurrentMap().map[x][y];

        if (gs.player.canSee(gs, cursorPosition)) {
            StringBuilder sb = new StringBuilder();
            sb.append("you see ");

            List<String> interestingThings = new ArrayList<>();
            Actor actor = gs.getCurrentMap().actorAt(x,y);
            if (actor != null) {
                interestingThings.add(TextUtils.format(actor.name, 1, false));
            }
            if (square.items != null) {
                for (int i = 0; i < square.items.size(); i++) {
                    interestingThings.add(square.items.items.get(i).stringWithInfo());
                }
            }
            if (!interestingThings.isEmpty()) {
                sb.append(makeListString(interestingThings));
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
