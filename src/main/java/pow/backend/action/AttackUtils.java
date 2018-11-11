package pow.backend.action;

import pow.backend.*;
import pow.backend.actors.Actor;
import pow.backend.conditions.ConditionTypes;
import pow.backend.dungeon.DungeonItem;
import pow.backend.dungeon.gen.ArtifactData;
import pow.backend.dungeon.gen.GeneratorUtils;
import pow.backend.event.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AttackUtils {
    public static double hitProb(int toHit, int defense) {
        // squaring everything pushes away from 50% probability faster,
        // so changes between toHit and defense are more significant.
        double z = (double) (toHit * toHit) / (toHit * toHit + defense * defense);
        return z;
    }

    // Adjusts the damage based on the damage type and the defender's resistances
    public static int adjustDamage(int baseDamage, SpellParams.Element element, Actor defender) {
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

        MessageLog.MessageType messageType = actor.friendly
                ? MessageLog.MessageType.COMBAT_BAD
                : MessageLog.MessageType.COMBAT_GOOD;
        backend.logMessage(actor.getPronoun() + " died", messageType);

        if (actor == gs.party.player) {
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
            double dropChance = gs.party.player.increaseWealth ? GameConstants.BONUS_MONSTER_DROP_CHANCE : GameConstants.MONSTER_DROP_CHANCE;
            if (gs.rng.nextDouble() <= dropChance) {
                int difficultyLevel = map.level;
                DungeonItem item = GeneratorUtils.getRandomItemForLevel(difficultyLevel, gs.rng);
                map.map[actor.loc.x][actor.loc.y].items.add(item);
            }
        }

        // with some probability, drop some gold
        double multiplier = gs.party.player.increaseWealth ? GameConstants.BONUS_GOLD_DROP_RATE_MULTIPLIER : 1.0;
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

        if (actor == gs.party.player.monsterTarget) {
            gs.party.player.monsterTarget = null;
        }
        if (actor == gs.party.pet) {
            gs.party.pet = null;
        }

        return GameEvent.Killed();
    }

    public static String getDamageTypeString(SpellParams.Element element) {
        String damTypeString = "";
        if (element != SpellParams.Element.NONE && element != SpellParams.Element.DAMAGE) {
            damTypeString = " " + element.toString().toLowerCase();
        }
        return damTypeString;
    }

    public static List<GameEvent> doSimpleHit(GameBackend backend, Actor attacker, Actor defender,
                                        SpellParams.Element element, int damage) {
        String damTypeString = getDamageTypeString(element);
        int adjustedDamage = adjustDamage(damage, element, defender);
        MessageLog.MessageType messageType = defender.friendly
                ? MessageLog.MessageType.COMBAT_BAD
                : MessageLog.MessageType.COMBAT_GOOD;
        backend.logMessage(attacker.getPronoun() + " hit " + defender.getPronoun() + " for " + adjustedDamage + damTypeString + " damage", messageType);
        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.Attacked());
        List<GameEvent> damageEvents = defender.takeDamage(backend, adjustedDamage);
        for (GameEvent event : damageEvents) {
            if (event.eventType == GameEvent.EventType.KILLED) {
                attacker.gainExperience(backend, defender.experience, defender);
                // If the attacker is a party member, give some experience to other chars.
                int partialExp = (int) Math.round(0.75 * defender.experience);
                GameState gs = backend.getGameState();
                if (attacker == gs.party.pet) {
                    gs.party.player.gainExperience(backend, partialExp, defender);
                } else if (attacker == gs.party.player && gs.party.pet != null) {
                    gs.party.pet.gainExperience(backend, partialExp, defender);
                }
            }
        }
        events.addAll(damageEvents);

        return events;
    }

    public static class HitParams {
        public final SpellParams.Element element;
        public final int damage;
        // needed for some types of hits, such as poison, that have lingering effects
        public final int duration;
        public final int intensity;

        public HitParams(SpellParams.Element element, int damage, int duration, int intensity) {
            this.element = element;
            this.damage = damage;
            this.duration = duration;
            this.intensity = intensity;
        }

        // plain physical hit
        public HitParams(int damage) {
            this(SpellParams.Element.NONE, damage, 0, 0);
        }

        public HitParams(SpellParams spellParams, Actor actor, Random rng) {
            this(spellParams.element,
                    spellParams.getPrimaryAmount(actor).rollDice(rng),
                    spellParams.duration,
                    spellParams.getSecondaryAmount(actor));
        }
    }

    public static List<GameEvent> doHit(GameBackend backend, Actor attacker, Actor defender, HitParams hitParams) {

        List<GameEvent> events = new ArrayList<>();
        switch (hitParams.element) {
            case CONFUSE:
                if (backend.getGameState().rng.nextInt(defender.level + 1) == 0) {
                    // TODO: have this not affect the caster?
                    events.addAll(defender.conditions.get(ConditionTypes.CONFUSE).start(backend, hitParams.duration, hitParams.intensity));
                } else {
                    backend.logMessage(attacker.getPronoun() + " failed to confuse " + defender.getPronoun(), MessageLog.MessageType.COMBAT_NEUTRAL);
                }
                break;
            case SLEEP:
                if (backend.getGameState().rng.nextInt(defender.level + 1) == 0) {
                    defender.putToSleep(backend);
                } else {
                    backend.logMessage(attacker.getPronoun() + " failed to put " + defender.getPronoun() + " to sleep", MessageLog.MessageType.COMBAT_NEUTRAL);
                }
                break;
            case STUN:
                events.addAll(defender.conditions.get(ConditionTypes.STUN).start(backend, hitParams.duration, hitParams.intensity));
                events.addAll(doSimpleHit(backend, attacker, defender, hitParams.element, hitParams.damage));
                break;
            case POISON:
                events.addAll(defender.conditions.get(ConditionTypes.POISON).start(backend, hitParams.duration, hitParams.intensity));
                events.addAll(doSimpleHit(backend, attacker, defender, hitParams.element, hitParams.damage));
                break;
            default:
                events.addAll(doSimpleHit(backend, attacker, defender, hitParams.element, hitParams.damage));
        }
       return events;
    }
}
