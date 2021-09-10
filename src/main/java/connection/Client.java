package connection;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private ConnectionToServer server;
    private LinkedBlockingQueue<Object> messages;
    private Socket socket;
    private OutputStream outputStream;
    private DataOutputStream dataOutputStream;
    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private Thread messageHandling;
    private Thread read;

    public Client(String address, int port) throws IOException, InterruptedException {
        socket = new Socket(address, port);
        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(outputStream);
        inputStream = socket.getInputStream();
        dataInputStream = new DataInputStream(inputStream);
        System.out.println("Start client on ip " + socket.getInetAddress() + " port " + socket.getLocalPort());
        messages = new LinkedBlockingQueue<Object>();
        server = new ConnectionToServer(socket);
    }


    public void messagesQueueHandling() {
         messageHandling = new Thread(() -> {
            while (true) {
                try {
                    Object message = messages.take();
                    System.out.println("Updating gui status: " + message);
                    // Update GUI status
                } catch (InterruptedException  e) {
                    e.printStackTrace();
                }
            }
        });
        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    public void connectToNewNode(String address, int port) throws IOException {
        //Get server ip and port and name
        sendMessage("REGISTER_"+address+"_"+port);
    }

    private class ConnectionToServer {
        Socket socket;
        public ConnectionToServer(Socket socket) throws IOException, InterruptedException {
            this.socket = socket;
             read = new Thread(() -> {
                while (true) {
                    try {
                        //Receive message From server and update GUI
                        String message = dataInputStream.readUTF();
                        messages.put(message);
                    } catch (IOException  | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            read.setDaemon(true);
            read.start();
        }
    }

    public void sendMessage(String msg) throws IOException {
        System.out.println("Send message "  + msg);
        dataOutputStream.writeUTF(msg);
        dataOutputStream.flush(); // send the message
    }

    public void join() throws InterruptedException {
        messageHandling.join();
        read.join();
    }

    public void close() throws IOException {
        dataOutputStream.close();
        dataInputStream.close();
        socket.close();
    }
}
