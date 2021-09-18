package connection.message;

import java.io.Serializable;
import java.util.List;

public class TileMessage implements Serializable {
    private List<Integer> tiles;

    public TileMessage(List<Integer> tiles) {
        this.tiles = tiles;
    }

    public List<Integer> getTiles() {
        return tiles;
    }

    public void setTiles(List<Integer> tiles) {
        this.tiles = tiles;
    }

    @Override
    public String toString() {
        return tiles.toString();
    }
}
