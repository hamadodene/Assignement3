package connection.client;

import connection.message.ErrorMessage;
import connection.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnectionHandler {

    private Socket socket;
    private Thread readFromServer;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private MessagesQueue queue;

    public ServerConnectionHandler(Socket socket, MessagesQueue queue) {
        this.socket = socket;
        this.queue = queue;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        readFromServer = new Thread(() -> {
            while (true) {
                try {
                    processNodeRequest(in.readObject());
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        readFromServer.setDaemon(true);
        readFromServer.start();
    }

    private void processNodeRequest(Object request) throws IOException, ClassNotFoundException, InterruptedException {
        if (request instanceof ErrorMessage) {
            System.out.println("Client: encored error during connection " + ((ErrorMessage) in.readObject()).getError());
        } else if (request instanceof Message) {
            Message message = (Message) request;
            queue.add(message);
            System.out.println("Client: received message " + message);
        }
    }

    public void sendMessage(String message) {
        Message msg = new Message(message, false);
        try {
            System.out.println("Send message " + msg.getMessage());
            out.writeObject(msg);
            out.flush();
            System.out.println("Socket status " + socket.getInetAddress().isReachable(1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void join() throws InterruptedException {
        readFromServer.join();
    }

}
