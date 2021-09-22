package puzzle.server;

import puzzle.message.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerRemoteRequestHandler {
    private Socket socket;
    private ObjectOutputStream out;
    private String name;
    private Thread readFromNode;
    private Shared shared;
    private ObjectInputStream in;
    private boolean sendConnectionRequest;
    private NodeInfo info;

    public ServerRemoteRequestHandler(Socket socket, String name, Shared shared, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.name = name;
        this.shared = shared;
        start();
    }

    private void start() {
        readFromNode = new Thread(() -> {
            while (true) {
                try {
                    Object message = in.readObject();
                    processNodeRequest(message);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        readFromNode.setDaemon(true);
        readFromNode.start();
    }

    private void processNodeRequest(Object request) throws IOException {
        if (request instanceof TileMessage) {
            try {
                TileMessage message = (TileMessage) request;
                System.out.println("Server: received tile message from another node: " + message.toString());
                shared.add(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (request instanceof NodeInfoList) {
            System.out.println("Server: Received node list " + shared.getActiveServer().toString());
            ArrayList<NodeInfo> nodeList = ((NodeInfoList) request).getActiveNode();
            shared.setActiveServer(nodeList);
            //Open connection with all node
            for(NodeInfo node : nodeList) {
                initializeConnectionWithNode(node, request);
            }
        } else if(request instanceof RicartAgrawalaMessage) {
            Message type = ((RicartAgrawalaMessage) request).getType();
            String message = ((RicartAgrawalaMessage) request).getMessage();
            switch (type) {
                case REQUEST:
                    System.out.println("Receive Agrawala REQUEST, check");
                    break;
                case PERMIT:
                    System.out.println("Receive permit");
                    //add response to list
                    break;
                case NOTPERMIT:
                    System.out.println("Receive not permit message");
                    //ad response to list
                    //We will check list with another thread
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

    public void sendConnectionRequest(NodeInfo info) {
        try {
            info.setServer(true);
            ConnectionRequest request = new ConnectionRequest(info);
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
        ServerRemoteRequestHandler srr = new ServerRemoteRequestHandler(socket, name, shared, out, in);
        srr.sendConnectionRequest(((ConnectionRequest) request).getNodeInfo());
        shared.addServer(srr);
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
