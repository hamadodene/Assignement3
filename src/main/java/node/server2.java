package node;

import connection.Client;
import connection.Server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class server2 {
    public static void main(String[] args) throws InterruptedException, IOException {
        InetAddress address = Inet4Address.getLocalHost();
        Server server2 = new Server(address,0,8082,"hama3");
        server2.startNode();
        server2.messagesQueueHandling();

        Client client = new Client("192.168.1.55",8082);
        client.sendMessage("ROLE_1");
        client.messagesQueueHandling();
        client.connectToNewNode("192.168.1.55", 8080);

        client.sendMessage("Saluto Serverrsss");

        server2.join();
        client.join();
    }
}
