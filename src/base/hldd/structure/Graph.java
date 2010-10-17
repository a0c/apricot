package base.hldd.structure;

import base.hldd.structure.nodes.Node;

/**
 * @author Anton Chepurov
 */
public class Graph {

	/**
	 * Graph index
	 */
	private int index;
	/**
	 * Graph root node
	 */
	private Node rootNode;

	public Graph(Node rootNode) {
		this.rootNode = rootNode;
	}

	public String toString() {
		int size = getSize();
		StringBuilder sb = new StringBuilder("GRP#\t" + index + ":  BEG =  " + rootNode.getAbsoluteIndex() + ", LEN = " + size + " -----\n");

		String[] nodesAsStrings = rootNode.toStringArray(new String[size]);
		for (String nodeAsString : nodesAsStrings) {
			sb.append(nodeAsString).append("\n");
		}

		return sb.toString();
	}

	/* Getters START */

	public Node getRootNode() {
		return rootNode;
	}

	public int getSize() {
		return rootNode.getSize();
	}

	public int getIndex() {
		return index;
	}

	/* Getters END */

	public void setIndex(int index) {
		this.index = index;
	}

	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}
}
