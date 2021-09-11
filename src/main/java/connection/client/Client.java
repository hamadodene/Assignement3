package connection.client;

import connection.message.ConnectionRequest;
import connection.message.Message;
import connection.message.NodeInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends Thread {
    private String name;
    private int port;
    private String address;
    //Need this to write to server
    ObjectOutputStream out;

    public Client(String address, int port, String name) {
        this.address = address;
        this.port = port;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            Socket toServer = new Socket(address, port);
            ObjectInputStream in = new ObjectInputStream(toServer.getInputStream());
            System.out.println("Client: connected!");
            String address = toServer.getInetAddress().getHostAddress();
            int port = toServer.getLocalPort();
            //Send a connection request for start game
            NodeInfo nodeInfo = new NodeInfo(address,port,name,false);
            ConnectionRequest conn = new ConnectionRequest(nodeInfo);
            out = new ObjectOutputStream(toServer.getOutputStream());
            out.writeUnshared(conn);
            out.flush();

            while (true) {
                Message message = (Message) in.readObject();
                System.out.println("Client: received message " + message );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendConnectionRequest(String address, int port, String name) {
        NodeInfo nodeInfo = new NodeInfo(address,port,name,true);
        ConnectionRequest conn = new ConnectionRequest(nodeInfo);
        try {
            out.writeUnshared(conn);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
