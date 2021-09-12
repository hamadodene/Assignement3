package connection.server;

import connection.client.MessagesQueue;
import connection.message.ConnectionRequest;
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
            while (socket.isConnected()) {
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
        } else if (request instanceof ConnectionRequest) {
            String address = ((ConnectionRequest) request).getNodeInfo().getAddress();
            int port = ((ConnectionRequest) request).getNodeInfo().getPort();
            String name = ((ConnectionRequest) request).getNodeInfo().getName();

            System.out.println("Server: received connection request request for " + address + " " + port);
            try {
                System.out.println("Initialize connection for " + address + ":" + port);
                Socket socket = new Socket(address, port);
                ServerRemoteRequestHandler srr = new ServerRemoteRequestHandler(socket, name, shared);
                shared.addServer(srr);
                shared.addServerInfo(((ConnectionRequest) request).getNodeInfo());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            out.reset();
            out.writeUnshared(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void join() throws InterruptedException {
        readFromClient.join();
    }
}
