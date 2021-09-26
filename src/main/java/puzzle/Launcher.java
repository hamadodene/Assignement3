package puzzle;

import puzzle.client.Node;

import java.io.IOException;
import java.net.InetAddress;

public class Launcher {
    public static void main(String[] args) throws IOException, InterruptedException {
        final String nodeName = "NODE1";
        final InetAddress serverAddress = InetAddress.getLocalHost();
        final int serverPort = 8080;
        final int backlog = 50;
        final int n = 3;
        final int m = 5;
        final String imagePath = "src/main/java/puzzle/client/game/bletchley-park-mansion.jpg";
        Node node = new Node(nodeName, serverAddress, serverPort, backlog, n, m, imagePath);
    }
}
