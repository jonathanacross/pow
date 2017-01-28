package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class Attack implements Action {
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

    public static ActionResult doAttack(GameBackend backend, Actor attacker, Actor defender) {
        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();
        if (gs.rng.nextDouble() > hitProb(attacker.toHit, defender.defense)) {
            backend.logMessage(attacker.getPronoun() + " misses " + defender.getPronoun());
        } else {
            int damage = attacker.attackDamage.rollDice(gs.rng);
            if (damage == 0) {
                backend.logMessage(attacker.getPronoun() + " misses " + defender.getPronoun());
            } else {

                backend.logMessage(attacker.getPronoun() + " hit " + defender.getPronoun() + " for " + damage + " damage");

                events.add(GameEvent.Attacked());

                defender.takeDamage(backend, damage);
                // TODO: this logic will likely be common for other types of attacks, so should
                // be put into actor.takeDamage().  However, we need to add the list of game
                // events directly to the backend, rather than returning.
                if (defender.health < 0) {
                    backend.logMessage(defender.getPronoun() + " died");

                    if (defender == gs.player) {
                        gs.gameInProgress = false;
                        events.add(GameEvent.LostGame());
                    } else {
                        // Only remove the actor if it's NOT the player,
                        // so that the player won't disappear from the map.
                        gs.world.currentMap.removeActor(defender);
                    }
                    if (defender == gs.pet) {
                        gs.pet = null;
                    }
                }
            }
        }
        return ActionResult.Succeeded(events);
    }

    public static double hitProb(int dex, int defense) {
        return 1.0 / (1.0 + Math.exp(-(dex - defense) * 0.2));
    }

    @Override
    public ActionResult process(GameBackend backend) {
        return doAttack(backend, this.attacker, this.target);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
