package connection.client;

import connection.message.ErrorMessage;
import connection.message.Message;
import connection.message.TileMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnectionHandler {

    private Socket socket;
    private Thread readFromServer;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private MessagesQueue queue;

    public ServerConnectionHandler(Socket socket, MessagesQueue queue) throws IOException {
        this.socket = socket;
        this.queue = queue;
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    public void start() {
        readFromServer = new Thread(() -> {
            while (true) {
                try {
                    Object message = in.readObject();
                    processNodeRequest(message);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        readFromServer.setDaemon(true);
        readFromServer.start();
    }

    private void processNodeRequest(Object request) throws IOException, ClassNotFoundException, InterruptedException {
        if (request instanceof ErrorMessage) {
            System.out.println("Client: encored error during connection " + ((ErrorMessage) in.readObject()).getError());
        } else if (request instanceof TileMessage) {
            TileMessage message = (TileMessage) request;
            System.out.println("Client node: received tile message " + message);
            queue.add(message);
        }
    }

    public void sendMessage(String message) {
        Message msg = new Message(message, false);
        try {
            System.out.println("Send message " + msg.getMessage());
            out.reset();
            out.writeObject(msg);
            out.flush();
            //System.out.println("Socket status " + socket.getInetAddress().isReachable(1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTile(TileMessage tileMessage) {
        try {
            out.reset();
            System.out.println("Client Send tile message: " + tileMessage.toString());
            out.writeObject(tileMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void join() throws InterruptedException {
        readFromServer.join();
    }

}
