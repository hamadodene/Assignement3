package puzzle.server;

import puzzle.message.Message;
import puzzle.message.NodeInfo;
import puzzle.message.RicartAgrawalaMessage;
import puzzle.message.TileMessage;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerManager {
    private LinkedBlockingQueue<TileMessage> messages;
    private LinkedBlockingQueue<TileMessage> clientMessages;
    private ArrayList<ServerRemoteRequestHandler> serverList;
    private ClientRemoteRequestHandler client;
    private ArrayList<NodeInfo> activeServer;
    private int positionFirstPuzzle = 0;
    private int positionSecondPuzzle = 0;
    private TileMessage lastClientMessage;

    public ServerManager() {
        this.messages = new LinkedBlockingQueue<>();
        this.clientMessages = new LinkedBlockingQueue<>();
        serverList = new ArrayList<>();
        activeServer = new ArrayList<>();
    }

    public synchronized void criticalSection(TileMessage message) {
        if (ClientRemoteRequestHandler.isRequestingCS()) {
            System.out.println("----------------------------------------------- Time entered " + TimeStamp.getTime());
            Iterator<ServerRemoteRequestHandler> it = serverList.iterator();
            System.out.println("List of server " + serverList.get(0).getAddress() + " " + serverList.get(0).getPort());
            while (it.hasNext()) {
                ServerRemoteRequestHandler srh = it.next();
                // writeUnshared() is like writeObject(), but always writes
                System.out.println("Server: Broadcast tile message to " + srh.getAddress() + ":" + srh.getPort());
                // a new copy of the object
                srh.sendTile(message);
            }
            exitCriticalSection();
        } else {
            System.out.println("-----------EXIT FROM CRITICAL SECTION---NO ACTION EXECUTED--");
        }
    }

    private void exitCriticalSection() {
        ClientRemoteRequestHandler.setRequestingCS(false);
        TimeStamp.setInstanceToNull();
        System.out.println("----------------------------------------------- Time exited  " + TimeStamp.getTime());
    }

    public void sendRequest(String request, Timestamp timeStamp, Message type) {
        //Set critical section request to TRUE
        ClientRemoteRequestHandler.setRequestingCS(true);
        Iterator<ServerRemoteRequestHandler> it = serverList.iterator();
        while (it.hasNext()) {
            ServerRemoteRequestHandler srh = it.next();
            System.out.println("Prepare Agrawala request for " + srh.getAddress() + " " + srh.getPort());
            RicartAgrawalaMessage message = new RicartAgrawalaMessage(request, type, positionFirstPuzzle, positionSecondPuzzle, timeStamp);
            srh.sendAgrawalaMessage(message);
        }
    }

    public synchronized void determineCriticalSectionEntry(ServerRemoteRequestHandler srr, Timestamp myTimeStamp, Timestamp guestTimeStamp, boolean positionAlreadyLocked) {
        RicartAgrawalaMessage permitMessage = new RicartAgrawalaMessage("", Message.PERMIT, -1, -1, null);
        RicartAgrawalaMessage notPermitMessage = new RicartAgrawalaMessage("", Message.NOTPERMIT, -1, -1, null);
        if (ClientRemoteRequestHandler.isRequestingCS()) {
            if (!positionAlreadyLocked) {
                System.out.println("Position not locked, send PERMIT message");
                srr.sendAgrawalaMessage(permitMessage);
            }
            if (positionAlreadyLocked) {  //if remote and local node have clicked the same puzzle
                //Check timestamp
                if (myTimeStamp.compareTo(guestTimeStamp) > 0) { //Guest timestamp has higher priority
                    System.out.println("Position locked, Guest timestamp is higher, send PERMIT message");
                    //Not execute critical section
                    exitCriticalSection();
                    //Send PERMIT message to Guest
                    srr.sendAgrawalaMessage(permitMessage);
                } else if (myTimeStamp.compareTo(guestTimeStamp) < 0) { //my timestamp has higher priority
                    System.out.println("Position locked, my timestamp is higher, send NOTPERMIT message");
                    //Send NOTPERMIT message to guest and continue executing critical section
                    srr.sendAgrawalaMessage(notPermitMessage);
                } else if (myTimeStamp.compareTo(guestTimeStamp) == 0) {
                    //Complex situation
                    //To doubt, discard Guest and my REQUEST
                    System.out.println("Position locked, timestamp is equal, send NOTPERMIT message---------Discard GUEST and my REQUEST");
                    //Not execute critical section
                    exitCriticalSection();
                    srr.sendAgrawalaMessage(notPermitMessage);
                }
            }
        } else {
            System.out.println("I'm not in critical section, send PERMIT message");
            srr.sendAgrawalaMessage(permitMessage);
        }
    }

    public void checkConnections() {
        for (ServerRemoteRequestHandler server : serverList) {
            server.update();
        }
        Iterator<ServerRemoteRequestHandler> iterator = serverList.iterator();
        while (iterator.hasNext()) {
            ServerRemoteRequestHandler server = iterator.next();
            if (!server.connected) {
                server.close();
                iterator.remove();
                System.out.println("Node at " + server.getAddress() + ":" + server.getPort() + " has disconnected");
            }
        }
    }

    public ClientRemoteRequestHandler getClient() {
        return client;
    }

    public void setClient(ClientRemoteRequestHandler client) {
        this.client = client;
    }

    public synchronized void saveServerTileMessage(TileMessage message) throws InterruptedException {
        System.out.println("Server: added message on queue");
        messages.put(message);
    }

    public synchronized void saveClientTileMessage(TileMessage message) throws InterruptedException {
        System.out.println("Server: added message on queue");
        clientMessages.put(message);
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
        this.activeServer = activeServer;
    }

    public TileMessage takeServerMessage() throws InterruptedException {
        return messages.take();
    }

    public TileMessage takeClientMessage() throws InterruptedException {
        return clientMessages.take();
    }

    public int getPositionFirstPuzzle() {
        return positionFirstPuzzle;
    }

    public int getPositionSecondPuzzle() {
        return positionSecondPuzzle;
    }

    public synchronized TileMessage getLastClientMessage() {
        return lastClientMessage;
    }

    public synchronized void setLastClientMessage(TileMessage lastClientMessage) {
        this.lastClientMessage = lastClientMessage;
    }

    public void join() throws InterruptedException {
        for (ServerRemoteRequestHandler srr : serverList) {
            srr.join();
        }
    }
}
