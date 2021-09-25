package puzzle.server;

import puzzle.message.*;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;

public class ServerRemoteRequestHandler {
    private Socket socket;
    private ObjectOutputStream out;
    private String name;
    private Thread readFromNode;
    private ServerManager serverManager;
    private ObjectInputStream in;
    private ArrayList<Message> recvdMsgTokens;
    int timeout = 60000;
    long lastCheckIn;
    boolean connected;
    boolean readFail = false;


    public ServerRemoteRequestHandler(Socket socket, String name, ServerManager serverManager, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.name = name;
        this.serverManager = serverManager;
        recvdMsgTokens = new ArrayList<>();
        connected = true;
        lastCheckIn = System.currentTimeMillis();
        start();
        ping();
    }

    private void start() {
        readFromNode = new Thread(() -> {
            while (connected) {
                try {
                    Object message = in.readObject();
                    lastCheckIn = System.currentTimeMillis();
                    processNodeRequest(message);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    if (!readFail) {
                        System.out.println("Server: [!] Failed to read from node " + this.getAddress() + " " + this.getPort());
                        readFail = true;
                    }
                    e.printStackTrace();
                    break;
                }
            }
        });
        readFromNode.setDaemon(true);
        readFromNode.start();
    }

    private synchronized void processNodeRequest(Object request) throws IOException, InterruptedException {
        if (request instanceof TileMessage) {
            TileMessage message = (TileMessage) request;
            System.out.println("Server: received tile message from another node: " + message);
            serverManager.saveServerTileMessage(message);
        } else if (request instanceof NodeInfoList) {
            System.out.println("Server: Received node list " + serverManager.getActiveServer().toString());
            ArrayList<NodeInfo> nodeList = ((NodeInfoList) request).getActiveNode();
            serverManager.setActiveServer(nodeList);
            //Open connection with all node
            for (NodeInfo node : nodeList) {
                initializeConnectionWithNode(node, request);
            }
        } else if (request instanceof RicartAgrawalaMessage) {
            Message type = ((RicartAgrawalaMessage) request).getType();
            int remotePositionFirstPuzzle = ((RicartAgrawalaMessage) request).getPositionFirstPuzzle();
            int remotePositionSecondPuzzle = ((RicartAgrawalaMessage) request).getPositionSecondPuzzle();
            int positionFirstPuzzle = serverManager.getPositionFirstPuzzle();
            int positionSecondPuzzle = serverManager.getPositionSecondPuzzle();
            Timestamp guestTimeStamp = ((RicartAgrawalaMessage) request).getTimeStamp();
            switch (type) {
                case REQUEST:
                    System.out.println("Receive token REQUEST");
                    boolean positionAlreadyLocked = ClientRemoteRequestHandler.positionAlreadyLock(remotePositionFirstPuzzle, remotePositionSecondPuzzle, positionFirstPuzzle, positionSecondPuzzle);
                    serverManager.determineCriticalSectionEntry(this, ClientRemoteRequestHandler.getMyTimeStamp(), guestTimeStamp, positionAlreadyLocked);
                    break;
                case PERMIT:
                    System.out.println("Receive token PERMIT");
                    //add token to token list
                    recvdMsgTokens.add(Message.PERMIT);
                    System.out.println("Token size " + recvdMsgTokens.size() + "active server " + serverManager.activeServerSize());
                    if (recvdMsgTokens.size() == serverManager.activeServerSize()) {
                        System.out.println("Check if is time to enter in critical section: Contain PERMIT " + recvdMsgTokens.contains(Message.PERMIT) + " only PERMIT? " + verifyToken(recvdMsgTokens));
                        if (recvdMsgTokens.contains(Message.PERMIT) && verifyToken(recvdMsgTokens)) {
                            recvdMsgTokens.clear();
                            //System.out.println("Entering in critical section " + serverManager.takeClientMessage());
                            serverManager.criticalSection(serverManager.takeClientMessage());
                        } else {
                            System.out.println("Another node is already executing the critical section " + recvdMsgTokens.toString());
                            recvdMsgTokens.clear();
                            //Send update to GUI?
                        }
                    }
                    break;
                case NOTPERMIT:
                    System.out.println("Receive token NOTPERMIT, Discard my operation");
                    recvdMsgTokens.clear();
                    break;
                default:
                    System.out.println("Request corrupted: Unknown message type");
                    break;
            }
        } else if(request instanceof Ping) {
            lastCheckIn = System.currentTimeMillis();
            System.out.println("Receive ping message, the socket is healthy");
        }
    }

    public boolean update() {
        long timeSinceCheckIn = System.currentTimeMillis() - lastCheckIn;
        if (timeSinceCheckIn > timeout) {
            System.out.println("Node at " + this.getAddress() + ":" + this.getPort() + " timed out (" + timeSinceCheckIn + "ms).");
            connected = false;
        } else {
            connected = true;
        }
        return connected;
    }

    public String getAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    public int getPort() {
        return socket.getPort();
    }

    public void join() throws InterruptedException {
        readFromNode.join();
    }

    public void sendConnectionRequest(NodeInfo info, boolean alreadyHaveNodeList) {
        try {
            info.setServer(true);
            ConnectionRequest request = new ConnectionRequest(info, alreadyHaveNodeList);
            out.writeObject(request);
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

    private void ping() {
        Timer connectionMonitor = new Timer();
        connectionMonitor.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Ping ping = new Ping();
                try {
                    out.writeObject(ping);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Date(), timeout/2);
    }

    public boolean verifyToken(ArrayList<Message> recvdMsgTokens) {
        return new HashSet<>(recvdMsgTokens).size() <= 1;
    }

    public void sendAgrawalaMessage(RicartAgrawalaMessage message) {
        try {
            System.out.println("Send Agrawala " + message.getType());
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeConnectionWithNode(NodeInfo node, Object request) throws IOException {
        Socket socket = new Socket(node.getAddress(), node.getPort());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ServerRemoteRequestHandler srr = new ServerRemoteRequestHandler(socket, name, serverManager, out, in);
        srr.sendConnectionRequest(((ConnectionRequest) request).getNodeInfo(), true);
        serverManager.addServer(srr);
        System.out.println("Initialize connection for " + node.getAddress() + ":" + node.getPort());
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
