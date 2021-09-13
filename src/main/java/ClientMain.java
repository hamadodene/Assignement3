import connection.client.Client;
import connection.server.Server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientMain  {
    public static void main(String[] args) throws IOException, InterruptedException {
        InetAddress address = Inet4Address.getLocalHost();

        Server server = new Server(address, 0, 8082);
        server.start();


        Client client = new Client(Inet4Address.getLocalHost().getHostAddress(),8082, "Hamado");
        client.start();



        /*
        for (int i = 0; i < 2 ; i++) {
            client.sendRequestToServer("Hi server, this is my message number " + i);
            Thread.sleep(4000);
        }*/


        client.join();
        server.join();
    }
}
