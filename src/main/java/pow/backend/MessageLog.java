package pow.backend;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class MessageLog implements Serializable {

    public enum MessageType implements Serializable {
        // Most common messages; anything that doesn't fit in a special category below.
        GENERAL,

        // E.g., "you gained a level", or "you win!"
        GAME_EVENT,

        // Combat messages favorable to the player. "You hit the monster for 5 damage."
        COMBAT_GOOD,

        // Combat messages neutral to the player. "The monster misses you."
        COMBAT_NEUTRAL,

        // Combat messages unfavorable to the player. "The monster hit you for 3 damage."
        COMBAT_BAD,

        // For alerting if user tried to do something they can't, e.g.,
        // "There's nothing to pick up here.",  "You can't go that way."
        USER_ERROR,

        // Status messages, e.g., for "You're poisoned!" or
        // "The monster is more resistant to fire."
        STATUS,

        // For debugging messages/commands.
        DEBUG
    }

    public static class Message implements Serializable {
        public final String message;
        public final MessageType type;
        public int count;

        public Message(String message, MessageType type) {
            this.message = message;
            this.type = type;
            this.count = 1;
        }

        public void increment() {
            this.count++;
        }

        @Override
        public String toString() {
            if (count == 1) {
                return message;
            }

            return message + " (x" + count + ")";
        }
    }

    private final List<Message> messages;
    private final int maxSize;

    public MessageLog(int maxSize) {
        this.maxSize = maxSize;
        this.messages = new LinkedList<>();
    }

    public void add(String message, MessageType type) {
        if (messages.isEmpty()) {
            messages.add(new Message(message, type));
            return;
        }

        Message lastMessage = messages.get(messages.size() - 1);
        if (lastMessage.message.equals(message)) {
            lastMessage.increment();
        } else {
            messages.add(new Message(message, type));
        }
        while (messages.size() > maxSize) {
            messages.remove(0);
        }
    }

    public List<Message> getLastN(int n) {
        int startIdx = Math.max(0, messages.size() - n);
        int endIdx = messages.size();
        return messages.subList(startIdx, endIdx);
    }
}
