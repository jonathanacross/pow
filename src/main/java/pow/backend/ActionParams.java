package pow.backend;

import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.backend.conditions.ConditionTypes;
import pow.backend.dungeon.gen.FeatureData;
import pow.backend.utils.GeneratorUtils;
import pow.backend.dungeon.gen.TerrainData;
import pow.util.Direction;
import pow.util.Point;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

public class ActionParams implements Serializable {

    public enum ActionName {
        NO_ACTION,
        MODIFY_TERRAIN_ACTION,
        MODIFY_FEATURE_ACTION,
        MODIFY_SPEED_ACTION,
        MOVE_TO_AREA_ACTION,
        POISON_ACTION,
        HEAL_ACTION,
        HEROISM_ACTION,
        AGILITY_ACTION,
        RESTORE_HEALTH_ACTION,
        RESTORE_MANA_ACTION,
        RESTORE_ACTION,
        UNLOCK_DOOR_ACTION,
        ENTER_SHOP_ACTION,
        ENTER_PORTAL_ACTION,
        OPEN_PORTAL_ACTION,
        DROP_PEARL_ACTION,
        DEBUG_ACTION,
    }

    public ActionName actionName;
    public Point point;
    private final Direction dir;
    public int number;
    public String name;

    public ActionParams() {
        this.actionName = ActionName.NO_ACTION;
        this.point = null;
        this.dir = null;
        this.number = -1;
        this.name = null;
    }

    public ActionParams(ActionParams other) {
        this.actionName = other.actionName;
        this.point = other.point;
        this.dir = other.dir;
        this.number = other.number;
        this.name = other.name;
    }

    public static Action buildAction(Actor actor, ActionParams params) {
        switch (params.actionName) {
            case MODIFY_TERRAIN_ACTION:
                return new ModifyTerrain(actor, params.point, TerrainData.getTerrain(params.name));
            case MODIFY_FEATURE_ACTION:
                return new ModifyFeature(actor, params.point, FeatureData.getFeature(params.name));
            case MOVE_TO_AREA_ACTION:
                return new GotoArea(params.name, params.point);
            case HEAL_ACTION:
                return new Heal(actor, params.number);
            case RESTORE_HEALTH_ACTION:
                return new RestoreHealth(actor, params.number);
            case RESTORE_MANA_ACTION:
                return new RestoreMana(actor, params.number);
            case RESTORE_ACTION:
                return new Restore(actor, params.number);
            case POISON_ACTION:
                return new StartCondition(actor, Collections.singletonList(ConditionTypes.POISON), 5, params.number);
            case MODIFY_SPEED_ACTION:
                return new StartCondition(actor, Collections.singletonList(ConditionTypes.SPEED), 30, params.number);
            case HEROISM_ACTION:
                return new StartCondition(
                        actor,
                        Arrays.asList(ConditionTypes.HEALTH, ConditionTypes.TO_DAM),
                        30,
                        params.number);
            case AGILITY_ACTION:
                return new StartCondition(
                        actor,
                        Arrays.asList(ConditionTypes.DEFENSE, ConditionTypes.TO_HIT),
                        30,
                        params.number);
            case UNLOCK_DOOR_ACTION:
                return new UnlockDoor(actor, params.point, params.number, FeatureData.getFeature(params.name));
            case ENTER_SHOP_ACTION:
                return new EnterShop(actor, ShopData.ShopState.parseFromString(params.name));
            case OPEN_PORTAL_ACTION:
                return new OpenPortal(actor, params.point, GeneratorUtils.buildOpenPortalFeature(params.name));
            case ENTER_PORTAL_ACTION:
                return new EnterPortal(actor);
            case DROP_PEARL_ACTION:
                return new DropPearl(actor, params.point);
            case DEBUG_ACTION:
                return new DebugAction(DebugAction.What.valueOf(params.name));
            default:
                throw new RuntimeException("tried to create unknown action: " + params.actionName);
        }
    }
}
