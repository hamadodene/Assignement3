package connection.server;

import connection.client.MessagesQueue;
import connection.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientRemoteRequestHandler {
    private Socket socket;
    private ObjectOutputStream out;
    private String name;
    private Thread readFromClient;
    private Shared shared;
    private ObjectInputStream in;

    public ClientRemoteRequestHandler(Socket socket, String name, Shared shared) throws IOException, InterruptedException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        this.name = name;
        this.shared = shared;
        start();
    }

    public void start() {
        readFromClient = new Thread(() -> {
            while (true) {
                try {
                    Object message = in.readObject();
                    processNodeRequest(message);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        readFromClient.setDaemon(true);
        readFromClient.start();
    }

    private void processNodeRequest(Object request) {
        if (request instanceof Message) {
            System.out.println("Server: receive message from client:  " + ((Message) request).getMessage());
            shared.broadCast((Message) request);
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeUnshared(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void join() throws InterruptedException {
        readFromClient.join();
    }
}
