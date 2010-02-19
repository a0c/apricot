package ui.base;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 18.12.2008
 * <br>Time: 1:43:35
 */
public class NodeItem extends AbstractItem {
    private final int graphIndex;
    private final int nodeIndex;

    public NodeItem(int graphIndex, int nodeIndex) {
        this.graphIndex = graphIndex;
        this.nodeIndex = nodeIndex;
    }

    /* To be implemented */

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        NodeItem nodeItem = (NodeItem) obj;

        return graphIndex == nodeItem.graphIndex && nodeIndex == nodeItem.nodeIndex;

    }

    public int hashCode() {
        int result;
        result = graphIndex;
        result = 31 * result + nodeIndex;
        return result;
    }
}
