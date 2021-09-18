import connection.client.Client;
import connection.client.MessagesQueue;
import connection.client.game.PuzzleBoard;
import connection.server.Server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Main2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        InetAddress address = Inet4Address.getLocalHost();
        Server server = new Server(address, 0, 8081);
        server.start();

        MessagesQueue queue = new MessagesQueue();
        List<Integer> randomPositions = new ArrayList<>();

        Client client = new Client(Inet4Address.getLocalHost().getHostAddress(), 8081, "Hamado2", queue);
        client.start();

        client.sendConnectionRequest(address.getHostAddress(), 8080,"boh", true);

        final int n = 3;
        final int m = 5;

        final String imagePath = "src/main/java/game/bletchley-park-mansion.jpg";

        final PuzzleBoard puzzle = new PuzzleBoard(n, m, imagePath, client, queue, randomPositions);
        puzzle.setVisible(true);

        server.join();
        client.join();
        puzzle.join();

    }
}
