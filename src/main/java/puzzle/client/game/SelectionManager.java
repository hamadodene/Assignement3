package puzzle.client.game;

public class SelectionManager {

    private boolean selectionActive = false;
    private Tile selectedTile;
    private int secondTilePosition;

    public void selectTile(final Tile tile, final Listener listener) {
        if (selectionActive) {
            selectionActive = false;
            secondTilePosition = tile.getCurrentPosition();
            swap(selectedTile, tile);

            listener.onSwapPerformed();
        } else {
            selectionActive = true;
            selectedTile = tile;
        }
    }

    private void swap(final Tile t1, final Tile t2) {
        int pos = t1.getCurrentPosition();
        t1.setCurrentPosition(t2.getCurrentPosition());
        t2.setCurrentPosition(pos);
    }
    public int getSecondTilePosition() {
        return secondTilePosition;
    }

    public void setSecondTilePosition(int secondTilePosition) {
        this.secondTilePosition = secondTilePosition;
    }

    @FunctionalInterface
    interface Listener {
        void onSwapPerformed();
    }
}
