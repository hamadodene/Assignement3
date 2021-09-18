package connection.client;

import connection.client.game.PuzzleBoard;
import connection.message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private String name;
    private int port;
    private String address;
    //Need this to write to server
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ServerConnectionHandler serverConnectionHandler;
    private MessagesQueue queue;
    private final int n;
    private final int m;
    private final String imagePath;
    private List<Integer> randomPositions;
    private PuzzleBoard puzzle;


    public Client(String address, int port, String name, MessagesQueue queue, int n, int m, String imagePath) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.queue = queue;
        this.m = m;
        this.n = n;
        this.imagePath = imagePath;
        this.randomPositions = new ArrayList<>();
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

            //Start gui
            puzzle = new  PuzzleBoard(n, m, imagePath, this, queue, randomPositions);
            puzzle.setVisible(true);

            //Start messages queue processing thread
            puzzle.startMessagesQueueHandling();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendConnectionRequest(String address, int port, String name, boolean isServer) {
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

    public void sendTileToServer(TileMessage message) {
        serverConnectionHandler.sendTile(message);
    }

    public void join() throws InterruptedException {
        serverConnectionHandler.join();
        puzzle.join();
    }
}
