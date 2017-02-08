package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class AttackUtils {
    public static double hitProb(int toHit, int defense) {
        // squaring everything pushes away from 50% probability faster,
        // so changes between toHit and defense are more significant.
        double z = (double) (toHit * toHit) / (toHit * toHit + defense * defense);
        return z;
    }

    public static List<GameEvent> doHit(GameBackend backend, Actor attacker, Actor defender, int damage) {

        GameState gs = backend.getGameState();
        List<GameEvent> events = new ArrayList<>();

        backend.logMessage(attacker.getPronoun() + " hit " + defender.getPronoun() + " for " + damage + " damage");
        events.add(GameEvent.Attacked());
        defender.takeDamage(backend, damage);

        if (defender.health < 0) {
            backend.logMessage(defender.getPronoun() + " died");

            if (defender == gs.player) {
                gs.gameInProgress = false;
                events.add(GameEvent.LostGame());
            } else {
                attacker.gainExperience(backend, defender.experience);

                GameMap map = gs.getCurrentMap();

                // TODO: drop any gold that the monster holds

                // with some probability, have the monster drop a random item.
                if (gs.rng.nextInt(8) == 0) {
                    int difficultyLevel = map.level;
                    DungeonItem item = GeneratorUtils.getRandomItemForLevel(difficultyLevel, gs.rng);
                    map.map[defender.loc.x][defender.loc.y].items.add(item);
                }

                // Only remove the actor if it's NOT the player,
                // so that the player won't disappear from the map.
                map.removeActor(defender);

                if (defender == gs.player.monsterTarget) {
                    gs.player.monsterTarget = null;
                }

            }
            if (defender == gs.pet) {
                gs.pet = null;
            }
        }

        return events;
    }
}
