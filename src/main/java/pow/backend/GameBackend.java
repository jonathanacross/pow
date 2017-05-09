package pow.backend;

import pow.backend.action.Action;
import pow.backend.action.ActionResult;
import pow.backend.action.Log;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.behavior.Behavior;
import pow.backend.event.GameEvent;
import pow.backend.event.GameResult;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class GameBackend {

    private GameState gameState;
    private final Deque<Action> commandQueue = new LinkedList<>();

    public GameState getGameState() {
        return gameState;
    }

    public GameBackend() { this.gameState = new GameState(); }

    public void tellPlayer(Action request) {
        gameState.player.addCommand(request);
    }
    public void tellPlayer(Behavior behavior) { gameState.player.behavior = behavior; }

    public void setGameInProgress(boolean gameInProgress) {
        gameState.gameInProgress = gameInProgress;
    }

    public GameResult update() {
        GameResult gameResult = new GameResult(new ArrayList<>());

        for (;;) {

            if (!gameState.gameInProgress) {
                return gameResult;
            }

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
                        gameState.getCurrentMap().advanceActor();
                    }

                    // update background things every time player takes a turn
                    if (command.getActor() == gameState.player) {
                        gameResult.addEvents(updateBackgroundThings());
                    }

//                    // refresh every time player takes a turn
//                    if (command.getActor() == gameState.player) {
//                        gameResult.addEvents(result.events);
//                        //return gameResult;
//                        //return new GameResult(madeProgress, result.events);
//                    }
                }
                if (!result.events.isEmpty()) {
                    gameResult.addEvents(result.events);
                    //return new GameResult(madeProgress, result.events);
                }
            }

            // at this point, we've processed all pending actions, so advance
            // the time.
            while (commandQueue.isEmpty()) {

                if (!gameState.gameInProgress) {
                    return gameResult;
                }

                Actor actor = gameState.getCurrentMap().getCurrentActor();

                // if waiting for input, just return
                if (actor.energy.canTakeTurn() && actor.needsInput(gameState)) {
                    return gameResult;
                    //return new GameResult(madeProgress, new ArrayList<>());
                }

                if (actor.energy.canTakeTurn() || actor.energy.gain(actor.getSpeed())) {
                    // If the actor can move now, but needs input from the user, just
                    // return so we can wait for it.
                    if (actor.needsInput(gameState)) {
                        return gameResult;
                        //return new GameResult(madeProgress, new ArrayList<>());
                    }

                    commandQueue.add(actor.act(this));
                    gameResult.addEvents(actor.conditions.update(this));

                    if (actor == gameState.player) {
                        // force return every time player takes turn
                        return gameResult;
                    }
                } else {
                    // This actor doesn't have enough energy yet, so move on to the next.
                    gameState.getCurrentMap().advanceActor();
                }

                // Each time we wrap around, process "idle" things that are ongoing and
                // speed independent.
                //if (actor == gameState.player) {
                //    gameResult.addEvents(updateBackgroundThings());
                //}
            }
        }
    }

    private List<GameEvent> updateBackgroundThings() {
        List<GameEvent> events = new ArrayList<>();
        GameMap map = gameState.getCurrentMap();
        Player player = gameState.player;
        if (map.flags.poisonGas && !player.hasGasMask()) {
            events.addAll(player.takeDamage(this, GameConstants.POISON_DAMAGE_PER_TURN));
        }
        if (map.flags.hot && !player.hasHeatSuit()) {
            events.addAll(player.takeDamage(this, GameConstants.HEAT_DAMAGE_PER_TURN));
        }
        gameState.turnCount++;
        return events;
    }

    // NOTE: to make sure that the UI updates, we can't modify gameState.log
    // directly; instead MUST use this method for all logging.
    public void logMessage(String message) {
        commandQueue.add(new Log());
        gameState.log.add(message);
    }

    public void newGame(Player player) { gameState = new GameState(player); }

    public void load(GameState gameState) {
        this.gameState = gameState;
    }
}
