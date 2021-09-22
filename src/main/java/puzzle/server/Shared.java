package puzzle.server;

import puzzle.message.Message;
import puzzle.message.NodeInfo;
import puzzle.message.RicartAgrawalaMessage;
import puzzle.message.TileMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class Shared {
    private LinkedBlockingQueue<TileMessage> messages;
    private ArrayList<ServerRemoteRequestHandler> serverList;
    private ArrayList<NodeInfo> activeServer;

    public Shared() {
        this.messages = new LinkedBlockingQueue<>();
        serverList = new ArrayList<>();
        activeServer = new ArrayList<>();
    }

    public void broadCast(TileMessage message) {
        Iterator<ServerRemoteRequestHandler> it = serverList.iterator();
        while (it.hasNext()) {
            ServerRemoteRequestHandler srh = it.next();
            // writeUnshared() is like writeObject(), but always writes
            System.out.println("Server: Broadcast tile message to " + srh.getAddress() + ":" + srh.getPort());
            // a new copy of the object
            srh.sendTile(message);
        }
    }

    public void sendRequest(String request, Message type) {
        Iterator<ServerRemoteRequestHandler> it = serverList.iterator();
        while (it.hasNext()) {
            ServerRemoteRequestHandler srh = it.next();
            RicartAgrawalaMessage message = new RicartAgrawalaMessage(request, type);
            // a new copy of the object
            srh.sendAgrawalaMessage(message);
        }
    }

    public void add(TileMessage message) throws InterruptedException {
        System.out.println("Server: added message on queue");
        messages.put(message);
    }

    public void addServer(ServerRemoteRequestHandler srh) {
        serverList.add(srh);
    }

    public void addServerInfo(NodeInfo nodeInfo) {
        activeServer.add(nodeInfo);
    }

    public int activeServerSize() {
        return activeServer.size();
    }

    public ArrayList<NodeInfo> getActiveServer() {
        return activeServer;
    }

    public ArrayList<ServerRemoteRequestHandler> getServerList() {
        return serverList;
    }
    public void setServerList(ArrayList<ServerRemoteRequestHandler> srr) {
        serverList = srr;
    }

    public void setActiveServer(ArrayList<NodeInfo> activeServer) {
        activeServer = activeServer;
    }

    public TileMessage takeMessage() throws InterruptedException {
        return messages.take();
    }

    public int queueSize() {
        return messages.size();
    }


    public void join() throws InterruptedException {
        for(ServerRemoteRequestHandler srr : serverList) {
            srr.join();
        }
    }
}
