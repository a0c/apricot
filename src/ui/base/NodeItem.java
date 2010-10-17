package ui.base;

/**
 * @author Anton Chepurov
 */
public class NodeItem extends AbstractItem {
	private final int graphIndex;
	private final int nodeIndex;

	public NodeItem(int graphIndex, int nodeIndex) {
		this.graphIndex = graphIndex;
		this.nodeIndex = nodeIndex;
	}

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
