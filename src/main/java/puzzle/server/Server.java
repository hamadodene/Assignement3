package puzzle.server;

import puzzle.message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private int port;
    private InetAddress address;
    private ServerSocket serverSocket;
    private Socket socket;
    private int backlog;
    private Shared shared;
    private ClientRemoteRequestHandler crh;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Thread accept;
    private Thread messageHandling;
    private boolean serverStart = false;

    public Server(InetAddress address, int backlog, int port) {
        this.address = address;
        this.backlog = backlog;
        this.port = port;
        shared = new Shared();
        messagesQueueHandling();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port, backlog, address);
        if(serverSocket.isBound()) {
            System.out.println("Start server: " + serverSocket.getInetAddress().getHostAddress() + " " + serverSocket.getLocalPort());
            serverStart = true;
        }
        accept = new Thread(() -> {
            while (true) {
                try {
                    socket = serverSocket.accept();
                    System.out.println("Accept connection from "  + socket.getPort() + " " + socket.getInetAddress());
                    out = new ObjectOutputStream(socket.getOutputStream());
                    processRequest(socket);
                } catch (IOException | InterruptedException ex) {
                    System.out.println("I/O error " + ex);
                    ex.printStackTrace();
                }
            }
        });
        accept.setDaemon(true);
        accept.start();
    }

    private void processRequest(Socket socket) throws IOException, InterruptedException {
        System.out.println("Server: Processing connection request");
        in = new ObjectInputStream(socket.getInputStream());
        Object request = null;
        try {
            request = in.readObject();
            System.out.println("Request is " + request);
        } catch (ClassNotFoundException ex) {
            System.out.println("Unknown type of request object " + ex);
        }
        if (request instanceof ConnectionRequest) {
            boolean isServer = ((ConnectionRequest) request).getNodeInfo().isServer();
            String name = ((ConnectionRequest) request).getNodeInfo().getName();
            if (isServer) {
                ServerRemoteRequestHandler rh = new ServerRemoteRequestHandler(socket, name, shared, out, in);
                if(shared.activeServerSize() > 1) {
                    ArrayList<NodeInfo> nodeInfo = shared.getActiveServer();
                    NodeInfo nn = new NodeInfo(address.getHostAddress(),port,name,true);
                    nodeInfo.add(nn);
                    NodeInfoList listNodes = new NodeInfoList(nodeInfo);
                    out.writeObject(listNodes);
                    out.flush();
                }
                shared.addServer(rh);
                shared.addServerInfo(((ConnectionRequest) request).getNodeInfo());
                System.out.println("Accepted connection from " + socket.getInetAddress().getHostAddress() + " " + socket.getPort());
            } else {
                System.out.println("Server: Initialize connection with client");
                crh = new ClientRemoteRequestHandler(socket, name, shared);
            }
        } else {
            ErrorMessage error = new ErrorMessage("Need Connection request before start", -1);
            out.writeUnshared(error);
            socket.close();
            out.close();
        }
    }

    //Get message received from other server and send to connected client
    public void messagesQueueHandling() {
        messageHandling = new Thread(() -> {
            while (true) {
                try {
                    TileMessage message = shared.takeMessage();
                    System.out.println("Send tile message to client: " + message.toString());
                    crh.sendTile(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    public void join() throws InterruptedException {
        accept.join();
        messageHandling.join();
        crh.join();
        shared.join();
    }

    public void checkConnections() {
        ArrayList<ServerRemoteRequestHandler> serverList = shared.getServerList();
        for (ServerRemoteRequestHandler srr : serverList) {
            //To do
        }
        shared.setServerList(serverList);
    } // checkConnection

    public boolean isServerStart() {
        return serverStart;
    }
}
