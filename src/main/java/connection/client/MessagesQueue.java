package connection.client;

import connection.message.TileMessage;

import java.util.concurrent.LinkedBlockingQueue;

public class MessagesQueue {
    private LinkedBlockingQueue<TileMessage> messages;

    public MessagesQueue() {
        messages = new LinkedBlockingQueue<>();
    }

    public void add(TileMessage message) throws InterruptedException {
        messages.put(message);
    }

    public synchronized TileMessage take() throws InterruptedException {
        return messages.take();
    }
}
