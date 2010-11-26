package base.hldd.structure.variables.utils;

import base.hldd.structure.nodes.utils.Condition;
import parsers.hldd.Collector;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.TemporalNode;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.Graph;
import base.Indices;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map;

/**
 * GraphVariableCreator for PPG Library.
 * Facilitates 2 additional key features:
 * <br>1) creating graphs from <b>cyclic</b> HLDD-s;
 * <br>2) creating {@link base.hldd.structure.nodes.TemporalNode}-s,
 * currently found in PPG Library only (notion of
 * <i>temporal window</i>).
 *
 * @author Anton Chepurov
 */
public class PPGLibraryGraphVariableCreator implements GraphVariableCreator {
	public void createGraphVariables(Collector collector) throws Exception {

		if (collector.getSize() - collector.getGraphOffset() > 1)
			throw new Exception("PPG contains more than 1 GraphVariable. Only 1 GV is allowed!");
		int startingIndex = 0;
		CyclicNodes cyclicNodes = new CyclicNodes();
		Object o = collector.getVarObject(collector.getGraphOffset());
		if (o instanceof Collector.GraphVariableData) {
			Collector.GraphVariableData graphVariableData = (Collector.GraphVariableData) o;
			/* Create Nodes */
			Collector.NodeData[] nodeData = graphVariableData.nodes;
			HashMap<Integer, Node> nodeByIndex = new HashMap<Integer, Node>();
			for (int i = nodeData.length - 1; i >= 0; i--) {
				/* Iterate in reversed order, to initiate Terminal Nodes first */
				/* Get data from holders */
				Collector.NodeData nodeDatum = nodeData[i];
				Object depVarObject = collector.getVarObject(nodeDatum.depVarIndex);
				TreeMap<Condition, Integer> successors = nodeDatum.successors;
				Indices depVarRange = nodeDatum.depVarRange;
				String[] windowPlaceholders = nodeDatum.windowPlaceholders;
				AbstractVariable dependentVariable;
				if (depVarObject instanceof AbstractVariable) {
					dependentVariable = (AbstractVariable) depVarObject;
				} else if (depVarObject instanceof Collector.GraphVariableData) {
					dependentVariable = ((Collector.GraphVariableData) depVarObject).graphVariable;
				} else throw new Exception("Error while creating nodes for GraphVariable in PPG Library:" +
						"\nDependent variable for a node is neither an AbstractVariable nor a GraphVariableData");
				/* Create and hash node */
				Node newNode;
				if (windowPlaceholders != null) {
					if (successors != null) {
						newNode = new TemporalNode.Builder(dependentVariable).range(depVarRange).createSuccessors(Condition.countValues(successors.keySet())).windowPlaceholders(windowPlaceholders).build();
					} else throw new Exception("Error while creating nodes for GraphVariable in PPG Library:" +
							"\nWindow is assigned to Terminal node! Notion of window is applicable to Control nodes (Boolean expressions or PSL Property) only");
				} else {
					newNode = successors == null ? new Node.Builder(dependentVariable).range(depVarRange).build()
							: new Node.Builder(dependentVariable).range(depVarRange).createSuccessors(Condition.countValues(successors.keySet())).build();
				}
				nodeByIndex.put(i, newNode);
				/* Index node manually */
				newNode.setRelativeIndex(i);
				newNode.setAbsoluteIndex(startingIndex + i);
				/* For control nodes, fill successors */
				if (successors != null) {
					for (Map.Entry<Condition, Integer> entry : successors.entrySet()) {
						Condition condition = entry.getKey();
						Integer successorIndex = entry.getValue();

						/* Note that cycles are allowed here, so nodeByIndex.get(successors[j]) may return null */
						Node successor = nodeByIndex.get(successorIndex);
						if (successor != null) {
							newNode.setSuccessor(condition, successor);
						} else {
							cyclicNodes.postponeCyclicNode(newNode, condition, successorIndex);
						}
					}
				}
			}
			/* Fill postponed cyclic nodes */
			cyclicNodes.fillCyclicNodes(nodeByIndex);

			/* Create real GraphVariable and add it to collector */
			Node rootNode = nodeByIndex.get(0);
			Graph newGraph = new Graph(rootNode);
			newGraph.setIndex(graphVariableData.graphIndex);
			graphVariableData.graphVariable.setGraph(newGraph);
			collector.addGraphVariable(graphVariableData.graphVariable);

		} else throw new Exception("No property graph is found while creating PPG GraphVariable");

	}

	private class CyclicNodes {
		private List<CyclicNode> cyclicNodeList = new LinkedList<CyclicNode>();

		public void postponeCyclicNode(Node cyclicNode, Condition successorCondition, int successorIndex) {
			cyclicNodeList.add(new CyclicNode(cyclicNode, successorCondition, successorIndex));
		}

		public void fillCyclicNodes(HashMap<Integer, Node> nodeByIndex) throws Exception {
			for (CyclicNode cyclicNode : cyclicNodeList) {
				cyclicNode.cyclicNode.setSuccessor(cyclicNode.successorCondition, nodeByIndex.get(cyclicNode.successorIndex));
			}
		}

		private class CyclicNode {
			private final Node cyclicNode;
			private final Condition successorCondition;
			private final int successorIndex;

			public CyclicNode(Node cyclicNode, Condition successorCondition, int successorIndex) {
				this.cyclicNode = cyclicNode;
				this.successorCondition = successorCondition;
				this.successorIndex = successorIndex;
			}
		}
	}
}
