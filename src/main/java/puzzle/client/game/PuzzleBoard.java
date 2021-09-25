package puzzle.client.game;

import puzzle.client.Client;
import puzzle.client.MessagesQueue;
import puzzle.client.ServerConnectionHandler;
import puzzle.client.utility.UtilityFunctions;
import puzzle.message.TileMessage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("serial")
public class PuzzleBoard extends JFrame {
	
	final int rows, columns;
	private List<Tile> tiles = new ArrayList<>();
    private final UtilityFunctions uFunctions = UtilityFunctions.getInstance();
    private List<Integer> randomPositions;
    private BufferedImage image;
    private Client client;
    private Thread messageHandling;
    private MessagesQueue queue;
    private final JPanel board;
	private SelectionManager selectionManager = new SelectionManager();
	ServerConnectionHandler server;
	private boolean waitingAgrawalaCheck = false;
	
    public PuzzleBoard(final int rows, final int columns, final String imagePath, Client client, MessagesQueue queue, List<Integer> randomPositions) {
    	this.rows = rows;
		this.columns = columns;
		this.client = client;
		this.randomPositions = randomPositions;
		this.queue = queue;
    	this.server = client.getServerConnectionHandler();
    	setTitle("Puzzle");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        board = new JPanel();

        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
        
        createTiles(imagePath);
        paintPuzzle(board);
    }

    
    private void createTiles(final String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;
        
        final List<Integer> randomPositions = new ArrayList<>();
        IntStream.range(0, rows*columns).forEach(item -> { randomPositions.add(item); }); 
        Collections.shuffle(randomPositions);
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
            	final Image imagePortion = createImage(new FilteredImageSource(image.getSource(),
                        new CropImageFilter(j * imageWidth / columns, 
                        					i * imageHeight / rows, 
                        					(imageWidth / columns), 
                        					imageHeight / rows)));

                tiles.add(new Tile(imagePortion, position, randomPositions.get(position)));
                position++;
            }
        }
	}
    
    private void paintPuzzle(final JPanel board) {
    	board.removeAll();
    	
    	Collections.sort(tiles);
    	tiles.forEach(tile -> {
    		final TileButton btn = new TileButton(tile);
            board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> {
            	selectionManager.selectTile(tile, () -> {
            		paintPuzzle(board);
                    System.out.println("Position first " + tile.getCurrentPosition() + " Position second " + selectionManager.getSecondTilePosition() );
            		//Send message to server for broadcast to all node
                    TileMessage message = new TileMessage(this.getPositions(), tile.getCurrentPosition(), selectionManager.getSecondTilePosition() );
            		client.sendTileToServer(message);
            		waitingAgrawalaCheck = true;
                    if (!server.discardChange) {
                        waitingAgrawalaCheck = false;
                        checkSolution();
                    } else {
                        //Rollback
                        refreshPuzzle();
                        waitingAgrawalaCheck = false;
                    }
            	});
            });
    	});
    	
    	pack();
        //setLocationRelativeTo(null);
    }

    public List<Integer> getPositions(){
        return this.tiles.stream().map(Tile::getOriginalPosition).collect(Collectors.toList());
    }

    private void checkSolution() {
    	if(tiles.stream().allMatch(Tile::isInRightPlace)) {
    		JOptionPane.showMessageDialog(this, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
    	}
    }

    private void refreshPuzzle(){
        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;

        List<Tile> newTiles = new ArrayList<>();

        List<Integer> newPositions = uFunctions.convert(randomPositions);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final Image imagePortion = createImage(new FilteredImageSource(image.getSource(),
                        new CropImageFilter(j * imageWidth / columns,
                                i * imageHeight / rows,
                                (imageWidth / columns),
                                imageHeight / rows)));

                newTiles.add(new Tile(imagePortion, position, newPositions.get(position)));
                position++;
            }
        }
        this.tiles = newTiles;
        System.out.println("Posizioni refresh: " + this.randomPositions);
    }

    public void startMessagesQueueHandling() {
        messageHandling = new Thread(() -> {
            while (true) {
                try {
                    TileMessage message = queue.take();
                    List<Integer> remoteRandomPosition = message.getTiles();
                    if(!this.randomPositions.equals(remoteRandomPosition) && !waitingAgrawalaCheck) {
                        this.randomPositions = remoteRandomPosition;
                        this.refreshPuzzle();
                        this.paintPuzzle(this.board);
                        System.out.println("Refresh Tiles completed");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    public void join() throws InterruptedException {
        messageHandling.join();
    }
}
