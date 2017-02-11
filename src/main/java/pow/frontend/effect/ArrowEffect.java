package pow.frontend.effect;

import pow.backend.actors.Actor;
import pow.util.Bresenham;
import pow.util.Point;
import pow.util.direction.DirectionNames;

import java.util.ArrayList;
import java.util.List;

// TODO: effects currently must have duplicated code with the
// backend -- e.g., arrow path, explosion sizes, etc.
// Need to move effects into the backend.

// shoots an arrow
public class ArrowEffect implements Effect {

    private int idx;
    private List<Point> points;
    private String glyphName;

    public ArrowEffect(Actor actor, Point point) {
        this.idx = 0;

        // get the path of the arrow
        List<Point> ray = Bresenham.makeRay(actor.loc, point, 100);
        points = new ArrayList<>();
        // remove starting point of actor, and remove all points after 'point'
        ray.remove(0);
        for (Point p : ray) {
            points.add(p);
            if (p.equals(point)) break;
        }

        // figure out the glyph to use
        int dir = DirectionNames.getDirName(actor.loc, point);
        String[] arrowGlyphIds = {
                "N arrow",
                "NE arrow",
                "E arrow",
                "SE arrow",
                "S arrow",
                "SW arrow",
                "W arrow",
                "NW arrow"
        };
        glyphName = arrowGlyphIds[dir];
    }

    @Override
    public boolean update() {
        if (idx < points.size() - 1) {
            idx++;
            return true;
        }
        return false;
    }

    @Override
    public List<GlyphLoc> render() {
        List<GlyphLoc> glyphLocs = new ArrayList<>();
        Point p = points.get(idx);
        glyphLocs.add(new GlyphLoc(p.x, p.y, glyphName));
        return glyphLocs;
    }
}
