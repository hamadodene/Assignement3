package puzzle.message;

import puzzle.server.TimeStamp;

import java.io.Serializable;
import java.sql.Timestamp;

public class RicartAgrawalaMessage implements Serializable {

    private String message;
    Message type;
    private int positionFirstPuzzle;
    private int positionSecondPuzzle;
    private Timestamp timeStamp;

    public RicartAgrawalaMessage(String message, Message type, int positionFirstPuzzle, int positionSecondPuzzle, Timestamp timeStamp) {
        this.message = message;
        this.type = type;
        this.positionFirstPuzzle = positionFirstPuzzle;
        this.positionSecondPuzzle = positionSecondPuzzle;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message getType() {
        return type;
    }

    public int getPositionFirstPuzzle() {
        return positionFirstPuzzle;
    }

    public int getPositionSecondPuzzle() {
        return positionSecondPuzzle;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
