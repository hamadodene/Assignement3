package connection;

import connection.client.Client;
import connection.server.Server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        InetAddress address = Inet4Address.getLocalHost();
        Server server = new Server(address, 0, 8080);
        server.start();
        server.join();

        //Client client = new Client("127.0.0.1",8080, "Hamado");
        //client.start();
        //client.sendMessage("Hello server");
    }
}
