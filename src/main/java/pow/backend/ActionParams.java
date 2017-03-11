package pow.backend;

import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.backend.conditions.Poison;
import pow.backend.dungeon.gen.FeatureData;
import pow.backend.dungeon.gen.TerrainData;
import pow.util.Direction;
import pow.util.Point;

import java.io.Serializable;

public class ActionParams implements Serializable {

    public enum ActionName {
        NO_ACTION,
        MODIFY_TERRAIN_ACTION,
        MODIFY_FEATURE_ACTION,
        MODIFY_SPEED_ACTION,
        MOVE_TO_AREA_ACTION,
        POISON_ACTION,
        HEAL_ACTION,
        RESTORE_MANA_ACTION,
        RESTORE_ACTION,
        ENTER_SHOP_ACTION
    }

    public ActionName actionName;
    public Point point;
    public Direction dir;
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
            case MODIFY_TERRAIN_ACTION: return new ModifyTerrain(actor, params.point, TerrainData.getTerrain(params.name));
            case MODIFY_FEATURE_ACTION: return new ModifyFeature(actor, params.point, FeatureData.getFeature(params.name));
            case MODIFY_SPEED_ACTION: return new StartCondition(actor, StartCondition.ConditionType.SPEED, 30, params.number);
            case MOVE_TO_AREA_ACTION: return new GotoArea(params.name, params.point );
            case POISON_ACTION: return new StartCondition(actor, StartCondition.ConditionType.POISON, 5, params.number);
            case HEAL_ACTION: return new Heal(actor, params.number);
            case RESTORE_MANA_ACTION: return new RestoreMana(actor, params.number);
            case RESTORE_ACTION: return new Restore(actor, params.number);
            case ENTER_SHOP_ACTION: return new EnterShop(actor, ShopData.ShopState.parseFromString(params.name));
            default: throw new RuntimeException("tried to create unknown action: " + params.actionName);
        }
    }
}
