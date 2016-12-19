package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.command.ActionResult;
import pow.backend.command.CommandRequest;
import pow.backend.event.GameResult;

import java.util.*;

public class GameBackend {

    private GameState gameState;
    public Deque<CommandRequest> commandQueue = new LinkedList<>();
    //private boolean logChanged;

    public GameState getGameState() {
        return gameState;
    }

    public GameBackend() {
        this.gameState = new GameState("Unknown Adventurer");
    }

    public void tellPlayer(CommandRequest request) {
        gameState.player.addCommand(request);
    }

    // TODO: right now, UI sees monsters move one at a time.  This is
    // sort of cool, but it's slow once there are lots of monsters.
    // Is there a way to move all monsters at once every time step? (I.e., update the UI
    // once each cycle?)
    public GameResult update() {
        boolean madeProgress = false;

        for (;;) {

            // process any ongoing/pending actions
            while (!commandQueue.isEmpty()) {

                // find alternate commands, if needed
                CommandRequest command = commandQueue.peek();
                ActionResult result = command.process(this);
                while (result.alternate != null) {
                    commandQueue.removeFirst();  // replace command with alternate
                    commandQueue.addFirst(result.alternate);
                    command = commandQueue.peek();
                    result = command.process(this);
                }

                madeProgress = true;

                if (result.done) {
                    commandQueue.removeFirst();

                    if (result.succeeded && command.consumesEnergy()) {
                        command.getActor().energy.spend();
                        gameState.map.advanceActor();
                    }

                    // refresh every time player takes a turn
                    if (command.getActor() == gameState.player) {
                        return new GameResult(madeProgress, result.events);
                    }
                }
                if (!result.events.isEmpty()) {
                    return new GameResult(madeProgress, result.events);
                }
            }

            // at this point, we've processed all pending actions, so advance
            // the time.
            while (commandQueue.isEmpty()) {
                Actor actor = gameState.map.getCurrentActor();

                // if waiting for input, just return
                if (actor.energy.canTakeTurn() && actor.needsInput()) {
                    return new GameResult(madeProgress, new ArrayList<>());
                }

                if (actor.energy.canTakeTurn() || actor.energy.gain(actor.speed)) {
                    // If the actor can move now, but needs input from the user, just
                    // return so we can wait for it.
                    if (actor.needsInput()) {
                        return new GameResult(madeProgress, new ArrayList<>());
                    }

                    commandQueue.add(actor.act(this));
                } else {
                    // This actor doesn't have enough energy yet, so move on to the next.
                    gameState.map.advanceActor();
                }

                // Each time we wrap around, process "idle" things that are ongoing and
                // speed independent.
//                if (actor == gameState.player) {
//                    trySpawnMonster();
//                }
            }

        }
    }

    // TODO: Currently just logging won't update the UI.  Fix.
    public void logMessage(String message) {
//        logChanged = true;
        gameState.log.add(message);
    }

    public void newGame(String name) {
        gameState = new GameState(name);
    }

    public void load(GameState gameState) {
        this.gameState = gameState;
    }
}
