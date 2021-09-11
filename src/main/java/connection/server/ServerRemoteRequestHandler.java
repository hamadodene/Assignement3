package connection.server;

import connection.message.ConnectionRequest;
import connection.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerRemoteRequestHandler {
    private Socket socket;
    private ObjectOutputStream out;
    private String name;
    private Thread readFromNode;
    private Shared shared;
    private ObjectInputStream in;

    public ServerRemoteRequestHandler(Socket socket, String name, Shared shared) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        this.name = name;
        this.shared = shared;
        start();
    }

    private void start() {
         readFromNode = new Thread(() -> {
            while (true) {
                try {
                    processNodeRequest(in.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });

        readFromNode.setName(name);
        readFromNode.setDaemon(true);
        readFromNode.start();
    }

    private void processNodeRequest(Object request) {
        if (request instanceof Message) {
            try {
                System.out.println("Server: received message:  " + ((Message) request).getMessage());
                shared.add((Message) request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeUnshared(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAddress() {
        return socket.getInetAddress().getHostAddress();
    }
    public int getPort() {
        return socket.getLocalPort();
    }

    public void join() throws InterruptedException {
        readFromNode.join();
    }
}
