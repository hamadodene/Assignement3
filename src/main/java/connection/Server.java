package connection;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Role:
 * 1 - Local Client
 * 2 - Other node
 */
public class Server {

    private ArrayList<ConnectionToOtherServer> serverList;
    private ConnectionToClient client;
    private LinkedBlockingQueue<Object> messages;
    private InetAddress address;
    private int backlog;
    private int port;
    private String name;
    private int CLIENT = 1;
    private int SERVER = 2;
    private Thread messageHandling;
    private Thread accept;
    private Thread readFromClient;
    private Thread read;
    private int clientCount = 0;


    public Server(InetAddress address, int backlog, int port, String name) throws IOException, InterruptedException {
        this.serverList = new ArrayList<>();
        this.messages = new LinkedBlockingQueue<>();
        this.address = address;
        this.backlog = backlog;
        this.port = port;
        this.name = name;
    }

    public void startNode() throws IOException, InterruptedException {
        ServerSocket node = new ServerSocket(port, backlog, address);
        System.out.println("Start server on ip " + node.getInetAddress() + " port " + node.getLocalPort());
         accept = new Thread(() -> {
            while (true) {
                try {
                    //start connection for new node
                    Socket socket = node.accept();
                    //Get role
                    InputStream input = socket.getInputStream();
                    DataInputStream data = new DataInputStream(input);
                    int role = Integer.parseInt(data.readUTF());

                    if (role == SERVER) {
                        System.out.println("Accept connection from " + socket.getInetAddress().getHostAddress()  + "port " + socket.getPort());
                        serverList.add(new ConnectionToOtherServer(socket));
                        //Send Game status here
                        /**
                         *  To do
                         */
                    } else {
                        if(clientCount > 0 ) {
                            System.out.println("Max client connection reached");
                            socket.close();
                        }
                        System.out.println("Accept connection from client " + socket.getInetAddress().getHostAddress() + " port" + socket.getPort());
                        client = new ConnectionToClient(socket);
                        clientCount++;
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        accept.setDaemon(true);
        accept.start();
    }

    public void messagesQueueHandling() throws InterruptedException {
         messageHandling = new Thread(() -> {
            while (true) {
                try {
                    Object message = messages.take();
                    //Send message to local client
                    client.sendMessage(message.toString());
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        messageHandling.setDaemon(true);
        messageHandling.start();
    }


    public void broadcast(String message) throws IOException {
        //Send broadCast
        for (ConnectionToOtherServer node : serverList) {
            node.sendMessage(message);
        }
    }

    private class ConnectionToClient {
        private OutputStream outputStream;
        private DataOutputStream dataOutputStream;
        private InputStream inputStream;
        private DataInputStream dataInputStream;
        private Socket socket;

        public ConnectionToClient(Socket socket) throws IOException, InterruptedException {
            this.socket = socket;
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            inputStream = socket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);

             readFromClient = new Thread(() -> {
                //Get message from local client
                try {
                    String message = dataInputStream.readUTF();
                    //Connect to other server
                    if(message.contains("REGISTER")) {
                        System.out.println("Connecting new node");
                        String nodeInfo = message.replace("REGISTER_", "");
                        String address = nodeInfo.split("_")[0];
                        String port = nodeInfo.split("_")[1];
                        Socket ss = new Socket(address, Integer.parseInt(port));
                        serverList.add(new ConnectionToOtherServer(socket));
                        System.out.println("List of server " + serverList.toString());
                    } else {
                        messages.put(message);
                        //Send also broadCast to other server
                        broadcast(message);
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });

            readFromClient.setDaemon(true);
            readFromClient.start();
        }

        public void sendMessage(String msg) throws IOException {
            System.out.println("Send message "  + msg);
            dataOutputStream.writeUTF(msg);
            dataOutputStream.flush(); // send the message
        }

        public void close() throws IOException {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
        }
    }

    private class ConnectionToOtherServer {
        private OutputStream outputStream;
        private DataOutputStream dataOutputStream;
        private InputStream inputStream;
        private DataInputStream dataInputStream;
        private Socket socket;

        public ConnectionToOtherServer(Socket socket) throws IOException, InterruptedException {
            this.socket = socket;
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            inputStream = socket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);

             read = new Thread(() -> {
                try {
                    //Read message From other server
                    String message = dataInputStream.readUTF();
                    messages.put(message);

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            read.setDaemon(true);
            read.start();
        }

        public void sendMessage(String msg) throws IOException {
            System.out.println("Send message "  + msg);
            dataOutputStream.writeUTF(msg);
            dataOutputStream.flush(); // send the message
        }

        public void close() throws IOException {
            dataOutputStream.close();
            dataInputStream.close();
            socket.close();
        }
    }

    public void join() throws InterruptedException {
        messageHandling.join();
        read.join();
        accept.join();
        readFromClient.join();
    }
}
