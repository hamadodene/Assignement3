package connection.server;

import connection.message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class Server extends Thread {
    private int port;
    private InetAddress address;
    private ServerSocket serverSocket;
    private Socket socket;
    private int backlog;
    private ArrayList<ServerRemoteRequestHandler> srh;
    private ArrayList<NodeInfo> activeServer;
    private ClientRemoteRequestHandler crh;
    private LinkedBlockingQueue<Object> messages;

    public Server(InetAddress address, int backlog, int port) {
        this.address = address;
        this.backlog = backlog;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port, backlog, address);
            ObjectInputStream in;
            ObjectOutputStream out;

            while (true) {
                socket = serverSocket.accept();
                System.out.println("Accepted connection from " + socket.getInetAddress().getHostAddress() + " " + socket.getLocalPort());
                in = new ObjectInputStream(socket.getInputStream());
                processRequest(in.readObject(), socket);
            }
        } catch (IOException | InterruptedException ex) {
            System.out.println("I/O error " + ex);
        } catch (ClassNotFoundException ex) {
            System.out.println("Unknown type of request object " + ex);
        }
    }

    private void processRequest(Object request, Socket socket) throws InterruptedException, IOException {
        if (request instanceof ConnectionRequest) {
            boolean isServer = ((ConnectionRequest) request).getNodeInfo().isServer();
            String name = ((ConnectionRequest) request).getNodeInfo().getName();
            if (isServer) {
                ServerRemoteRequestHandler rh = new ServerRemoteRequestHandler(socket, name);
                srh.add(rh);

            } else {
                crh = new ClientRemoteRequestHandler(socket, name);
            }
        } else {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ErrorMessage error = new ErrorMessage("Need Connection request before start");
            out.writeUnshared(error);
            socket.close();
            out.close();
        }
    }

    public synchronized void broadCast(Message message) {
        System.out.println("Server: send " + message);
        Iterator<ServerRemoteRequestHandler> it = srh.iterator();
        while (it.hasNext()) {
            ServerRemoteRequestHandler srh = it.next();
            // writeUnshared() is like writeObject(), but always writes
            // a new copy of the object
            srh.sendMessage(message);
        }
    }
}
