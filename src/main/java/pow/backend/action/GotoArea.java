package pow.backend.action;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.backend.utils.GeneratorUtils;
import pow.backend.dungeon.gen.ShopGenerator;
import pow.backend.event.GameEvent;
import pow.util.Point;

import java.util.ArrayList;
import java.util.List;

public class GotoArea implements Action {
    private final String areaName;
    private final Point loc;

    public GotoArea(String areaName, Point loc) {
        this.areaName = areaName;
        this.loc = loc;
    }

    @Override
    public ActionResult process(GameBackend backend) {
        GameState gs = backend.getGameState();

        // clear any targets
        gs.party.player.floorTarget = null;
        gs.party.player.monsterTarget = null;

        // remove player and pet from current area
        gs.getCurrentMap().removeActor(gs.party.player);
        if (gs.party.pet != null) {
            gs.getCurrentMap().removeActor(gs.party.pet);
        }

        // set the new area, update monsters/shops if needed
        boolean needsRegen = gs.world.recentMaps.setMap(gs.world.world.get(areaName));
        if (needsRegen) {
            GeneratorUtils.regenMonstersForCurrentMap(gs.getCurrentMap(), gs.rng);
            gs.getCurrentMap().shopData = ShopGenerator.genShop(gs.getCurrentMap().level, gs.rng);
        } else {
            // Even if we don't have to completely regen everything, still
            // refill monsters HP/MP, so the player can't abuse by going up stairs
            // and resting.
            GeneratorUtils.healAllMonsters(gs.getCurrentMap());
        }

        // set up player/pet in the new area
        gs.getCurrentMap().placePlayerAndPet(gs.party.player, loc, gs.party.pet);

        List<GameEvent> events = new ArrayList<>();
        events.add(GameEvent.DungeonUpdated());
        return ActionResult.Succeeded(events);
    }

    @Override
    public boolean consumesEnergy() { return false; }

    @Override
    public Actor getActor() {
        return null;
    }
}
