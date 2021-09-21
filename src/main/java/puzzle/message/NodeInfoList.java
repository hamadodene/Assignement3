package puzzle.message;

import java.io.Serializable;
import java.util.ArrayList;

public class NodeInfoList implements Serializable {

    private ArrayList<NodeInfo> activeNode;

    public NodeInfoList(ArrayList<NodeInfo> activeNode) {
        this.activeNode = activeNode;
    }

    public ArrayList<NodeInfo> getActiveNode() {
        return activeNode;
    }

    public void setActiveNode(ArrayList<NodeInfo> activeNode) {
        this.activeNode = activeNode;
    }
}
