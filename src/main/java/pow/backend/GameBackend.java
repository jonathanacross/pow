package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.action.ActionResult;
import pow.backend.action.Action;
import pow.backend.event.GameResult;

import java.util.*;

public class GameBackend {

    private GameState gameState;
    public Deque<Action> commandQueue = new LinkedList<>();
    //private boolean logChanged;

    public GameState getGameState() {
        return gameState;
    }

    public GameBackend() {
        this.gameState = new GameState("Unknown Adventurer");
    }

    public void tellPlayer(Action request) {
        gameState.player.addCommand(request);
    }

    public void setGameInProgress(boolean gameInProgress) {
        gameState.gameInProgress = gameInProgress;
    }

    public GameResult update() {
        GameResult gameResult = new GameResult(new ArrayList<>());
        if (!gameState.gameInProgress) {
            return gameResult;
        }

        for (;;) {

            // process any ongoing/pending actions
            while (!commandQueue.isEmpty()) {

                // find alternate commands, if needed
                Action command = commandQueue.peek();
                ActionResult result = command.process(this);
                while (result.alternate != null) {
                    commandQueue.removeFirst();  // replace action with alternate
                    commandQueue.addFirst(result.alternate);
                    command = commandQueue.peek();
                    result = command.process(this);
                }

                if (result.done) {
                    commandQueue.removeFirst();

                    if (result.succeeded && command.consumesEnergy()) {
                        command.getActor().energy.spend();
                        gameState.map.advanceActor();
                    }

                    // refresh every time player takes a turn
                    if (command.getActor() == gameState.player) {
                        gameResult.addEvents(result.events);
                        //return gameResult;
                        //return new GameResult(madeProgress, result.events);
                    }
                }
                if (!result.events.isEmpty()) {
                    gameResult.addEvents(result.events);
                    //return new GameResult(madeProgress, result.events);
                }
            }


            // at this point, we've processed all pending actions, so advance
            // the time.
            while (commandQueue.isEmpty()) {
                Actor actor = gameState.map.getCurrentActor();

                // if waiting for input, just return
                if (actor.energy.canTakeTurn() && actor.needsInput()) {
                    return gameResult;
                    //return new GameResult(madeProgress, new ArrayList<>());
                }

                if (actor.energy.canTakeTurn() || actor.energy.gain(actor.speed)) {
                    // If the actor can move now, but needs input from the user, just
                    // return so we can wait for it.
                    if (actor.needsInput()) {
                        return gameResult;
                        //return new GameResult(madeProgress, new ArrayList<>());
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
