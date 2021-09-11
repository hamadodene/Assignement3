package connection;

import connection.server.Server;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress address = Inet4Address.getLocalHost();
        Server server = new Server(address, 0, 8080);
        server.start();
    }
}
