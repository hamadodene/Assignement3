package connection;

import org.w3c.dom.Node;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {

    private ArrayList<ConnectionToOtherServer> serverList;
    private ArrayList<NodeInfo> activeServerList;
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
    private ServerSocket node;
    private int role;

    public Server(InetAddress address, int backlog, int port, String name) throws IOException, InterruptedException {
        this.serverList = new ArrayList<>();
        this.activeServerList = new ArrayList<>();
        this.messages = new LinkedBlockingQueue<>();
        this.address = address;
        this.backlog = backlog;
        this.port = port;
        this.name = name;
    }

    public void startNode() throws IOException, InterruptedException {
        node = new ServerSocket(port, backlog, address);
        System.out.println("Start server on ip " + node.getInetAddress() + " port " + node.getLocalPort());
         accept = new Thread(() -> {
            while (true) {
                try {
                    //start connection for new node
                    Socket socket = node.accept();
                    //Get role
                    InputStream input = socket.getInputStream();
                    DataInputStream data = new DataInputStream(input);
                    String message = data.readUTF();

                    if(message.contains("ROLE")) {
                        role = Integer.parseInt(message.split("_")[1]);
                    }

                    if (role == SERVER) {
                        System.out.println("Accept connection from " + socket.getInetAddress().getHostAddress()  + " port " + socket.getPort());
                        ConnectionToOtherServer cc = new ConnectionToOtherServer(socket);
                        serverList.add(cc);
                        NodeInfo nodeInfo = new NodeInfo(socket.getInetAddress().getHostAddress(), socket.getLocalPort());
                        activeServerList.add(nodeInfo);
                        /*ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        ObjectOutputStream obj = new ObjectOutputStream(bao);
                        obj.writeObject(activeServerList);
                        obj.close();*/

                    } else if(role == CLIENT) {
                        if(clientCount > 0 ) {
                            System.out.println("Max client connection reached");
                            socket.close();
                        }
                        System.out.println("Accept connection from client " + socket.getInetAddress().getHostAddress() + " port " + socket.getPort());
                        client = new ConnectionToClient(socket);
                        clientCount++;
                    } else {
                        //Message for me
                        messages.put(message);
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
                    if(client != null) {
                        //Send message to local client
                        client.sendMessage(message.toString());
                    } else {
                        System.out.println("Client not found");
                        messages.remove(message);
                    }
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
            System.out.println("Broadcast message to " + node.toString());
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
                 while (true) {
                     //Get message from local client
                     try {
                         String message = dataInputStream.readUTF();
                         //Connect to other server
                         if (message.contains("REGISTER")) {
                             System.out.println("Connecting new node");
                             String nodeInfo = message.replace("REGISTER_", "");
                             String address = nodeInfo.split("_")[0];
                             String port = nodeInfo.split("_")[1];
                             Socket ss = new Socket(address, Integer.parseInt(port));
                             ConnectionToOtherServer node = new ConnectionToOtherServer(ss);
                             node.sendMessage("ROLE_2");
                             serverList.add(node);
                             NodeInfo info = new NodeInfo(address, Integer.parseInt(port));
                             activeServerList.add(info);
                             /*ByteArrayOutputStream bao = new ByteArrayOutputStream();
                             ObjectOutputStream obj = new ObjectOutputStream(bao);
                             obj.writeObject(activeServerList);
                             obj.close();*/
                         } else {
                             System.out.println(socket.getInetAddress().getHostAddress() + "  Received message " + message);
                             messages.put(message);
                             //Send also broadCast to other server
                             broadcast(message);
                         }

                     } catch (IOException | InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
            });

            readFromClient.setDaemon(true);
            readFromClient.start();
        }

        public void sendMessage(String msg) throws IOException {
            //System.out.println("Send message "  + msg);
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
                 while (true) {
                     try {
                         //Read message From other server
                         String message = dataInputStream.readUTF();
                         System.out.println("Message from server " + message);
                         messages.put(message);

                     } catch (IOException | InterruptedException e) {
                         e.printStackTrace();
                     }
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

        @Override
        public String toString() {
            return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }
    }

    public void sendServerList(Socket socket)  {
        OutputStream outputStream;
        try {
            outputStream = socket.getOutputStream();
            try (ObjectOutputStream objectOutputStream =
                         new ObjectOutputStream(outputStream)) {
                objectOutputStream.writeObject(serverList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void join() throws InterruptedException {
        messageHandling.join();
        read.join();
        accept.join();
        readFromClient.join();
    }

    public void close() throws IOException {
        if(node != null) {
            node.close();
        }
    }
    public int numberOfActiveNode() {
        return serverList.size();
    }
}
