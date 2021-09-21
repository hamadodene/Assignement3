package puzzle.message;

import java.io.Serializable;

public class ConnectionRequest implements Serializable {
    private NodeInfo nodeInfo;

    public ConnectionRequest(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }
}
