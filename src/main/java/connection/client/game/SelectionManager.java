package connection.client.game;

public class SelectionManager {

	private boolean selectionActive = false;
	private Tile selectedTile;

	public void selectTile(final Tile tile, final Listener listener) {
		
		if(selectionActive) {
			selectionActive = false;
			
			swap(selectedTile, tile);
			
			listener.onSwapPerformed();
		} else {
			selectionActive = true;
			selectedTile = tile;
		}
	}

	//Set position here when receive a new position
	private void swap(final Tile t1, final Tile t2) {
		int pos = t1.getCurrentPosition();
		t1.setCurrentPosition(t2.getCurrentPosition());
		t2.setCurrentPosition(pos);
		System.out.println("Swapped:" + t1.getOriginalPosition() + " - " + t2.getOriginalPosition() );
	}

	public Tile getSelectedTile() {
		return selectedTile;
	}

	public void setSelectedTile(Tile selectedTile) {
		this.selectedTile = selectedTile;
	}

	@FunctionalInterface
	interface Listener{
		void onSwapPerformed();
	}
}
