package pow.backend.dungeon;

import pow.util.Point;

import java.io.Serializable;

public class DungeonObject implements Serializable {
    public final String id;   // program id, e.g., "axe"
    public final String name; // english name, e.g., "& axe~"
    public final String image; // for display
    public final String description;
    public Point loc;
    public final boolean solid;

    // This class repeats the members above; it's just helpful
    // to reduce the number of constructor parameters.
    public static class Params {
        public final String id;   // program id, e.g., "axe"
        public final String name; // english name, e.g., "& axe~"
        public final String image; // for display
        public final String description;
        public final Point loc;
        public final boolean solid;

        public Params(String id, String name, String image, String description, Point loc, boolean solid) {
            this.id = id;
            this.name = name;
            this.image = image;
            this.description = description;
            this.loc = loc;
            this.solid = solid;
        }
    }

    public DungeonObject(Params params) {
        this.id = params.id;
        this.name = params.name;
        this.image = params.image;
        this.description = params.description;
        this.loc = params.loc;
        this.solid = params.solid;
    }
}
