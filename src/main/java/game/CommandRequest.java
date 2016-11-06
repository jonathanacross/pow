package game;

public abstract class CommandRequest {
    public abstract void process(GameBackend backend);
}
