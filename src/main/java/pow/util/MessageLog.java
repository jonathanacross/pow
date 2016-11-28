package pow.util;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class MessageLog implements Serializable {

    private List<String> messages;
    private int maxSize;

    public MessageLog(int maxSize) {
        this.maxSize = maxSize;
        this.messages = new LinkedList<>();
    }

    public void add(String message) {
        messages.add(message);
        while (messages.size() > maxSize) {
            messages.remove(0);
        }
    }

    public List<String> getLastN(int n) {
        int startIdx = Math.max(0, messages.size() - n);
        int endIdx = messages.size();
        return messages.subList(startIdx, endIdx);
    }
}
