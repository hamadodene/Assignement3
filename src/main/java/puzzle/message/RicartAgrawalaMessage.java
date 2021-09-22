package puzzle.message;

import java.io.Serializable;

public class RicartAgrawalaMessage implements Serializable {

    private String message;
    Message type;

    public RicartAgrawalaMessage(String message, Message type) {
        this.message = message;
        this.type = type;
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

    public void setType(Message type) {
        this.type = type;
    }
}
