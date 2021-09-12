import connection.client.Client;
import connection.server.Server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientMain  {
    public static void main(String[] args) throws IOException, InterruptedException {
        InetAddress address = Inet4Address.getLocalHost();
        Client client = new Client(Inet4Address.getLocalHost().getHostAddress(),8080, "Hamado");
        client.start();

        Server server = new Server(address, 0, 8081);
        server.start();


        for (int i = 0; i < 2 ; i++) {
            client.sendRequestToServer("Hi server, this is my message number " + i);
            Thread.sleep(4000);
        }
        client.sendConnectionRequest(address.getHostAddress(), 8081,"boh", true);
        for (int i = 0; i < 10 ; i++) {
            client.sendRequestToServer("Hi server, this is my message number " + i);
            Thread.sleep(4000);
        }
        client.join();
        server.join();
    }
}
