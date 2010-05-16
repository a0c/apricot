package base.hldd.structure.nodes.utils;

import base.hldd.structure.nodes.Node;

import java.util.Set;
import java.util.HashSet;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.09.2008
 * <br>Time: 19:39:15
 */
public class MaxRelativeIndexCounter {
    private final Node rootNode;
    private final Set<Integer> checkedIndices;

    public MaxRelativeIndexCounter(Node rootNode) {
        this.rootNode = rootNode;
        checkedIndices = new HashSet<Integer>();
    }

    int count() {
        return getMaxRelativeIndex(rootNode);
    }

    /**
     * Traverse the tree from rootNode and adjust the maximum relative index.
     *
     * @param rootNode
     * @return
     */
    private int getMaxRelativeIndex(Node rootNode) {
        int maxIndex = rootNode.getRelativeIndex();
        if (!checkedIndices.contains(maxIndex)) {
            checkedIndices.add(maxIndex);
            if (rootNode.isControlNode()) {
                for (Node successor : rootNode.getSuccessors()) {
					int succMaxIndex = getMaxRelativeIndex(successor);
					if (succMaxIndex > maxIndex) {
						maxIndex = succMaxIndex;
					}
				}
            }
        }
        return maxIndex;
    }

}
