package connection.server;

import connection.client.MessagesQueue;
import connection.message.ConnectionRequest;
import connection.message.Message;
import connection.message.TileMessage;

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
    private int timeout = 1500;


    public ClientRemoteRequestHandler(Socket socket, String name, Shared shared) throws IOException, InterruptedException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
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
        if (request instanceof TileMessage) {
            System.out.println("Server: receive tile message from client, broadcast to all server" );
            shared.broadCast((TileMessage) request);
        } else if (request instanceof ConnectionRequest) {
            String address = ((ConnectionRequest) request).getNodeInfo().getAddress();
            int port = ((ConnectionRequest) request).getNodeInfo().getPort();
            String name = ((ConnectionRequest) request).getNodeInfo().getName();

            System.out.println("Server: received connection request for " + address + " " + port);
            try {
                Socket socket = new Socket(address, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ServerRemoteRequestHandler srr = new ServerRemoteRequestHandler(socket, name, shared, out, in);
                srr.sendConnectionRequest(((ConnectionRequest) request).getNodeInfo());
                shared.addServer(srr);
                shared.addServerInfo(((ConnectionRequest) request).getNodeInfo());
                System.out.println("Initialize connection for " + address + ":" + port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            //out.reset();
            System.out.println("Client SendMessage: "+ message.getMessage() );
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTile(TileMessage tileMessage) {
        try {
            //out.reset();
            System.out.println("Client Send tile message");
            out.writeObject(tileMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void join() throws InterruptedException {
        readFromClient.join();
    }

    public void close() {
        try {
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
