package base.hldd.structure.nodes.utils;

import base.hldd.structure.nodes.Node;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Anton Chepurov
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

	private int getMaxRelativeIndex(Node rootNode) {
		int maxIndex = rootNode.getRelativeIndex();
		if (!checkedIndices.contains(maxIndex)) {
			checkedIndices.add(maxIndex);
			if (rootNode.isControlNode()) {
				for (Node successor : rootNode.getSuccessors()) {
					int successorMaxIndex = getMaxRelativeIndex(successor);
					if (successorMaxIndex > maxIndex) {
						maxIndex = successorMaxIndex;
					}
				}
			}
		}
		return maxIndex;
	}

}
