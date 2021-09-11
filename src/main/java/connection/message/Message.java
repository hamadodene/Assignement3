package connection.message;

import java.io.Serializable;

public class Message implements Serializable {
    private String message;
    private boolean isServer;

    public Message(String message, boolean isServer) {
        this.message = message;
        this.isServer = isServer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean server) {
        isServer = server;
    }
}
