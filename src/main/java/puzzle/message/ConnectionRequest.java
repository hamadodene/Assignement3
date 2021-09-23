package puzzle.message;

import java.io.Serializable;

public class ConnectionRequest implements Serializable {
    private NodeInfo nodeInfo;
    private boolean alreadyHaveNodeList;
    public ConnectionRequest(NodeInfo nodeInfo, boolean alreadyHaveNodeList) {
        this.nodeInfo = nodeInfo;
        this.alreadyHaveNodeList = alreadyHaveNodeList;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public boolean getAlreadyHaveNodeList() {
        return alreadyHaveNodeList;
    }

    public void setAlreadyHaveNodeList(boolean alreadyHaveNodeList) {
        this.alreadyHaveNodeList = alreadyHaveNodeList;
    }
}
