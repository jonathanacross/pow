package pow.backend.dungeon;

import java.io.Serializable;

// Similar to DungeonExit, but may be disabled.
public class Portal implements Serializable {
    // Area/location in the area where the portal goes to.
    public String areaId;
    public String locName;

    // Indicates if the portal is enabled.
    public boolean enabled;

    // String serialization/deserialization, useful to
    // to make as part of an ActionParams.

    @Override
    public String toString() { return areaId + "|" + locName; }

    public Portal(String encoded) {
        String[] fields = encoded.split("\\|");
        this.areaId = fields[0];
        this.locName = fields[1];
        this.enabled = fields[2].equals("enabled");
    }

    public Portal(String areaId, String locName, boolean enabled) {
        this.areaId = areaId;
        this.locName = locName;
        this.enabled = enabled;
    }
}
