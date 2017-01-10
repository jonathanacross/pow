package pow.backend.actors;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.action.Action;
import pow.backend.dungeon.DungeonObject;
import pow.backend.dungeon.LightSource;
import pow.util.Circle;
import pow.util.MathUtils;
import pow.util.Point;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class Player extends Actor implements Serializable, LightSource {
    private Queue<Action> actionQueue;

    public int viewRadius;
    public int lightRadius;

    public Player(DungeonObject.Params objectParams, Actor.Params actorParams) {
        super(objectParams, actorParams);
        this.actionQueue = new LinkedList<>();
        this.viewRadius = 11;  // how far can you see, assuming things are lit
        this.lightRadius = 8;  // 3 = candle (starting), 8 = lantern, 13 = bright lantern
    }

    public void addCommand(Action request) {
        this.actionQueue.add(request);
    }

    public boolean canSee(GameState gs, Point point) {
        // must be within the player's view radius, and must be lit
        return ((MathUtils.dist2(loc, point) <= Circle.getRadiusSquared(viewRadius)) &&
                (gs.world.currentMap.map[point.x][point.y].brightness > 0));
    }

    @Override
    public String getPronoun() {
        return "you";
    }

    @Override
    public boolean needsInput() {
        return actionQueue.isEmpty();
    }

    @Override
    public Action act(GameBackend backend) {
        return this.actionQueue.poll();
    }

    @Override
    public Point getLocation() { return this.loc; }

    @Override
    public int getLightRadius() {
        return this.lightRadius;
    }
}
