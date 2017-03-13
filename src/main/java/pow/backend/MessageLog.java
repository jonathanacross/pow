package pow.backend;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class MessageLog implements Serializable {

    public static class Message implements Serializable {
        public final String message;
        public int count;

        public Message(String message) {
            this.message = message;
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

    public void add(String message) {
        if (messages.isEmpty()) {
            messages.add(new Message(message));
            return;
        }

        Message lastMessage = messages.get(messages.size() - 1);
        if (lastMessage.message.equals(message)) {
            lastMessage.increment();
        } else {
            messages.add(new Message(message));
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
