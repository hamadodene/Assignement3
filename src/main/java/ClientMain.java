import connection.client.Client;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class ClientMain  {
    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        Client client = new Client(Inet4Address.getLocalHost().getHostAddress(),8080, "Hamado");
        client.start();
        for (int i = 0; i < 10 ; i++) {
            client.sendRequestToServer("Hi server, this is my message number " + i);
            Thread.sleep(4000);
        }
        client.join();
    }
}
