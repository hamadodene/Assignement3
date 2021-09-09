package node;

import connection.Client;
import connection.Server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class client {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress address = Inet4Address.getLocalHost();
        try {
            Client client = new Client("192.168.1.55",8080);
            //System.out.println("aajkajkajk");
            client.sendMessage("1");
            client.messagesQueueHandling();
            client.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
