package puzzle.server;

import puzzle.message.ConnectionRequest;
import puzzle.message.Message;
import puzzle.message.RicartAgrawalaMessage;
import puzzle.message.TileMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;

public class ClientRemoteRequestHandler {
    private static Timestamp myTimeStamp;
    private Socket socket;
    private ObjectOutputStream out;
    private String name;
    private Thread readFromClient;
    private ServerManager serverManager;
    private ObjectInputStream in;
    private int timeout = 1500;
    private static boolean requestingCS;


    public ClientRemoteRequestHandler(Socket socket, String name, ServerManager serverManager) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.name = name;
        this.serverManager = serverManager;
        start();
    }

    public void start() {
        readFromClient = new Thread(() -> {
            while (true) {
                try {
                    Object message = in.readObject();
                    processNodeRequest(message);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        readFromClient.setDaemon(true);
        readFromClient.start();
    }

    public static Timestamp getMyTimeStamp() {
        return myTimeStamp;
    }

    public static void setMyTimeStamp(Timestamp myTimeStamp) {
        ClientRemoteRequestHandler.myTimeStamp = myTimeStamp;
    }

    public synchronized static boolean isRequestingCS() {
        return requestingCS;
    }

    public synchronized static void setRequestingCS(boolean requestingCS) {
        ClientRemoteRequestHandler.requestingCS = requestingCS;
    }

    private synchronized void processNodeRequest(Object request) throws InterruptedException {
        if (request instanceof TileMessage) {
            TileMessage message = (TileMessage) request;
            if (serverManager.activeServerSize() > 0) {
                serverManager.saveClientTileMessage(message);
                ClientRemoteRequestHandler.setMyTimeStamp(TimeStamp.getInstance());
                //Send REQUEST to all server
                serverManager.sendRequest(message.toString(), ClientRemoteRequestHandler.getMyTimeStamp(), Message.REQUEST);
            } else {
                //Play alone
                serverManager.saveClientTileMessage(message);
            }
        } else if (request instanceof ConnectionRequest) {
            String address = ((ConnectionRequest) request).getNodeInfo().getAddress();
            int port = ((ConnectionRequest) request).getNodeInfo().getPort();
            String name = ((ConnectionRequest) request).getNodeInfo().getName();
            System.out.println("Server: received connection request for " + address + " " + port);
            try {
                initializeConnectionWithNode(address, port, request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendAgrawalaCheckResult(RicartAgrawalaMessage message) {
        try {
            System.out.println("Client Send Agrawala check result: " + message.getType());
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTile(TileMessage tileMessage) {
        try {
            System.out.println("Client Send tile message: " + tileMessage.toString());
            out.writeObject(tileMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean positionAlreadyLock(int remotePositionFirstPuzzle, int remotePositionSecondPuzzle, int positionFirstPuzzle, int positionSecondPuzzle) {
        if (positionFirstPuzzle == remotePositionFirstPuzzle || remotePositionSecondPuzzle == positionSecondPuzzle) {
            return true;
        } else {
            return false;
        }
    }

    private void initializeConnectionWithNode(String address, int port, Object request) throws IOException {
        Socket socket = new Socket(address, port);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ServerRemoteRequestHandler srr = new ServerRemoteRequestHandler(socket, name, serverManager, out, in);
        srr.sendConnectionRequest(((ConnectionRequest) request).getNodeInfo(), false);
        serverManager.addServer(srr);
        serverManager.addServerInfo(((ConnectionRequest) request).getNodeInfo());
        System.out.println("Initialize connection for " + address + ":" + port);
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
