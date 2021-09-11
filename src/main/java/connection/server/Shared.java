package connection.server;

import connection.message.Message;
import connection.message.NodeInfo;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class Shared {
    private LinkedBlockingQueue<Message> messages;
    private ArrayList<ServerRemoteRequestHandler> serverList;
    private ArrayList<NodeInfo> activeServer;

    public Shared() {
        this.messages = new LinkedBlockingQueue<>();
        serverList = new ArrayList<>();
        activeServer = new ArrayList<>();
    }

    public synchronized void broadCast(Message message) {
        Iterator<ServerRemoteRequestHandler> it = serverList.iterator();
        while (it.hasNext()) {
            ServerRemoteRequestHandler srh = it.next();
            // writeUnshared() is like writeObject(), but always writes
            System.out.println("Server: Broadcast message to " + srh.getAddress() + ":" + srh.getPort());
            // a new copy of the object
            srh.sendMessage(message);
        }
    }

    public synchronized void add(Message message) throws InterruptedException {
        messages.put(message);
    }

    public synchronized void addServer(ServerRemoteRequestHandler srh) {
        serverList.add(srh);
    }

    public synchronized void addServerInfo(NodeInfo nodeInfo) {
        activeServer.add(nodeInfo);
    }

    public synchronized Message takeMessage() throws InterruptedException {
        return messages.take();
    }
}
