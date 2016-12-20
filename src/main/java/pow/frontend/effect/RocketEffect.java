package pow.frontend.effect;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;

import java.util.ArrayList;
import java.util.List;

// demo effect: shoots an exploding arrow
public class RocketEffect implements Effect {

    private int arrowDist;
    private int explodeDist;
    private int x;
    private int y;
    private final static int DISTANCE = 7;
    private final static int EXPLODE_SIZE = 3;

    public RocketEffect(Actor actor) {
        this.x = actor.x;
        this.y = actor.y;
        this.arrowDist = 1;
        this.explodeDist = 0;
    }

    @Override
    public boolean update() {
        if (arrowDist < DISTANCE) {
            arrowDist++;
            return true;
        } else if (explodeDist < EXPLODE_SIZE) {
            explodeDist++;
            return true;
        }
        return false;
    }

    @Override
    public List<GlyphLoc> render() {
        List<GlyphLoc> glyphLocs = new ArrayList<>();
        if (arrowDist < DISTANCE) {
           glyphLocs.add(new GlyphLoc(x, y + arrowDist, "N purple bolt"));
        } else {
            for (int i = -explodeDist; i <= explodeDist; i++) {
                for (int j = -explodeDist; j <= explodeDist; j++) {
                    if (i*i + j*j <= explodeDist*explodeDist) {
                        glyphLocs.add(new GlyphLoc(x + i, y + arrowDist + j, "big purple ball"));
                    }
                }
            }
        }
        return glyphLocs;
    }
}
