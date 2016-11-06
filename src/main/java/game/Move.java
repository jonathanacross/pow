package game;

/**
 * Created by jonathan on 9/25/16.
 */
public class Move extends CommandRequest {
    int dx;
    int dy;

    public Move(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void process(GameBackend backend) {
        GameState gs = backend.getGameState();
        int newx = gs.x + dx;
        int newy = gs.y + dy;
        if (gs.map.map[newy][newx] != '#') {
            gs.x = newx;
            gs.y = newy;
        }
        backend.notifyUpdate();
        //backend.shootArrow();
    }
}
