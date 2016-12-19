package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class Attack implements CommandRequest {
    Actor attacker;
    Actor target;

    public Attack(Actor attacker, Actor target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public Actor getActor() {
        return this.attacker;
    }

    // TODO: refactor this out so it's useful for monsters as well
    public static ActionResult doAttack(GameBackend backend, Actor attacker, Actor defender) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        if (gs.rng.nextBoolean()) {
            backend.logMessage(attacker.getPronoun() + " misses " + defender.getPronoun());
        } else {
            int damage = 1;

            backend.logMessage(attacker.getPronoun() + " hit " + defender.getPronoun() + " for " + damage + " damage");

            events.add(GameEvent.ATTACKED);

            defender.health -= damage;
            if (defender.health < 0) {
                backend.logMessage(defender.getPronoun() + " died");
//                DebugLogger.info("removing defender " + defender.toString());
//                DebugLogger.info("actorlist:");
//                for (Actor a: gs.map.actors) {
//                    DebugLogger.info("   " + a.toString());
//                }
                gs.map.removeActor(defender);

                if (defender == gs.player) {
                    events.add(GameEvent.LOST_GAME);
                }
            }
        }
        return ActionResult.Succeeded(events);
    }

    @Override
    public ActionResult process(GameBackend backend) {
        return doAttack(backend, this.attacker, this.target);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
