package base.hldd.structure.nodes.utils;

import base.HLDDException;
import base.SourceLocation;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.visitors.SourceLocationMerger;

import java.util.*;

/**
 * Provides utility operations on nodes
 *
 * @author Anton Chepurov
 */
public class Utility {

	// todo: remove these sets because they occupy space. Implement methods like isVariableUsedAsTerminal().
	private static Set<Node> uniqueControlNodes;
	private static Set<Node> processedControlNodesSet;
	private static Set<Node> collectedNodesSet;

	/**
	 * Counts Control Nodes.
	 * Allows duplicates (i.e. duplicate Control Nodes are counted separately).
	 *
	 * @param node where to start counting from
	 * @return number of control nodes
	 */
	public static int countControlNodes(Node node) {
		processedControlNodesSet = new HashSet<Node>();
		return cntControlNodes(node);
	}

	private static int cntControlNodes(Node node) {
		int count = 0;
		if (processedControlNodesSet.contains(node)) return count;
		if (node.isControlNode()) {
			count++;
			processedControlNodesSet.add(node);
			for (Node successorNode : node.getSuccessors()) {
				if (successorNode != null) {
					count += cntControlNodes(successorNode);
				}
			}
		}
		return count;
	}

	public static int countUniqueControlNodes(Node node) {
		uniqueControlNodes = new LinkedHashSet<Node>();
		return cntUniqueControlNodes(node);
	}

	private static int cntUniqueControlNodes(Node node) {
		int count = 0;
		if (node.isControlNode() && !containsIdenticalControlNode(node)) {
			uniqueControlNodes.add(node);
			count++;
			for (Node successorNode : node.getSuccessors()) {
				if (successorNode != null) {
					count += cntUniqueControlNodes(successorNode);
				}
			}
		}
		return count;
	}

	private static boolean containsIdenticalControlNode(Node nodeToFind) {
		for (Node node : uniqueControlNodes) {
			if (node.isIdenticalTo(nodeToFind)) return true;
		}
		return false;
	}

	public static boolean isVariableUsedAsTerminal(Node rootNode, AbstractVariable variable) {
		return isVariableUsedAsTerminal(rootNode, variable, new HashSet<Node>());
	}

	private static boolean isVariableUsedAsTerminal(Node node, AbstractVariable variable, Set<Node> usedNodesSet) {
		if (!usedNodesSet.contains(node)) {
			usedNodesSet.add(node);
			if (node.isTerminalNode()) {
				if (node.getDependentVariable() == variable) {
					return true;
				}
			} else {
				for (Node successorNode : node.getSuccessors()) {
					if (isVariableUsedAsTerminal(successorNode, variable, usedNodesSet)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Traverse the tree from rootNode and adjust the maximum relative index.
	 * <p/>
	 * <b>NB!</b> Tree of rootNode must be <u>indexed</u><br>
	 *
	 * @param rootNode root node of the tree to calculate the size of
	 * @return number of nodes in the tree with the specified <code>rootNode</code>
	 */
	public static int getSize(Node rootNode) {
		return new MaxRelativeIndexCounter(rootNode).count() + 1;
	}

	/**
	 * Reuses identical nodes with the following steps:<br>
	 * 1) Simplifies (minimizes) the rootNode tree with the use of a usedNodes set.<br>
	 * 2) Strips indices from the nodes in rootNode tree.<br>
	 * 3) Reindexes the rootNode tree.
	 *
	 * @param nodeToTrim node to minimize
	 * @param rootNodeAbsIndex absolute index of this node
	 */
	public static void minimize(Node nodeToTrim, int rootNodeAbsIndex) {
		/* Simplify (minimize) the rootNode tree */
		try {
			new Minimizer(nodeToTrim).minimize(); // todo: move next step (stripping indices) into the Minimizer, to traverse the tree once only. Bad idea (code clarity deteriorates).
		} catch (HLDDException e) {
			throw new RuntimeException(e);
		}
		/* Reindex the node's tree */
		indexate(nodeToTrim, rootNodeAbsIndex);
	}

	public static Node reduce(Node nodeToReduce, int rootNodeAbsIndex) {
		/* Reduce rootNode */
		Reducer reducer = new Reducer(nodeToReduce);
		try {
			reducer.reduce();
		} catch (HLDDException e) {
			throw new RuntimeException(e);
		}
		/* Get new root node */
		Node rootNode = reducer.getRootNode();
		/* Reindex the rootNode's tree */
		indexate(rootNode, rootNodeAbsIndex);
		/* Return new root node */
		return rootNode;
	}

	public static void indexate(Node nodeToIndexate, int startingIndex) {
		/* Strip indices */
		new NodeIndexStripper(nodeToIndexate).strip();
		/* Index nodes */
		new NodeIndexator(nodeToIndexate, startingIndex, false).indexate();
	}

	public static int getUnindexedSize(Node node) {
		collectedNodesSet = new HashSet<Node>();
		getUnSize(node);
		return collectedNodesSet.size();
	}

	private static void getUnSize(Node node) {
		if (!collectedNodesSet.contains(node)) {
			collectedNodesSet.add(node);
			if (node.isControlNode()) {
				for (Node successor : node.getSuccessors()) {
					if (successor == null) {
						System.out.println("Null successor found!");
					}
					getUnSize(successor);
				}
			}
		}
	}


	//todo: move to "utils" package and remove static modifier

	private static class NodeIndexator {
		private final int startingIndex;
		private int controlIndex, terminalIndex;
		private Node rootNode;
		private boolean doRemoveLoops;
		/* Auxiliary fields */
		private Set<Node> loopingNodes;


		public NodeIndexator(Node rootNode, int startingIndex, boolean doRemoveLoops) {
			this.rootNode = rootNode;
			this.startingIndex = startingIndex;
			controlIndex = 0;
			terminalIndex = doRemoveLoops ? countUniqueControlNodes(rootNode) : countControlNodes(rootNode);//todo...
			loopingNodes = new LinkedHashSet<Node>();
			this.doRemoveLoops = doRemoveLoops;
		}

		public void indexate() {
			/* Indexate */
			setIndices(rootNode);
			/* Reindexate looping nodes */
			if (doRemoveLoops) new LoopRemover().removeLoops();
		}

		private void setIndices(Node node) {
			/* Set new index for unindexed nodes only */
			if (node.getRelativeIndex() != -1) return;

			setNewIndex(node);
			if (node.isControlNode()) {
				for (Node successorNode : node.getSuccessors()) {
					detectLoop(successorNode, node);
					setIndices(successorNode);
				}
			}
		}

		/**
		 * A loop is detected, when the parentNode has a controlNode child
		 * that has already been indexed.
		 * <p/>
		 * todo: Mark the parent node as being processed in order to detect infinite loops.
		 *
		 * @param successorNode some successor node of the
		 *                      <code>parentNode</code>
		 * @param parentNode	parent node of the <code>successorNode</code>.
		 *                      This is the node being recorded when a loop
		 *                      is detected
		 */
		private void detectLoop(Node successorNode, Node parentNode) {
			/* Ignore loops when trim should not be done */
			if (!doRemoveLoops) return;
			/* A loop is detected */
			if (successorNode.isControlNode()) {
				if (successorNode.getRelativeIndex() != -1
						&& successorNode.getRelativeIndex() < parentNode.getRelativeIndex()) {
					/* If the successor index has already been set, then
					* check it to be smaller than the parent's one */
					loopingNodes.add(parentNode);
				}
			}

		}

		private void setNewIndex(Node node) {
			setIndex(node, node.isTerminalNode() ? terminalIndex++ : controlIndex++);
		}

		private void setIndex(Node node, int index) {
			node.setRelativeIndex(index);
			node.setAbsoluteIndex(startingIndex + node.getRelativeIndex());
		}


		/**
		 * Removes loops by means of looping subtree reindexing, which is
		 * actually a down-shifting of the looping subtree (looping child
		 * of the parent).
		 * <p/>
		 * This implementation implies that recieved graph(tree) is minimal.
		 * Currently the latter is granted by GraphGenerator that produces
		 * a minimal DD by usage of a set of usedNodes. Thus, loops can be
		 * removed simply by shifting (moving) them down the tree until <i>
		 * right after</i> the last ControlNode that uses the looping subtree.
		 * Thus, the size of the DD remains unchanged (particularly the size of
		 * the control part), while the looping subtree appears <i>after</i>
		 * the last node that uses it - so the loop is flattened out, it
		 * disappears.
		 */
		private class LoopRemover {
			private int nextIndex;
			private Set<Node> reindexedNodes;

			public void removeLoops() {
				for (Node loopingNode : loopingNodes) {
					removeLoop(loopingNode);
				}
			}

			/**
			 * At first, the looping subtree is reindexed.
			 * Then, all the controlNodes that initially resided between the
			 * looping subtree and the <code>loopingNode</code> are shifted up
			 * by reindexing (decrementing their indices by the size of the
			 * looping tree).
			 * <p/>
			 * todo: Beware of infinite loops (when the looping subtree calls itself recursively)
			 *
			 * @param loopingNode a ControlNode that has at least one child
			 *                    residing before the ControlNode in the tree.
			 */
			private void removeLoop(Node loopingNode) {
				int loopingNodeIndex = loopingNode.getRelativeIndex();
				reindexedNodes = new LinkedHashSet<Node>();
				int smallestReindexedInd = Integer.MAX_VALUE;
				int loopsSize = 0;
				for (Node successor : loopingNode.getSuccessors()) {
					int loopingChildIndex = successor.getRelativeIndex();
					/* Process LOOPING CHILDREN only */
					if (loopingChildIndex < loopingNodeIndex) {
						if (loopingChildIndex < smallestReindexedInd) smallestReindexedInd = loopingChildIndex;
						/* ###############################
						*	Reindex LOOPING SUB-TREE
						*  ############################### */
						/* Get the size of the loop */
						int loopSize = countUniqueControlNodes(successor);
						loopsSize += loopSize;
						/* Set new index to the one following the parent looping node */
						nextIndex = loopingNodeIndex - loopSize + 1;
						reindexLoop(successor);
					}
				}
				/* ###############################
				*      Reindex SUB-TREE between
				*  LOOPING SUB-TREE and LoopingNode
				*  ############################### */
				reindexTheRest(rootNode, smallestReindexedInd, loopingNodeIndex, loopsSize);
			}

			private void reindexTheRest(Node nodeToReindex, int lowestBoundIndex, int highestBoundIndex, int indexOffset) {
				/* Reindex CONTROL NODES only */
				if (nodeToReindex.isControlNode()) {
					/* Reindex controlNodes whose index is between
					* the lowestBoundIndex and highestBoundIndex */
					int relativeIndex = nodeToReindex.getRelativeIndex();
					if (!reindexedNodes.contains(nodeToReindex)
							&& lowestBoundIndex < relativeIndex && relativeIndex <= highestBoundIndex) {
						setIndex(nodeToReindex, relativeIndex - indexOffset);
						reindexedNodes.add(nodeToReindex);
					}
					/* Reindex successors */
					for (Node successor : nodeToReindex.getSuccessors()) {
						reindexTheRest(successor, lowestBoundIndex, highestBoundIndex, indexOffset);
					}
				}
			}

			private void reindexLoop(Node nodeInsideLoop) {
				if (nodeInsideLoop.isControlNode() && !reindexedNodes.contains(nodeInsideLoop)) {
					setIndex(nodeInsideLoop, nextIndex++);
					reindexedNodes.add(nodeInsideLoop);
					for (Node successor : nodeInsideLoop.getSuccessors()) {
						reindexLoop(successor);
					}
				}
			}

		}
	}

	/**
	 * Reduces redundant ControlNodes.
	 *
	 * @see #isRedundant(base.hldd.structure.nodes.Node) definition of redundancy.
	 *      <p/>
	 *      todo: Consider if SourceLocation must be moved from reduced ControlNode into
	 *      todo: replacing node...
	 */
	private static class Reducer {
		private Node rootNode;
		private final Stack<Node> parentNodesStack = new Stack<Node>();
		private final Stack<Condition> parentConditionsStack = new Stack<Condition>();

		public Reducer(Node rootNode) {
			this.rootNode = rootNode;
		}

		public Node getRootNode() {
			return rootNode;
		}

		public void reduce() throws HLDDException {
			doPush(null, null);
			reduceNode(rootNode);
			doPop();
			if (!parentNodesStack.isEmpty()) {
				System.out.println("parentNodesStack is NOT empty when reducing model!!!");
			}
			if (!parentConditionsStack.isEmpty()) {
				System.out.println("parentConditionsStack is NOT empty when reducing model!!!");
			}
		}

		private void reduceNode(Node node) throws HLDDException {
			if (node.isControlNode()) {
				/* Reduce each successor */
				int conditionsCount = node.getConditionsCount();
				for (int idx = 0; idx < conditionsCount; idx++) {
					Condition condition = node.getCondition(idx);
					doPush(node, condition);
					reduceNode(node.getSuccessor(condition));
					doPop();
				}
				/* After all successors are reduced, check the node for redundancy. */
				if (isRedundant(node)) {
					/* Reduce redundant node (Reduction rule 1) */
					Node parentNode = parentNodesStack.peek();
					Condition parentCondition = parentConditionsStack.peek();
					Node firstSuccessor = node.getSuccessor(node.getCondition(0));
					/* Collect SourceLocation-s from all successors into the firstSuccessor */
					firstSuccessor.setSource(collectSuccessorSources(node.getSuccessors()));
					if (parentNode == null) {
						rootNode = firstSuccessor;
					} else {
						parentNode.setSuccessor(parentCondition, firstSuccessor);
					}
				}
			}
		}

		private SourceLocation collectSuccessorSources(Collection<Node> successors) {
			Collection<SourceLocation> sources = new LinkedList<SourceLocation>();
			for (Node successor : successors) {
				SourceLocation source = successor.getSource();
				if (source != null) {
					sources.add(source);
				}
			}
			return SourceLocation.createFrom(sources);
		}

		private void doPop() {
			parentNodesStack.pop();
			parentConditionsStack.pop();
		}

		private void doPush(Node parentNode, Condition parentCondition) {
			parentNodesStack.push(parentNode);
			parentConditionsStack.push(parentCondition);
		}

		/**
		 * ControlNode is redundant, if all its successors are identical.
		 *
		 * @param node node to check for redundancy
		 * @return <tt>true</tt> if the specified node is redundant
		 */
		private boolean isRedundant(Node node) {
			Node previousNode = null;
			for (Node successor : node.getSuccessors()) {
				if (previousNode == null) {
					previousNode = successor;
				} else {
					if (!previousNode.isIdenticalTo(successor)) {
						return false;
					}
				}
			}
			return true;
		}
	}

	//todo: move to "utils" package and remove static modifier

	private static class Minimizer {
		private Node rootNode;
		private Set<Node> usedNodes;

		public Minimizer(Node rootNode) {
			this.rootNode = rootNode;
			usedNodes = new LinkedHashSet<Node>();
		}

		public void minimize() throws HLDDException {
			minimizeNode(rootNode);
			compactNode(rootNode); // compact nodes in a separate tree traversal, to keep getIdenticalNode() correct (otherwise, a complex comparison will impair performance)
		}

		private void minimizeNode(Node nodeToTrim) throws HLDDException {
			if (nodeToTrim.isControlNode()) {
				int conditionsCount = nodeToTrim.getConditionsCount();
				for (int idx = 0; idx < conditionsCount; idx++) {
					Condition condition = nodeToTrim.getCondition(idx);
					Node successor = nodeToTrim.getSuccessor(condition);

					successor = getIdenticalNode(successor);
					nodeToTrim.setSuccessor(condition, successor);
					minimizeNode(successor);
				}
			}
		}

		private Node getIdenticalNode(Node node) {
			/* Search for identical amongst usedNodes */
			for (Node usedNode : usedNodes) { //todo: use Collection, and make Node implement equals() for a faster search
				if (usedNode.isIdenticalTo(node)) {
					/* Add VHDL lines to the usedNode */
					new SourceLocationMerger(node).visitNode(usedNode);
					return usedNode;
				}
			}
			/* Identical is not found. Add the node to usedNodes and return it. */
			usedNodes.add(node);
			return node;
		}

		private void compactNode(Node nodeToCompact) throws HLDDException {
			if (nodeToCompact.isControlNode()) {
				nodeToCompact.compact();
				int conditionsCount = nodeToCompact.getConditionsCount();
				for (int idx = 0; idx < conditionsCount; idx++) {
					Condition condition = nodeToCompact.getCondition(idx);
					Node successor = nodeToCompact.getSuccessor(condition);

					compactNode(successor);
				}
			}
		}
	}

	private static class NodeIndexStripper {
		private final Node rootNode;
		private final Set<Node> processedNodesSet = new HashSet<Node>();

		public NodeIndexStripper(Node nodeToStripIndex) {
			this.rootNode = nodeToStripIndex;
		}


		public void strip() {
			stripIndices(rootNode);
		}

		private void stripIndices(Node node) {
			if (!processedNodesSet.contains(node)) {
				cleanIndices(node);
				if (node.isControlNode()) {
					for (Node successor : node.getSuccessors()) {
						stripIndices(successor);
					}
				}
			}
		}

		private void cleanIndices(Node node) {
			node.setRelativeIndex(-1);
			node.setAbsoluteIndex(-1);
			processedNodesSet.add(node);
		}
	}

}
