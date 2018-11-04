//package pow.backend.actors;
//
//import pow.backend.GameBackend;
//import pow.backend.GameConstants;
//import pow.backend.GameState;
//import pow.backend.action.Attack;
//import pow.backend.action.Action;
//import pow.backend.actors.ai.Movement;
//import pow.backend.actors.ai.StepMovement;
//import pow.backend.behavior.ActionBehavior;
//import pow.backend.behavior.AiBehavior;
//import pow.backend.behavior.Behavior;
//import pow.backend.dungeon.DungeonObject;
//import pow.util.MathUtils;
//
//import java.io.Serializable;
//
//import static pow.util.MathUtils.dist2;
//
//public class Pet extends PlayerActor implements Serializable {
//
//    private int lightRadius;
//    public boolean autoPlay;
//
//    public Pet(DungeonObject.Params objectParams, Actor.Params actorParams, GameState gs) {
//        super(objectParams, actorParams);
//        // TODO: reduce dependence on gs here?
//        this.behavior = new AiBehavior(this, gs);
//        this.autoPlay = true;
//        this.lightRadius = GameConstants.PLAYER_SMALL_LIGHT_RADIUS;
//    }
//
//    @Override
//    public int getLightRadius() {
//        return this.lightRadius;
//    }
//
//    @Override
//    public String getPronoun() {
//        return this.name;
//    }
//
//    @Override
//    public void gainExperience(GameBackend backend, int experience, Actor source) {
//        super.gainExperience(backend, experience, source);
//        // give experience to the player
//        int playerExp = (int) Math.round(experience * 0.6);
//        backend.getGameState().player.gainExperience(backend, playerExp, source);
//    }
//}
