package puzzle.server;

import puzzle.message.*;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ServerRemoteRequestHandler {
    private Socket socket;
    private ObjectOutputStream out;
    private String name;
    private Thread readFromNode;
    private ServerManager serverManager;
    private ObjectInputStream in;
    private ArrayList<Message> recvdMsgTokens;
    private List<Integer> tiles;
    private TimeStamp myTimeStamp;

    public ServerRemoteRequestHandler(Socket socket, String name, ServerManager serverManager, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.name = name;
        this.serverManager = serverManager;
        tiles = new ArrayList<>();
        recvdMsgTokens = new ArrayList<Message>();
        start();
    }

    private void start() {
        readFromNode = new Thread(() -> {
            while (true) {
                try {
                    Object message = in.readObject();
                    processNodeRequest(message);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        readFromNode.setDaemon(true);
        readFromNode.start();
    }

    private void processNodeRequest(Object request) throws IOException, InterruptedException {
        if (request instanceof TileMessage) {
            try {
                TileMessage message = (TileMessage) request;
                System.out.println("Server: received tile message from another node: " + message.toString());
                serverManager.saveTileMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (request instanceof NodeInfoList) {
            System.out.println("Server: Received node list " + serverManager.getActiveServer().toString());
            ArrayList<NodeInfo> nodeList = ((NodeInfoList) request).getActiveNode();
            serverManager.setActiveServer(nodeList);
            //Open connection with all node
            for(NodeInfo node : nodeList) {
                initializeConnectionWithNode(node, request);
            }
        } else if(request instanceof RicartAgrawalaMessage) {
            Message type = ((RicartAgrawalaMessage) request).getType();
            int remotePositionFirstPuzzle = ((RicartAgrawalaMessage) request).getPositionFirstPuzzle();
            int remotePositionSecondPuzzle = ((RicartAgrawalaMessage) request).getPositionSecondPuzzle();
            int positionFirstPuzzle = serverManager.getPositionFirstPuzzle();
            int positionSecondPuzzle = serverManager.getPositionSecondPuzzle();
            Timestamp guestTimeStamp = ((RicartAgrawalaMessage) request).getTimeStamp();
            switch (type) {
                case REQUEST:
                    System.out.println("Receive token REQUEST");
                    boolean positionAlreadyLocked = ClientRemoteRequestHandler.positionAlreadyLock(remotePositionFirstPuzzle,remotePositionSecondPuzzle,positionFirstPuzzle,positionSecondPuzzle);
                    serverManager.determineCriticalSectionEntry(this,ClientRemoteRequestHandler.getMyTimeStamp(),guestTimeStamp, positionAlreadyLocked);
                    break;
                case PERMIT:
                    System.out.println("Receive token PERMIT");
                    //add token to token list
                    recvdMsgTokens.add(Message.PERMIT);
                    if(recvdMsgTokens.size() == serverManager.activeServerSize()) {
                        if(recvdMsgTokens.contains(Message.PERMIT) && verifyToken(recvdMsgTokens)) {
                            serverManager.criticalSection(serverManager.takeMessage());
                        };
                    } else {
                        System.out.println("Another node is already executing the critical section");
                        //Send update to GUI?
                    }
                    break;
                case NOTPERMIT:
                    System.out.println("Receive token NOTPERMIT, Discard my operation");
                    break;
                default:
            }
        }
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

    public boolean verifyToken(ArrayList<Message> recvdMsgTokens) {
        return new HashSet<>(recvdMsgTokens).size() <= 1;
    }

    public void sendAgrawalaMessage(RicartAgrawalaMessage message) {
        try {
            System.out.println("Send Agrawala" + message.getType());
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
