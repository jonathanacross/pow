package pow.backend;

import pow.backend.action.Action;
import pow.backend.action.ModifyTerrain;
import pow.backend.actors.Actor;
import pow.backend.dungeon.gen.TerrainData;
import pow.util.Point;
import pow.util.direction.Direction;

import java.io.Serializable;

public class ActionParams implements Serializable {

    public String actionName;
    public Point point;
    public Direction dir;
    public int number;
    public String name;

    public ActionParams() {
        this.name = null;
        this.point = null;
        this.dir = null;
        this.number = -1;
        this.name = null;
    }

    public Action buildAction(Actor actor, ActionParams params) {
        switch (actionName) {
            //TODO:change dig->modifyTerrain
            case "dig": return new ModifyTerrain(actor, params.point, TerrainData.getTerrain(params.name));
//            case "modifyFeature": return new ModifyFeatureAction(params.point, params.name);
//            case "heal": return new HealAction(params.number);
//            case "restoreMana": return new RestoreManaAction(params.number);
            default: throw new RuntimeException("unknown action name " + actionName);
        }
    }
}
