//package pow.backend.actors;
//
//import pow.backend.GameBackend;
//import pow.backend.GameState;
//import pow.backend.action.Action;
//import pow.backend.behavior.ActionBehavior;
//import pow.backend.dungeon.ItemList;
//import pow.backend.dungeon.LightSource;
//import pow.util.Point;
//
//import java.io.Serializable;
//
//// A playable actor, notably a pet or the main character.
//public abstract class PlayerActor extends Actor implements Serializable, LightSource {
//
//    // computed as totals in MakePlayerExpLevels
//    protected static final int[] levelBreakpoints = {
//            0,
//            40,
//            256,
//            661,
//            1293,
//            2213,
//            3503,
//            5272,
//            7661,
//            10853,
//            15086,
//            20664,
//            27982,
//            37546,
//            50006,
//            66197,
//            87191,
//            114362,
//            149470,
//            194771,
//            253152
//    };
//
//    // TODO: there's a name conflict between this type of experience (how much you have)
//    // vs the Actor experience (how much you're worth if you die).
//    public int experience;
//
//    public final ItemList equipment;
//
//    @Override
//    public Action act(GameBackend backend) {
//        return behavior.getAction();
//    }
//
//    public void addCommand(Action request) {
//        this.behavior = new ActionBehavior(this, request);
//    }
//
//    @Override
//    public boolean needsInput(GameState gameState) {
//        if (this.behavior != null && !this.behavior.canPerform(gameState)) {
//            clearBehavior();
//        }
//        return behavior == null;
//    }
//
//    @Override
//    public String getPronoun() {
//        return null;
//    }
//
//    @Override
//    public Point getLocation() { return this.loc; }
//
//
//    // gets the experience level the character should have
//    // based on experience alone.
//    private int getTargetLevel() {
//        int targetLevel = 0;
//        for (int i = 0; i < levelBreakpoints.length; i++) {
//            if (experience >= levelBreakpoints[i]) {
//                targetLevel = i;
//            }
//        }
//        return targetLevel;
//    }
//}
