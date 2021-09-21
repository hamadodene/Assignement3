package puzzle.client;

import puzzle.message.ErrorMessage;
import puzzle.message.TileMessage;

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
        this.queue = queue;
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
