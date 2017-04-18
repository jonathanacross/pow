package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameMap;
import pow.backend.GameState;
import pow.backend.SpellParams;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonEffect;
import pow.backend.event.GameEvent;
import pow.util.Direction;
import pow.util.Metric;
import pow.util.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CircleCut implements Action {

    private final Actor attacker;
    private final SpellParams spellParams;

    public CircleCut(Actor attacker, SpellParams spellParams) {
        this.attacker = attacker;
        this.spellParams = spellParams;
    }

    @Override
    public Actor getActor() {
        return this.attacker;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        GameMap map = gs.getCurrentMap();

        List<Point> fovSquares = SpellUtils.getFieldOfView(gs, attacker.loc, spellParams.size, Metric.rogueMetric);
        Set<Point> fovSquareSet = new HashSet<>();
        fovSquareSet.addAll(fovSquares);

        for (Direction dir : Direction.ALL) {
            String effectId = DungeonEffect.getEffectName(
                    DungeonEffect.EffectType.BOLT,
                    SpellUtils.getEffectColor(spellParams.element),
                    dir);
            List<Point> squares = getHitSquares(attacker.loc.x, attacker.loc.y, dir, spellParams.size, fovSquareSet);

            for (Point p : squares) {
                Actor defender = map.actorAt(p.x, p.y);
                if (defender != null) {
                    int damage = spellParams.getAmount(attacker);
                    backend.logMessage(attacker.getPronoun() + " hits " + defender.getPronoun());
                    List<GameEvent> hitEvents = AttackUtils.doHit(backend, attacker, defender, damage);
                    events.addAll(hitEvents);
                }
            }
            events.add(GameEvent.Effect(new DungeonEffect(effectId, squares)));
        }

        events.add(GameEvent.DungeonUpdated());

        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return true; }

    private List<Point> getHitSquares(int x, int y, Direction dir, int size, Set<Point> fovSquares) {
        List<Point> squares = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= i; j++) {
                switch (dir) {
                    case N:
                        squares.add(new Point(x+1-j, y-i));
                        break;
                    case NE:
                        squares.add(new Point(x+j, y-i));
                        break;
                    case E:
                        squares.add(new Point(x+i, y+1-j));
                        break;
                    case SE:
                        squares.add(new Point(x+i, y+j));
                        break;
                    case S:
                        squares.add(new Point(x-1+j, y+i));
                        break;
                    case SW:
                        squares.add(new Point(x-j, y+i));
                        break;
                    case W:
                        squares.add(new Point(x-i, y-1+j));
                        break;
                    case NW:
                        squares.add(new Point(x-i, y-j));
                        break;
                    default:
                        break;
                }
            }
        }

        List<Point> fovAndHit = new ArrayList<>();
        for (Point s : squares) {
            if (fovSquares.contains(s)) {
                fovAndHit.add(s);
            }
        }

        return fovAndHit;
    }

}
