package puzzle.message;

import java.io.Serializable;

public class ConnectionStatusMessage implements Serializable {
    private boolean isServerReady;

    public ConnectionStatusMessage(boolean isServerReady) {
        this.isServerReady = isServerReady;
    }

    public boolean isServerReady() {
        return isServerReady;
    }

    public void setServerReady(boolean serverReady) {
        isServerReady = serverReady;
    }
}
