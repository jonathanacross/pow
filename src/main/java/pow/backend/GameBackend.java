package pow.backend;

import pow.backend.action.Action;
import pow.backend.action.ActionResult;
import pow.backend.action.Log;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.behavior.Behavior;
import pow.backend.event.GameEvent;
import pow.backend.event.GameResult;

import java.util.*;

public class GameBackend {

    private GameState gameState;
    private final Deque<Action> commandQueue;
    private final Deque<GameEvent> eventQueue;

    public GameState getGameState() {
        return gameState;
    }

    public GameBackend() {
        this.gameState = new GameState();
        this.commandQueue = new ArrayDeque<>();
        this.eventQueue = new ArrayDeque<>();
    }

    public void tellSelectedActor(Action request) {
        gameState.party.selectedActor.addCommand(request);
    }
    public void tellSelectedActor(Behavior behavior) { gameState.party.selectedActor.behavior = behavior; }

    public void setPet(Player pet) {
        this.gameState.party.addPet(pet);
        this.gameState.party.pet.setAutoplay(this.gameState, true);
        this.gameState.getCurrentMap().placePet(this.gameState.party.player, this.gameState.party.pet);
    }

    public void setGameInProgress(boolean gameInProgress) {
        gameState.gameInProgress = gameInProgress;
    }

    private List<GameEvent> processCommands(Deque<Action> commandQueue) {
        List<GameEvent> events = new ArrayList<>();

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
            if (command.getActor() == gameState.party.player) {
                events.addAll(updateBackgroundThings());
            }
        }
        events.addAll(result.events);

        return events;
    }

    public GameResult update() {
        GameResult gameResult = new GameResult(new ArrayList<>());

        for (;;) {

            if (!gameState.gameInProgress) {
                return gameResult;
            }

            // finish the existing action by processing any remaining events
            while (!eventQueue.isEmpty()) {
                GameEvent event = eventQueue.removeFirst();
                gameResult.events.add(event);
                // effect requiring a pause.  Return to frontend to pause for the
                // appropriate amount of time.
                this.gameState.getCurrentMap().effects.clear();
                if (event.eventType == GameEvent.EventType.EFFECT) {
                    // TODO: put this in an event "process" method.
                    this.gameState.getCurrentMap().effects.add(event.effect);
                    return gameResult;
                }
            }

            // process any ongoing/pending actions
            while (!commandQueue.isEmpty()) {
                gameResult.addEvents(processCommands(commandQueue));
            }
//            if (!gameResult.events.isEmpty()) {
//                // if there are events, then go back to top to process them.
//                continue;
//            }

            // at this point, we've processed all pending actions, so advance
            // the time until some actor has an action.
            while (commandQueue.isEmpty()) {

                if (!gameState.gameInProgress) {
                    return gameResult;
                }

                Actor actor = gameState.getCurrentMap().getCurrentActor();

                if (!actor.energy.canTakeTurn()) {
                    actor.energy.gain(actor.getSpeed());
                    gameState.getCurrentMap().advanceActor();
                } else {
                    // if waiting for input, just return
                    if (actor.needsInput(gameState)) {
                        gameState.party.setSelectedActor(actor);
                        // TODO: need to change this to not RETURN, but just add events
                        // to the event list and go back to event processing.
                        return gameResult;
                    }
                    else {
                        commandQueue.add(actor.act(this));
                        gameResult.addEvents(actor.conditions.update(this));

                        if (actor == gameState.party.selectedActor) {
                            // force return every time player takes turn
                            // TODO: need to change this to not RETURN, but just add events
                            // to the event list and go back to event processing.
                            return gameResult;
                        }
                    }
                }
            }
        }
    }

    private List<GameEvent> updateBackgroundThings() {
        List<GameEvent> events = new ArrayList<>();
        GameMap map = gameState.getCurrentMap();
        Party party = gameState.party;
        Player player = gameState.party.player;
        if (map.flags.poisonGas && !party.artifacts.hasGasMask()) {
            logMessage("the noxious air burns " + player.getNoun() + "'s lungs", MessageLog.MessageType.COMBAT_BAD);
            // TODO: impact pet as well
            events.addAll(player.takeDamage(this, GameConstants.POISON_DAMAGE_PER_TURN, null));
        }
        if (map.flags.hot && !party.artifacts.hasHeatSuit()) {
            logMessage(player.getNoun() + " withers in the extreme heat", MessageLog.MessageType.COMBAT_BAD);
            events.addAll(player.takeDamage(this, GameConstants.HEAT_DAMAGE_PER_TURN, null));
        }
        // handle traps
        for (Actor a : map.actors) {
            if (map.hasTrapAt(a.loc.x, a.loc.y)) {
                MessageLog.MessageType messageType = (a == player || a == party.pet) ?
                        MessageLog.MessageType.COMBAT_BAD :
                        MessageLog.MessageType.COMBAT_GOOD;
                logMessage(a.getNoun() + " is caught in a trap.", messageType);
                events.addAll(a.takeDamage(this, GameConstants.TRAP_DAMAGE_PER_TURN, null));
            }
        }
        gameState.turnCount++;
        return events;
    }

    // NOTE: to make sure that the UI updates, we can't modify gameState.log
    // directly; instead MUST use this method for all logging.
    public void logMessage(String message, MessageLog.MessageType type) {
        commandQueue.add(new Log());
        gameState.log.add(message, type);
    }

    public void newGame(Player player) { gameState = new GameState(player); }

    public void load(GameState gameState) {
        this.gameState = gameState;
    }
}
