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

    private void processNodeRequest(Object request) {
        if (request instanceof TileMessage) {
            try {
                TileMessage message = (TileMessage) request;
                System.out.println("Server: received tile message from another node: " + message.toString());
                shared.add(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (request instanceof NodeInfoList) {
            ArrayList<NodeInfo> nodeList = ((NodeInfoList) request).getActiveNode();
            shared.setActiveServer(nodeList);
            System.out.println("Server: Received node list " + shared.getActiveServer().toString());
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

    public void close() {
        try {
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
