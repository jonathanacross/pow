package pow.backend;

import pow.backend.action.*;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.behavior.Behavior;
import pow.backend.event.GameEvent;
import pow.backend.event.GameResult;

import java.util.*;

public class GameBackend {

    private boolean gameInProgress;
    private GameState gameState;
    private final Deque<Action> commandQueue;
    private final Deque<GameEvent> eventQueue;
    private boolean dirty;

    public GameState getGameState() {
        return gameState;
    }

    public GameBackend() {
        this.gameState = null;
        this.gameInProgress = false;
        this.commandQueue = new ArrayDeque<>();
        this.eventQueue = new ArrayDeque<>();
        this.dirty = false;
    }

    public void tellSelectedActor(Action request) {
        gameState.party.selectedActor.addCommand(request);
    }
    public void tellSelectedActor(Behavior behavior) { gameState.party.selectedActor.behavior = behavior; }

    public void setPet(Player pet) {
        this.gameState.party.addPet(pet);
        this.gameState.party.pet.setAutoplay(this.gameState, true);
        if (! this.gameState.getCurrentMap().placePet(this.gameState.party.player, this.gameState.party.pet) ) {
            this.logMessage("There is no space for " + this.gameState.party.pet.getNoun() + " to join you here",
                    MessageLog.MessageType.GENERAL);
        }
    }

    public void setGameInProgress(boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    private List<GameEvent> processCommand(Deque<Action> commandQueue) {

        Action command = commandQueue.removeFirst();
        ActionResult result = command.process(this);
        List<GameEvent> events = new ArrayList<>(result.events);

        List<Action> derivedActions = new ArrayList<>(result.derivedActions);

        if (result.succeeded && command.consumesEnergy()) {
            command.getActor().energy.spend();
            gameState.getCurrentMap().advanceActor();

            // update background things every time player takes a turn
            if (command.getActor() == gameState.party.player) {
                events.addAll(updateBackgroundThings());
            }
        }

        // Add all the elements to the head of the commandQueue.
        for (int i = derivedActions.size() - 1; i >=0; i--) {
            commandQueue.addFirst(derivedActions.get(i));
        }

        return events;
    }

    // TODO: would like to refactor this to return both new commands and new events
    // or better yet, somehow split into one that just gets commands.
    private List<GameEvent> processActor(Actor actor) {
        List<GameEvent> events = new ArrayList<>();

        if (!actor.energy.canTakeTurn()) {
            actor.energy.gain(actor.getSpeed());
            gameState.getCurrentMap().advanceActor();
        } else {
            // if waiting for input, just return
            if (actor.needsInput(gameState)) {
                gameState.party.setSelectedActor(actor);
                events.add(GameEvent.WAITING_USER_INPUT);
                if (dirty) {
                    events.add(GameEvent.UPDATE_NEED_REDRAW);
                }
            }
            else {
                commandQueue.add(actor.act(this));
                commandQueue.add(new UpdateConditions(actor));
            }
        }
        return events;
    }

    public GameResult update() {
        GameResult gameResult = new GameResult(new ArrayList<>());

        for (;;) {

            // finish any remaining events
            dirty = false;
            while (!eventQueue.isEmpty()) {
                GameEvent event = eventQueue.removeFirst();
                gameResult.events.add(event);

                if (event != GameEvent.WAITING_USER_INPUT) {
                    dirty = true;
                }

                // effect requiring a pause.  Return to frontend to pause for the
                // appropriate amount of time.
                if (event.showUpdate()) {
                    return gameResult;
                }
            }

            if (!gameInProgress) {
                return gameResult;
            }


            // process any ongoing/pending actions
            if (!commandQueue.isEmpty()) {
                eventQueue.addAll(processCommand(commandQueue));
            }

            // at this point, we've processed all pending actions, so advance
            // the time until some actor has an action.
            while (commandQueue.isEmpty() && eventQueue.isEmpty()) {
                Actor actor = gameState.getCurrentMap().getCurrentActor();
                eventQueue.addAll(processActor(actor));
            }
        }
    }

    private List<GameEvent> updateBackgroundThings() {
        List<GameEvent> events = new ArrayList<>();
        GameMap map = gameState.getCurrentMap();
        Party party = gameState.party;
        if (map.flags.poisonGas && !party.artifacts.hasGasMask()) {
            for (Actor a : party.playersInParty()) {
                int dam = GameConstants.POISON_DAMAGE_PER_TURN;
                logMessage("the noxious air burns " + a.getNoun() + "'s lungs for " + dam + " damage.",
                        MessageLog.MessageType.COMBAT_BAD);
                events.addAll(a.takeDamage(this, dam, null));
            }
        }
        if (map.flags.hot && !party.artifacts.hasHeatSuit()) {
            for (Actor a : party.playersInParty()) {
                int dam = GameConstants.HEAT_DAMAGE_PER_TURN;
                logMessage(a.getNoun() + " withers in the extreme heat, taking " + dam + " damage.",
                        MessageLog.MessageType.COMBAT_BAD);
                events.addAll(a.takeDamage(this, dam, null));
            }
        }
        // handle traps
        for (Actor a : map.actors) {
            if (map.hasTrapAt(a.loc.x, a.loc.y)) {
                int dam = GameConstants.TRAP_DAMAGE_PER_TURN;
                MessageLog.MessageType messageType = (party.containsActor(a)) ?
                        MessageLog.MessageType.COMBAT_BAD :
                        MessageLog.MessageType.COMBAT_GOOD;
                logMessage(a.getNoun() + " is caught in a trap, taking " + dam + " damage.", messageType);
                events.addAll(a.takeDamage(this, dam, null));
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
