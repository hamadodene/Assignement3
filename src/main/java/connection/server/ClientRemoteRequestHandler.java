package connection.server;

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

    public ClientRemoteRequestHandler(Socket socket, String name, Shared shared) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.name = name;
        this.shared = shared;
        start();
    }

    public void start() {
         readFromClient = new Thread(() -> {
            while (true) {
                try {
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    processNodeRequest(in.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
        readFromClient.setName(name);
        readFromClient.setDaemon(true);
        readFromClient.start();
    }

    private void processNodeRequest(Object request) {
        if (request instanceof Message) {
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
