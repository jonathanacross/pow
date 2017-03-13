package pow.backend.dungeon;

// Class to encode where a particular exit of a dungeon goes,
// consisting of the area id, and a location in that area.
// Each area has a map of location name to a particular point.
// Restrictions: area id and location name cannot have '|' in them.
public class DungeonExit {

    // information about where the exit goes to.
    public final String areaId;
    public final String locName;

    public DungeonExit(String areaId, String locName) {
        this.areaId = areaId;
        this.locName = locName;
    }

    // string serialization/deserialization, useful to
    // to make as part of an ActionParams.

    @Override
    public String toString() { return areaId + "|" + locName; }

    public DungeonExit(String encoded) {
        String[] fields = encoded.split("\\|");
        this.areaId = fields[0];
        this.locName = fields[1];
    }
}
