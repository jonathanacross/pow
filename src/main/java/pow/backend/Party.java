package pow.backend;

import pow.backend.actors.Actor;
import pow.backend.actors.Knowledge;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.event.GameEvent;
import pow.backend.event.GameEventOld;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Party implements Serializable {
    public Player player;
    public Player pet;
    public Player selectedActor;  // which actor is the player controlling?
    public final Artifacts artifacts;   // set of artifacts the party is carrying.
    public final Knowledge knowledge;

    public Party(Player player) {
        addPlayer(player);
        this.selectedActor = this.player;
        this.pet = null;
        this.artifacts = new Artifacts();
        this.knowledge = new Knowledge();
    }

    private void addPlayer(Player player) {
        this.player = player;
        player.party = this;
    }

    public void addPet(Player pet) {
        this.pet = pet;
        pet.party = this;
    }

    public void setSelectedActor(Actor actor) {
        if (actor == this.player) {
            selectedActor = this.player;
        } else if (actor == this.pet) {
            selectedActor = this.pet;
        } else {
            selectedActor = null;
        }
    }

    public List<Player> playersInParty() {
        List<Player> players = new ArrayList<>();
        players.add(player);
        if (pet != null) {
            players.add(pet);
        }
        return players;
    }

    public Player otherPlayerInParty(Actor a) {
        if (a != player) {
            return player;
        }
        if (pet != null && a != pet) {
            return pet;
        }
        return null;
    }

    public boolean containsActor(Actor actor) {
        return actor != null && (actor == this.player || actor == this.pet);
    }

    public boolean isPetSelected() {
        return this.selectedActor != null && this.selectedActor == this.pet;
    }

    public List<GameEvent> addArtifact(DungeonItem item) {
        artifacts.add(item);
        List<GameEvent> events = new ArrayList<>();

        // check for getting a pet
        if (item.artifactSlot.equals(DungeonItem.ArtifactSlot.PETSTATUE)) {
            events.add(GameEventOld.GotPet());
        }

        // check for a win!
        if (!player.winner && artifacts.hasAllPearls()) {
            player.winner = true;
            events.add(GameEventOld.WonGame());
        }

        player.updateStats();
        if (pet != null) {
            pet.updateStats();
        }

        return events;
    }

}
