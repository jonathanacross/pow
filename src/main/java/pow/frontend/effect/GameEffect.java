package pow.frontend.effect;

import pow.backend.dungeon.DungeonObject;

import java.util.ArrayList;
import java.util.List;

public class GameEffect implements Effect {
    private List<DungeonObject> objects;

    public GameEffect(List<DungeonObject> objects) {
        this.objects = objects;
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public List<GlyphLoc> render() {
        List<GlyphLoc> glyphLocs = new ArrayList<>();
        for (DungeonObject object : objects) {
            glyphLocs.add(new GlyphLoc(object.loc.x, object.loc.y, object.image));
        }
        return glyphLocs;
    }
}
