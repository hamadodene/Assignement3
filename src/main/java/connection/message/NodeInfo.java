package connection.message;

import java.io.Serializable;

public class NodeInfo implements Serializable {
    private String address;
    private int port;
    private String name;
    private boolean isServer;


    public  NodeInfo(String ip, int port, String name, boolean isServer) {
        this.address = ip;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean server) {
        isServer = server;
    }
}
