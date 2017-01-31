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
        if (gs.rng.nextDouble() > hitProb(attacker.attack.plusToHit, defender.defense)) {
            backend.logMessage(attacker.getPronoun() + " misses " + defender.getPronoun());
        } else {
            int damage = attacker.attack.dieRoll.rollDice(gs.rng);
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
                        attacker.gainExperience(backend, defender.experience);

                        // defender needs to drop all items they carry; additionally,
                        // they have some chance of dropping another item.
                        // Or, perhaps generate an item at monster creation time?

                        // Only remove the actor if it's NOT the player,
                        // so that the player won't disappear from the map.
                        gs.getCurrentMap().removeActor(defender);

                    }
                    if (defender == gs.pet) {
                        gs.pet = null;
                    }
                }
            }
        }
        return ActionResult.Succeeded(events);
    }

    public static double hitProb(int toHit, int defense) {
        // add smoothing, so that a player with 0 toHit can still hit something
        // or 0 defense doesn't mean 100% chance of getting hit.
        double smooth = 5.0;
        double z = (toHit + smooth) / (toHit + defense + 2*smooth);
        return z;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        return doAttack(backend, this.attacker, this.target);
    }

    @Override
    public boolean consumesEnergy() { return true; }
}
