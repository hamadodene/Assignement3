package puzzle.client;

import puzzle.client.game.PuzzleBoard;
import puzzle.server.Server;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private JTextField ip;
    private JTextField port;
    private JTextField name;
    private String nodeName;
    private JButton joinButton;
    private JButton createButton;
    private Server server;
    private InetAddress serverAddress;
    private int backlog;
    private int serverPort;
    private MessagesQueue messagesQueue;
    private int n;
    private int m;
    private String imagePath;
    private Client client;
    private List<Integer> randomPositions;
    private PuzzleBoard puzzle;
    private boolean serverIsReady = false;

    public Node(String nodeName, InetAddress serverAddress, int serverPort, int backlog, int n, int m, String imagePath) throws IOException {
        this.nodeName = nodeName;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.backlog = backlog;
        this.messagesQueue = new MessagesQueue();
        this.n = n;
        this.m = m;
        this.imagePath = imagePath;
        this.randomPositions = new ArrayList<>();
        initializeNode();
        startMainMenu();
    }

    private void startMainMenu() {

        JFrame frame = new JFrame(nodeName);

        //new Game
        createButton = new JButton("Start Game");
        createButton.setBounds(50, 50, 200, 50);

        //Join another game
        ip = new JTextField("Insert ip address");
        ip.setBounds(50, 200, 200, 30);
        port = new JTextField("Insert port");
        port.setBounds(50, 250, 200, 30);
        name = new JTextField("Insert name");
        name.setBounds(50, 300, 200, 30);
        joinButton = new JButton("Join");
        joinButton.setBounds(50, 350, 200, 30);

        frame.add(createButton);
        frame.add(ip);
        frame.add(port);
        frame.add(name);
        frame.add(joinButton);

        frame.setSize(300, 600);

        frame.setLayout(null);
        frame.setVisible(true);

        createButton.addActionListener(e -> {
            frame.setVisible(false);
            try {
                startGame();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        });

        joinButton.addActionListener(e -> {
            frame.setVisible(false);
            try {
                joinGame();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        });
    }

    private void initializeNode() throws IOException {
        server = new Server(serverAddress, backlog, serverPort);
        server.start();

        if (server.isServerStart()) {
            client = new Client(serverAddress.getHostAddress(), serverPort, nodeName, messagesQueue);
            client.start();
        }
    }

    public void startGame() throws InterruptedException {
        if (client.isClientIsConnected()) {
            //Start gui
            puzzle = new PuzzleBoard(n, m, imagePath, client, messagesQueue, randomPositions);
            puzzle.setVisible(true);

            //Start messages queue processing thread
            puzzle.startMessagesQueueHandling();
            //puzzle.join();
        }
    }

    private void joinGame() throws InterruptedException {
        String remoteIp = ip.getText();
        int remotePort = 0;
        try {
            remotePort = Integer.parseInt(port.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(new JFrame(), "Port not valid", "Port validation", JOptionPane.ERROR_MESSAGE);
        }

        if (!validIP(remoteIp)) {
            JOptionPane.showMessageDialog(new JFrame(), "IP not valid", "Ip validation", JOptionPane.ERROR_MESSAGE);
        }

        if (client.isClientIsConnected()) {
            client.sendConnectionRequest(remoteIp, remotePort, nodeName, true);
            startGame();
        }
    }

    public static boolean validIP(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            if (ip.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
