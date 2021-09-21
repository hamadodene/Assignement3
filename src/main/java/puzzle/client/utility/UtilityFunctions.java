package puzzle.client.utility;

import puzzle.client.game.Tile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UtilityFunctions {

    private static UtilityFunctions INSTANCE;

    public static UtilityFunctions getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new UtilityFunctions();
        }
        return INSTANCE;
    }

    public List<Integer> convert(List<Integer> startList){
        List<Integer> convertedList = IntStream.range(0, startList.size()).boxed().collect(Collectors.toList());
        IntStream.range(0, startList.size()).forEach( e-> {
            convertedList.add(startList.get(e), e);
            convertedList.remove(startList.get(e)+1);
        });
        return convertedList;
    }

    public List<Integer> fromTileToList(List<Tile> tileList){
        return tileList.stream()
                .map(Tile::getOriginalPosition)
                .collect(Collectors.toList());
    }
}