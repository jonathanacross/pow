package pow.backend.command;

import pow.backend.GameBackend;
import pow.backend.event.GameEvent;

import java.util.Arrays;
import java.util.List;

public class FireRocket implements CommandRequest {
    @Override
    public List<GameEvent> process(GameBackend backend) {
        return Arrays.asList(GameEvent.ROCKET);
    }
}
