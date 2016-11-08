package game.backend.command;

import game.GameBackend;

public abstract class CommandRequest {
    public abstract void process(GameBackend backend);
}
