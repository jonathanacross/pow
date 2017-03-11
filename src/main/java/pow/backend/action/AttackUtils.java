package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.gen.ArtifactData;
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

    // TODO: given the cases here, this should eventually be moved into an internal
    // die method within each type of actor
    public static List<GameEvent> doDie(GameBackend backend, Actor actor) {
        GameState gs = backend.getGameState();
        GameMap map = gs.getCurrentMap();
        List<GameEvent> events = new ArrayList<>();

        backend.logMessage(actor.getPronoun() + " died");

        if (actor == gs.player) {
            gs.gameInProgress = false;
            events.add(GameEvent.LostGame());
        } else {
            // see if this is a boss; if so, update the map so it won't regenerate
            if (map.genMonsterIds.canGenBoss && map.genMonsterIds.bossId.equals(actor.id)) {
                map.genMonsterIds.canGenBoss = false;
            }

            // drop any artifacts
            String artifactId = actor.requiredItemDrops;
            if (artifactId != null) {
                DungeonItem item = ArtifactData.getArtifact(artifactId);
                map.map[actor.loc.x][actor.loc.y].items.add(item);
            }

            // with some probability, have the monster drop some random items
            for (int attempt = 0; attempt < actor.numDropAttempts; attempt++) {
                double dropChance = gs.player.increaseWealth ? 0.75 : 0.5;
                if (gs.rng.nextDouble() <= dropChance) {
                    int difficultyLevel = map.level;
                    DungeonItem item = GeneratorUtils.getRandomItemForLevel(difficultyLevel, gs.rng);
                    if (item.flags.money) {
                        if (gs.player.increaseWealth) {
                            item.count *= 3;
                        }
                    }
                    map.map[actor.loc.x][actor.loc.y].items.add(item);
                }
            }

            // Only remove the actor if it's NOT the player,
            // so that the player won't disappear from the map.
            map.removeActor(actor);

            if (actor == gs.player.monsterTarget) {
                gs.player.monsterTarget = null;
            }

        }
        if (actor == gs.pet) {
            gs.pet = null;
        }

        return events;
    }

    public static List<GameEvent> doHit(GameBackend backend, Actor attacker, Actor defender, int damage) {
        List<GameEvent> events = new ArrayList<>();
        backend.logMessage(attacker.getPronoun() + " hit " + defender.getPronoun() + " for " + damage + " damage");
        events.add(GameEvent.Attacked());
        events.addAll(defender.takeDamage(backend, damage));
        return events;
    }
}
