package pow.frontend.effect;

import java.util.List;

public interface Effect {
    boolean update();
    List<GlyphLoc> render();
}
