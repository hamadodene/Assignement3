package puzzle.message;

import java.io.Serializable;
import java.util.List;

public class TileMessage implements Serializable {
    private List<Integer> tiles;
    private int positionFirstPuzzle;
    private int positionSecondPuzzle;

    public TileMessage(List<Integer> tiles,int positionFirstPuzzle, int positionSecondPuzzle) {
        this.tiles = tiles;
        this.positionFirstPuzzle = positionFirstPuzzle;
        this.positionSecondPuzzle = positionSecondPuzzle;
    }

    public List<Integer> getTiles() {
        return tiles;
    }

    @Override
    public String toString() {
        return tiles.toString();
    }
}
