package pow.backend;

import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.backend.dungeon.gen.FeatureData;
import pow.backend.dungeon.gen.TerrainData;
import pow.util.Point;
import pow.util.Direction;

import java.io.Serializable;

public class ActionParams implements Serializable {

    public String actionName;
    public Point point;
    public Direction dir;
    public int number;
    public String name;

    public ActionParams() {
        this.actionName = null;
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
            //TODO:change dig->modifyTerrain
            case "dig": return new ModifyTerrain(actor, params.point, TerrainData.getTerrain(params.name));
            case "modifyFeature": return new ModifyFeature(actor, params.point, FeatureData.getFeature(params.name));
            case "gotoArea": return new GotoArea(params.name, params.point );
            case "heal": return new Heal(actor, params.number);
            case "enterShop": return new EnterShop(actor, ShopData.ShopState.parseFromString(params.name));
//            case "restoreMana": return new RestoreManaAction(params.number);
            default: throw new RuntimeException("unknown action name " + params.actionName);
        }
    }
}
