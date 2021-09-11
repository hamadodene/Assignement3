package connection.server;

import connection.message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private int port;
    private InetAddress address;
    private ServerSocket serverSocket;
    private Socket socket;
    private int backlog;
    private Shared shared;
    ClientRemoteRequestHandler crh;

    public Server(InetAddress address, int backlog, int port) {
        this.address = address;
        this.backlog = backlog;
        this.port = port;
        shared = new Shared();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port, backlog, address);
            System.out.println("Start server: " + serverSocket.getInetAddress().getHostAddress() + " " + serverSocket.getLocalPort());
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
                ServerRemoteRequestHandler rh = new ServerRemoteRequestHandler(socket, name, shared);
                shared.addServer(rh);
                shared.addServerInfo(((ConnectionRequest) request).getNodeInfo());

            } else {
                crh = new ClientRemoteRequestHandler(socket, name, shared);
            }
        } else {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ErrorMessage error = new ErrorMessage("Need Connection request before start", -1);
            out.writeUnshared(error);
            socket.close();
            out.close();
        }
    }
}
