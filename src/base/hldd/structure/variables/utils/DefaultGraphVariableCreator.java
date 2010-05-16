package base.hldd.structure.variables.utils;

import base.hldd.structure.nodes.utils.Condition;
import parsers.hldd.Collector;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.Graph;
import base.Indices;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map;
import java.util.logging.Logger;

import ui.ExtendedException;

/**
 * GraphVariableCreator for trivial HLDD files.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.09.2008
 * <br>Time: 21:26:13
 */
public class DefaultGraphVariableCreator implements GraphVariableCreator{

	private static final Logger LOG = Logger.getLogger(DefaultGraphVariableCreator.class.getName());

    public void createGraphVariables(Collector collector) throws Exception {
        //todo: cycle with boolean "incomplete", to handle nodes with so far unhashed base variables
        int startingIndex = 0;
        for (int index = collector.getGraphOffset(); index < collector.getSize(); index++) {
            Object o = collector.getVarObject(index);
            if (o instanceof Collector.GraphVariableData) {
                Collector.GraphVariableData graphVariableData = (Collector.GraphVariableData) o;
                /* Create Nodes */
                Collector.NodeData[] nodeData = graphVariableData.nodes;
                HashMap<Integer, Node> nodeByIndex = new HashMap<Integer, Node>();
				List<CyclicNode> cyclicNodes = new LinkedList<CyclicNode>();
                for (int i = nodeData.length - 1; i >= 0; i--) {
                    /* Iterate in reversed order, to initiate Terminal Nodes first */
                    /* Get data from holders */
                    Collector.NodeData nodeDatum = nodeData[i];
                    Object depVarObject = collector.getVarObject(nodeDatum.depVarIndex);
                    TreeMap<Condition,Integer> successors = nodeDatum.successors;
                    Indices depVarPartedIndices = nodeDatum.depVarPartedIndices;
                    AbstractVariable dependentVariable;
                    if (depVarObject instanceof AbstractVariable) {
                        dependentVariable = (AbstractVariable) depVarObject;
                    } else if (depVarObject instanceof Collector.GraphVariableData) {
                        dependentVariable = ((Collector.GraphVariableData) depVarObject).graphVariable;
                    } else throw new Exception("Error while creating nodes for GraphVariable:" +
                            "\nDependent variable for a node is neither an AbstractVariable nor a GraphVariableData");
                    /* Create and hash node */
                    Node newNode = successors == null ? new Node.Builder(dependentVariable).partedIndices(depVarPartedIndices).build()
                            : new Node.Builder(dependentVariable).partedIndices(depVarPartedIndices).createSuccessors(Condition.countValues(successors.keySet())).build();
                    nodeByIndex.put(i, newNode);
                    /* Index node manually */
                    newNode.setRelativeIndex(i);
                    newNode.setAbsoluteIndex(startingIndex + i);
                    /* For control nodes, fill successors */
                    if (successors != null) {
                        for (Map.Entry<Condition, Integer> entry : successors.entrySet()) {
							Condition condition = entry.getKey();
							Integer successorIndex = entry.getValue();

							Node newSuccessor = nodeByIndex.get(successorIndex);
							/* Note that cycles are allowed here, so remember missing successors for further setting */
							if (newSuccessor == null) {
								// For cyclic HLDDs, remember missing successors data
								cyclicNodes.add(new CyclicNode(newNode, condition, successorIndex));
							}
							newNode.setSuccessor(condition, newSuccessor);
                        }
                    }
                }

				fillCyclicSuccessors(cyclicNodes, nodeByIndex);

                /* Create real GraphVariable and add it to collector */
                Node rootNode = nodeByIndex.get(0);
                startingIndex += rootNode.getSize(); //todo: speed-up
                Graph newGraph = new Graph(rootNode);
                newGraph.setIndex(graphVariableData.graphIndex);
                graphVariableData.graphVariable.setGraph(newGraph);
                collector.addGraphVariable(graphVariableData.graphVariable);
            }
        }

    }

	private void fillCyclicSuccessors(List<CyclicNode> cyclicNodes, HashMap<Integer, Node> newNodeByIndex) throws Exception {
		for (CyclicNode cyclicNode : cyclicNodes) {
			Node missingSuccessor = newNodeByIndex.get(cyclicNode.missingNodeRelIndex);
			if (missingSuccessor == null) {
				String message = "cyclic successor is still missing after all the nodes have been processed";
				LOG.fine(message);
				throw new ExtendedException("When creating cyclic HLDD, postponed " + message, ExtendedException.ERROR_TEXT);
			}
			cyclicNode.baseNode.setSuccessor(cyclicNode.controlCondition, missingSuccessor);
		}
	}

	private class CyclicNode {
		private Node baseNode;
		private Condition controlCondition;
		private int missingNodeRelIndex;

		public CyclicNode(Node baseNode, Condition controlCondition, int missingNodeRelIndex) {
			this.baseNode = baseNode;
			this.controlCondition = controlCondition;
			this.missingNodeRelIndex = missingNodeRelIndex;
		}

	}
}
