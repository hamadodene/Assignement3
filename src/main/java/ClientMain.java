import connection.client.Client;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class ClientMain  {
    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        Client client = new Client(Inet4Address.getLocalHost().getHostAddress(),8080, "Hamado");
        client.start();
        client.sendRequestToServer("Hello server");
        client.sendRequestToServer("Hello server 2");


        Client client2 = new Client(Inet4Address.getLocalHost().getHostAddress(),8080, "Hamado2");
        client2.start();
        client2.sendRequestToServer("Hello server");
        client2.sendRequestToServer("Hello server 2");

        client.join();
        client2.join();
    }
}
