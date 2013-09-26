package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.GraphVariable;

import java.util.*;

/**
 * <b>NB!</b> Class assumes that the model under traversal has been minimized.
 *
 * @author Anton Chepurov
 */
public class TerminalNodeCollector implements HLDDVisitor {
	/* MUX_ADDR VALUES mapping, where:
	* key is GraphVariable and value is a list of terminal nodes */
	private Map<GraphVariable, List<Node>> muxAddrValueMap;
	private List<GraphVariable> valueRetainingNodeUsed;
	private List<Node> terminalNodes;

	/* Temporary variable used for extinguishing Terminal Node for RETAINING value */
	private GraphVariable currentGraphVariable;


	public TerminalNodeCollector() {
		muxAddrValueMap = new HashMap<GraphVariable, List<Node>>();
		valueRetainingNodeUsed = new ArrayList<GraphVariable>();
	}

	public void visitNode(Node node) throws Exception {

		if (node.isTerminalNode() || node.isIndexNode()) {
			/* Skip Terminal Node that retains value */
			if (node.getDependentVariable() == currentGraphVariable) {
				valueRetainingNodeUsed.add(currentGraphVariable);
				return;
			}
			if (!terminalNodes.contains(node)) {
				terminalNodes.add(node);
			}
		} else {
			if (node.getDependentVariable().isReset()) {
				/* Traverse the sub-tree of inactive reset */
				node.getSuccessor(Condition.FALSE).traverse(this);
			} else {
				for (Node successor : node.getSuccessors()) {
					successor.traverse(this);
				}
			}
		}
	}

	public void visitGraphVariable(GraphVariable graphVariable) throws Exception {
		terminalNodes = new ArrayList<Node>();
		currentGraphVariable = graphVariable;
		graphVariable.getGraph().getRootNode().traverse(this);

		muxAddrValueMap.put(graphVariable, terminalNodes);
	}

	public List<Node> getTerminalNodes(GraphVariable graphVariable) throws Exception {
		if (!muxAddrValueMap.containsKey(graphVariable))
			throw new Exception("Specified GraphVariable (" + graphVariable.getName() + ") has no terminal nodes to return");
		return muxAddrValueMap.get(graphVariable);
	}

	public int getMuxAddrLength(GraphVariable graphVariable) {
		return Integer.toBinaryString(getTerminalNodesCount(graphVariable)).length();
	}

	public int getTerminalNodesCount(GraphVariable graphVariable) {
		return muxAddrValueMap.containsKey(graphVariable)
				? muxAddrValueMap.get(graphVariable).size()
				: -1;
	}

	public boolean isValueRetainingUsed(GraphVariable graphVariable) {
		return valueRetainingNodeUsed.contains(graphVariable);
	}

	// todo: rename to getMuxAddrFor()

	public int getTransitionValue(GraphVariable graphVariable, Node terminalNode) throws Exception {
		if (!muxAddrValueMap.containsKey(graphVariable)) {
			throw new Exception("Cannot return TRANSITION INDEX for the following GraphVariable: " + graphVariable.getName() +
					"\nNo terminal nodes have been registered for this variable.");
		}
		List<Node> terminalNodes = muxAddrValueMap.get(graphVariable);
		for (Node termNode : terminalNodes) {
			if (termNode == terminalNode) {
				return terminalNodes.indexOf(termNode);
			}
		}
		throw new Exception("Cannot return TRANSITION INDEX for the following GraphVariable: " + graphVariable.getName() +
				"\nRequested terminal node is not found amongst the list of the GraphVariable's terminal nodes: " + terminalNode);
	}

	public void fillMuxAddrNode(GraphVariable graphVariable, Node muxAddrNode) throws Exception {
		if (!(muxAddrValueMap.containsKey(graphVariable))) throw new Exception("Todo: "); //todo...

		List<Node> terminalNodes = muxAddrValueMap.get(graphVariable);
		for (int index = 0; index < terminalNodes.size(); index++) {
			muxAddrNode.setSuccessor(Condition.createCondition(index), Node.clone(terminalNodes.get(index)));
		}
	}
}
