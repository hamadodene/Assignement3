package puzzle.server;

import puzzle.message.Message;
import puzzle.message.NodeInfo;
import puzzle.message.RicartAgrawalaMessage;
import puzzle.message.TileMessage;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerManager {
    private LinkedBlockingQueue<TileMessage> messages;
    private ArrayList<ServerRemoteRequestHandler> serverList;
    private ArrayList<NodeInfo> activeServer;
    private ArrayList<String> recvdMsgTokens;
    private List<Integer> tiles;
    private int positionFirstPuzzle = 0;
    private int positionSecondPuzzle = 0;
    private boolean accessing = false;

    public ServerManager() {
        this.messages = new LinkedBlockingQueue<>();
        serverList = new ArrayList<>();
        activeServer = new ArrayList<>();
    }

    public synchronized void criticalSection(TileMessage message) {
        System.out.println("----------------------------------------------- Time entered " + TimeStamp.getTime());
        Iterator<ServerRemoteRequestHandler> it = serverList.iterator();
        while (it.hasNext()) {
            ServerRemoteRequestHandler srh = it.next();
            // writeUnshared() is like writeObject(), but always writes
            System.out.println("Server: Broadcast tile message to " + srh.getAddress() + ":" + srh.getPort());
            // a new copy of the object
            srh.sendTile(message);
        }
        exitCriticalSection();
    }

    public synchronized void determineCriticalSectionEntry(ServerRemoteRequestHandler srr, Timestamp myTimeStamp, Timestamp guestTimeStamp, boolean positionAlreadyLocked) {
        RicartAgrawalaMessage permitMessage = new RicartAgrawalaMessage("", Message.PERMIT, -1, -1, null);
        RicartAgrawalaMessage notPermitMessage = new RicartAgrawalaMessage("", Message.NOTPERMIT, -1, -1, null);
        if (ClientRemoteRequestHandler.isRequestingCS()) {
            if (!positionAlreadyLocked) {
                srr.sendAgrawalaMessage(permitMessage);
            }
            if (positionAlreadyLocked) {  //if remote and local node have clicked the same puzzle
                //Check timestamp
                if (myTimeStamp.compareTo(guestTimeStamp) > 0) { //Guest timestamp has higher priority
                    srr.sendAgrawalaMessage(permitMessage);
                } else if (myTimeStamp.compareTo(guestTimeStamp) < 0) { //my timestamp has higher priority
                    srr.sendAgrawalaMessage(notPermitMessage);
                } else {
                    //Complex situation
                    //To doubt i send permit
                    srr.sendAgrawalaMessage(permitMessage);
                }
            }
        } else {
            srr.sendAgrawalaMessage(permitMessage);
        }
    }

    private void exitCriticalSection() {
        ClientRemoteRequestHandler.setRequestingCS(false);
        TimeStamp.setInstanceToNull();
        System.out.println("----------------------------------------------- Time exited  " + TimeStamp.getTime());
    }

    public void sendRequest(String request, Timestamp timeStamp, Message type) {
        Iterator<ServerRemoteRequestHandler> it = serverList.iterator();
        while (it.hasNext()) {
            ServerRemoteRequestHandler srh = it.next();
            RicartAgrawalaMessage message = new RicartAgrawalaMessage(request, type, positionFirstPuzzle, positionSecondPuzzle, timeStamp);
            // a new copy of the object
            srh.sendAgrawalaMessage(message);
        }
    }

    public void saveTileMessage(TileMessage message) throws InterruptedException {
        System.out.println("Server: added message on queue");
        messages.put(message);
    }

    public void addServer(ServerRemoteRequestHandler srh) {
        serverList.add(srh);
    }

    public void addToken(Message token) {
        recvdMsgTokens.add(token.toString());
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

    public synchronized boolean isAccessing() {
        return accessing;
    }

    public synchronized void setAccessing(boolean accessing) {
        this.accessing = accessing;
    }

    public int getPositionFirstPuzzle() {
        return positionFirstPuzzle;
    }

    public void setPositionFirstPuzzle(int positionFirstPuzzle) {
        this.positionFirstPuzzle = positionFirstPuzzle;
    }

    public int getPositionSecondPuzzle() {
        return positionSecondPuzzle;
    }

    public void setPositionSecondPuzzle(int positionSecondPuzzle) {
        this.positionSecondPuzzle = positionSecondPuzzle;
    }

    public void join() throws InterruptedException {
        for (ServerRemoteRequestHandler srr : serverList) {
            srr.join();
        }
    }
}
