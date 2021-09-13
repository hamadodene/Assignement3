package connection.client;

import connection.message.ConnectionRequest;
import connection.message.ErrorMessage;
import connection.message.Message;
import connection.message.NodeInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private String name;
    private int port;
    private String address;
    //Need this to write to server
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ServerConnectionHandler serverConnectionHandler;
    private MessagesQueue queue;
    private Thread messageHandling;

    public Client(String address, int port, String name) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.queue = new MessagesQueue();
        //Start messages queue processing thread
        messagesQueueHandling();
    }

    public void start() {
        try {
            Socket toServer = new Socket(address, port);
            System.out.println("Client: connected! ");
            String address = toServer.getInetAddress().getHostAddress();
            int port = toServer.getLocalPort();
            //Send a connection request for start game
            in = new ObjectInputStream(toServer.getInputStream());
            out = new ObjectOutputStream(toServer.getOutputStream());
            sendConnectionRequest(address, port, name, false);

            serverConnectionHandler = new ServerConnectionHandler(toServer, queue);
            serverConnectionHandler.start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void sendConnectionRequest(String address, int port, String name, boolean isServer) {
        NodeInfo nodeInfo;
        if (isServer) {
            nodeInfo = new NodeInfo(address, port, name, true);
        } else {
            nodeInfo = new NodeInfo(address, port, name, false);
        }
        ConnectionRequest conn = new ConnectionRequest(nodeInfo);
        try {
            out.reset();
            out.writeObject(conn);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequestToServer(String message) {
        serverConnectionHandler.sendMessage(message);
    }

    public void join() throws InterruptedException {
        serverConnectionHandler.join();
        messageHandling.join();
    }

    public void messagesQueueHandling() {
        messageHandling = new Thread(() -> {
            while (true) {
                try {
                    Message message = queue.take();
                    //Update GUI
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        messageHandling.setDaemon(true);
        messageHandling.start();
    }
}
