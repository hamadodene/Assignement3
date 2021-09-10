package node;

import connection.Client;
import connection.Server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

public class server1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        InetAddress address = Inet4Address.getLocalHost();
        Server server = new Server(address,0,8081,"hama2");
        server.startNode();
        server.messagesQueueHandling();
        Client client = new Client("192.168.1.55",8081);
        //System.out.println("aajkajkajk");
        client.sendMessage("ROLE_1");
        client.messagesQueueHandling();
        client.connectToNewNode("192.168.1.55", 8080);
        server.join();
        client.join();
    }
}
