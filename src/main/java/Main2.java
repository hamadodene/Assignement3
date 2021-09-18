import connection.client.Client;
import connection.client.MessagesQueue;
import connection.server.Server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

public class Main2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        InetAddress address = Inet4Address.getLocalHost();
        final int n = 3;
        final int m = 5;
        final String imagePath = "src/main/java/game/bletchley-park-mansion.jpg";
        MessagesQueue queue = new MessagesQueue();


        Server server = new Server(address, 0, 8081);
        server.start();

        Client client = new Client(Inet4Address.getLocalHost().getHostAddress(), 8081, "Hamado2", queue,3,5, imagePath);
        client.start();

        client.sendConnectionRequest(address.getHostAddress(), 8080,"boh", true);

        server.join();
        client.join();

    }
}
