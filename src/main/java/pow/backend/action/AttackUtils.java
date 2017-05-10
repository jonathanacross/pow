package pow.backend.action;

import pow.backend.*;
import pow.backend.actors.Actor;
import pow.backend.conditions.ConditionTypes;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.gen.ArtifactData;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttackUtils {
    public static double hitProb(int toHit, int defense) {
        // squaring everything pushes away from 50% probability faster,
        // so changes between toHit and defense are more significant.
        double z = (double) (toHit * toHit) / (toHit * toHit + defense * defense);
        return z;
    }

    // Adjusts the damage based on the damage type and the defender's resistances
    private static int adjustDamage(int baseDamage, SpellParams.Element element, Actor defender) {
        int bonus;
        switch (element) {
            case FIRE: bonus = defender.baseStats.resFire; break;
            case ICE: bonus = defender.baseStats.resCold; break;
            case ACID: bonus = defender.baseStats.resAcid; break;
            case LIGHTNING: bonus = defender.baseStats.resElec; break;
            case POISON: bonus = defender.baseStats.resPois; break;
            default: bonus = 0;
        }

        double scale = Math.pow(0.7, bonus);
        return (int) Math.round(scale * baseDamage);
    }

    // TODO: given the cases here, this should eventually be moved into an internal
    // die method within each type of actor
    public static GameEvent doDie(GameBackend backend, Actor actor) {
        GameState gs = backend.getGameState();
        GameMap map = gs.getCurrentMap();

        backend.logMessage(actor.getPronoun() + " died");

        if (actor == gs.player) {
            gs.gameInProgress = false;
            return GameEvent.LostGame();
        }

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
            double dropChance = gs.player.increaseWealth ? GameConstants.BONUS_MONSTER_DROP_CHANCE : GameConstants.MONSTER_DROP_CHANCE;
            if (gs.rng.nextDouble() <= dropChance) {
                int difficultyLevel = map.level;
                DungeonItem item = GeneratorUtils.getRandomItemForLevel(difficultyLevel, gs.rng);
                map.map[actor.loc.x][actor.loc.y].items.add(item);
            }
        }

        // with some probability, drop some gold
        double multiplier = gs.player.increaseWealth ? GameConstants.BONUS_GOLD_DROP_RATE_MULTIPLIER : 1.0;
        double goldDropChance =
                1 - (multiplier * Math.pow(1 - GameConstants.MONSTER_GOLD_DROP_CHANCE, actor.numDropAttempts + 1));
        if (gs.rng.nextDouble() <= goldDropChance) {
            int difficultyLevel = map.level;
            DungeonItem item = GeneratorUtils.getRandomMoneyForLevel(difficultyLevel, gs.rng);
            map.map[actor.loc.x][actor.loc.y].items.add(item);
        }

        // Only remove the actor if it's NOT the player,
        // so that the player won't disappear from the map.
        map.removeActor(actor);

        if (actor == gs.player.monsterTarget) {
            gs.player.monsterTarget = null;
        }
        if (actor == gs.pet) {
            gs.pet = null;
        }

        return GameEvent.Killed();
    }

    public static List<GameEvent> doSimpleHit(GameBackend backend, Actor attacker, Actor defender,
                                        SpellParams.Element element, int damage) {
        String damTypeString = "";
        if (element != SpellParams.Element.NONE && element != SpellParams.Element.DAMAGE) {
            damTypeString = " " + element.toString().toLowerCase();
        }
        int adjustedDamage = adjustDamage(damage, element, defender);
        backend.logMessage(attacker.getPronoun() + " hit " + defender.getPronoun() + " for " + adjustedDamage + damTypeString + " damage");
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.Attacked());
        List<GameEvent> damageEvents = defender.takeDamage(backend, adjustedDamage);
        for (GameEvent event : damageEvents) {
            if (event.eventType == GameEvent.EventType.KILLED) {
                attacker.gainExperience(backend, defender.experience, defender);
            }
        }
        events.addAll(damageEvents);

        return events;
    }

    public static List<GameEvent> doHit(GameBackend backend, Actor attacker, Actor defender,
                                        SpellParams.Element element, int damage) {

        List<GameEvent> events = new ArrayList<>();
        switch (element) {
            case CONFUSE:
                // TODO: pull in spell params for duration/intensity?
                if (backend.getGameState().rng.nextInt(defender.level + 1) == 0) {
                    // TODO: have this not affect the caster?
                    events.addAll(defender.conditions.get(ConditionTypes.CONFUSE).start(backend, 15, 1));
                } else {
                    backend.logMessage(attacker.getPronoun() + " failed to confuse " + defender.getPronoun());
                }
                break;
            case SLEEP:
                if (backend.getGameState().rng.nextInt(defender.level + 1) == 0) {
                    defender.putToSleep(backend);
                } else {
                    backend.logMessage(attacker.getPronoun() + " failed to put " + defender.getPronoun() + " to sleep");
                }
                break;
            case STUN:
                // TODO: pull in spell params for duration/intensity?  damage is probably too much for intensity.
                events.addAll(defender.conditions.get(ConditionTypes.STUN).start(backend, 15, damage));
                events.addAll(doSimpleHit(backend, attacker, defender, element, damage));
                break;
            case POISON:
                // TODO: pull in spell params for duration/intensity? -- intensity is too low to be meaningful for high-lev monsters. need tuning
                events.addAll(defender.conditions.get(ConditionTypes.POISON).start(backend, 15, 1));
                events.addAll(doSimpleHit(backend, attacker, defender, element, damage));
            default:
                events.addAll(doSimpleHit(backend, attacker, defender, element, damage));
        }
       return events;
    }
}
