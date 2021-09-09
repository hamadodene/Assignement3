import connection.Client;
import connection.Server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        InetAddress address = Inet4Address.getLocalHost();
        try {
            Server server = new Server(address,0,8080,"hama");
            server.startNode();
            server.messagesQueueHandling();
            server.join();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
