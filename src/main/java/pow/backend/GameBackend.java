package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.command.ActionResult;
import pow.backend.command.CommandRequest;
import pow.backend.event.GameEvent;

import java.util.*;

public class GameBackend {

    private GameState gameState;
    public Deque<CommandRequest> commandQueue = new LinkedList<>();
    private boolean logChanged;
    boolean madeProgress;

    public GameState getGameState() {
        return gameState;
    }

    public GameBackend() {
        this.gameState = new GameState("Unknown Adventurer");
    }

    public void tellPlayer(CommandRequest request) {
        gameState.player.addCommand(request);
    }

    public List<GameEvent> processCommand() {

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
                        return result.events;
                    }

                    if (!result.events.isEmpty()) {
                        return result.events;
                    }
                }
            }

            // at this point, we've processed all pending actions, so advance
            // the time.
            while (commandQueue.isEmpty()) {
                Actor actor = gameState.map.getCurrentActor();

                // if waiting for input, just return
                if (actor.energy.canTakeTurn() && actor.needsInput) {
                    return new ArrayList<>();
                }

                if (actor.energy.canTakeTurn() || actor.energy.gain(actor.speed)) {
                    // If the actor can move now, but needs input from the user, just
                    // return so we can wait for it.
                    if (actor.needsInput) {
                        return new ArrayList<>();
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

//    public List<GameEvent> processCommand() {
//        List<GameEvent> events = new ArrayList<>();
//
//        this.logChanged = false;
//        if (! commandQueue.isEmpty()) {
//            gameState.player.addCommand(commandQueue.poll());
//
//            for (Actor a: gameState.map.actors) {
//                events.addAll(a.act(this));
//            }
//
//            // TODO: this is a bad hack to make sure we don't modify the
//            // array of actors while we're iterating through it -- e.g.,
//            // because a monster/pet gets killed.  Right now, it's possible
//            // that we kill a monster, and then it does its attack, and then
//            // the monster is removed here.  :(
//            for (Iterator<Actor> iterator = gameState.map.actors.iterator(); iterator.hasNext(); ) {
//                Actor a = iterator.next();
//                if (a.health < 0) {
//                    events.add(GameEvent.KILLED);
//                    iterator.remove();
//                    if (a == gameState.pet) {
//                        gameState.pet = null;
//                    }
//                }
//            }
//        }
//        if (logChanged) {
//            events.add(GameEvent.LOG_UPDATE);
//        }
//
//        return events;
//    }

    // note: this must be the only way to log a message, otherwise it may
    // not immediately show up to the user.
    // TODO: is there a way to clean up the 'logChanged' variable?
    public void logMessage(String message) {
        logChanged = true;
        gameState.log.add(message);
    }

    public void newGame(String name) {
        gameState = new GameState(name);
    }

    public void load(GameState gameState) {
        this.gameState = gameState;
    }
}
