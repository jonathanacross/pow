package pow.backend.dungeon.gen.mapgen;

public class TerrainFeatureTriplet {
    public final String terrain;
    public final String feature1;
    public final String feature2;

    public TerrainFeatureTriplet(String terrain, String feature1, String feature2) {
        this.terrain = terrain;
        this.feature1 = feature1;
        this.feature2 = feature2;
    }

    // just a rough thing for debugging
    @Override
    public String toString() {
        char t = terrain == null ? '_' : terrain.charAt(0);
        char f1 = feature1 == null ? '_' : feature1.charAt(0);
        char f2 = feature2 == null ? '_' : feature2.charAt(0);
        return "" + t + f1 + f2;
    }
}
