package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;
import pow.util.DebugLogger;

import java.util.ArrayList;
import java.util.List;

public class Attack implements CommandRequest {
    Actor attacker;
    Actor target;

    public Attack(Actor attacker, Actor target) {
        this.attacker = attacker;
        this.target = target;
    }

    // TODO: refactor this out so it's useful for monsters as well
    public static List<GameEvent> doAttack(GameBackend backend, Actor attacker, Actor defender) {
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
                DebugLogger.info("removing defender " + defender.toString());
                DebugLogger.info("actorlist:");
                for (Actor a: gs.map.actors) {
                    DebugLogger.info("   " + a.toString());
                }
                // TODO: would like to call this here: gs.map.actors.remove(defender);
                // but can't because we can't currently modify the actor list

                if (defender == gs.player) {
                    events.add(GameEvent.LOST_GAME);
                }
            }
        }
        return events;
    }

    @Override
    public List<GameEvent> process(GameBackend backend) {
        return doAttack(backend, this.attacker, this.target);
    }
}
