package pow.backend;

import pow.backend.dungeon.DungeonItem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// Holds the set of artifacts that the player/party owns.
public class Artifacts implements Serializable {
    private final Map<DungeonItem.ArtifactSlot, DungeonItem> artifacts;

    public Artifacts() {
        this.artifacts = new HashMap<>();
    }

    public int getLightRadius() {
        int lightRadius = GameConstants.PLAYER_SMALL_LIGHT_RADIUS;
        if (artifacts.containsKey(DungeonItem.ArtifactSlot.LANTERN)) {
            lightRadius = GameConstants.PLAYER_MED_LIGHT_RADIUS;
        }
        if (artifacts.containsKey(DungeonItem.ArtifactSlot.LANTERN2)) {
            lightRadius = GameConstants.PLAYER_LARGE_LIGHT_RADIUS;
        }
        return lightRadius;
    }

    public Map<DungeonItem.ArtifactSlot, DungeonItem> getArtifacts() {
        return artifacts;
    }

    public void add(DungeonItem item) {
        artifacts.put(item.artifactSlot, item);
    }

    public boolean hasFloat() { return artifacts.containsKey(DungeonItem.ArtifactSlot.FLOAT); }
    public boolean hasGlasses() { return artifacts.containsKey(DungeonItem.ArtifactSlot.GLASSES); }
    public boolean hasBag() { return artifacts.containsKey(DungeonItem.ArtifactSlot.BAG); }
    public boolean hasPickAxe() { return artifacts.containsKey(DungeonItem.ArtifactSlot.PICKAXE); }
    public boolean hasGasMask() {
        return artifacts.containsKey(DungeonItem.ArtifactSlot.GASMASK);
    }
    public boolean hasHeatSuit() { return artifacts.containsKey(DungeonItem.ArtifactSlot.HEATSUIT); }
    public boolean hasMap() { return artifacts.containsKey(DungeonItem.ArtifactSlot.MAP); }
    public boolean hasKey() { return artifacts.containsKey(DungeonItem.ArtifactSlot.KEY); }
    public boolean hasPortalKey() { return artifacts.containsKey(DungeonItem.ArtifactSlot.PORTALKEY); }
    public boolean hasXRayScope() { return artifacts.containsKey(DungeonItem.ArtifactSlot.XRAYSCOPE); }
    public boolean hasAllPearls() {
        return  artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL1) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL2) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL3) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL4) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL5) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL6) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL7) &&
                artifacts.containsKey(DungeonItem.ArtifactSlot.PEARL8);
    }

}
