package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.actors.Actor;
import pow.backend.actors.Knowledge;
import pow.backend.actors.Player;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.MonsterDisplay;
import pow.util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class MonsterInfoWindow extends AbstractWindow {

    private Actor actor;

    public MonsterInfoWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        this.actor = null;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @Override
    public void processKey(KeyEvent e) { }

    @Override
    public void drawContents(Graphics graphics) {
        if (actor == null) {
            return;
        }

        Player player = backend.getGameState().party.player;
        Knowledge.MonsterSummary monsterSummary = new Knowledge.MonsterSummary(actor);

        // actual drawing here
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        MonsterDisplay.drawMonsterInfo(
                graphics,
                monsterSummary,
                player,
                true,
                dim.width,
                new Point(0,0));
    }
}
