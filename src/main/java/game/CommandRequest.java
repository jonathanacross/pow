package game;

/**
 * Created by jonathan on 9/25/16.
 */
public abstract class CommandRequest {
    public abstract void process(GameBackend backend);
}
