package game.backend.command;

import game.GameBackend;
import game.GameState;

public class StartGame extends CommandRequest {
    @Override
    public void process(GameBackend backend) {
        GameState gs = backend.getGameState();
        gs.metaGameState = GameState.MetaGameState.IN_GAME;
    }
}
