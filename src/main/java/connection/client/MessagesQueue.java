package connection.client;

import connection.message.Message;

import java.util.concurrent.LinkedBlockingQueue;

public class MessagesQueue {
    private LinkedBlockingQueue<Message> messages;

    public MessagesQueue() {
        messages = new LinkedBlockingQueue<>();
    }

    public synchronized void add(Message message) throws InterruptedException {
        messages.put(message);
    }

    public synchronized Message take() throws InterruptedException {
        return messages.take();
    }
}
