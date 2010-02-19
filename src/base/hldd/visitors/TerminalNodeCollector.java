package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Utility;
import base.hldd.structure.variables.GraphVariable;

import java.util.*;

/**
 * <b>NB!</b> Class assumes that the model under traversal has been trimmed.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 15.02.2008
 * <br>Time: 0:21:28
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

        if (node.isTerminalNode()) {
            /* Skip Terminal Node that retains value */
            if (node.getDependentVariable() == currentGraphVariable) {
                valueRetainingNodeUsed.add(currentGraphVariable);
                return;
            }
            if (!terminalNodes.contains(node)) {
                terminalNodes.add(node);
            }
        } else {
            Node[] successors = node.getSuccessors();
            if (node.getDependentVariable().isReset()) {
                /* Traverse the subtree of inactive reset */
                successors[0].traverse(this);
            } else {
                for (Node successor : successors) {
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
                "\nRequested terminal node is not found amongst the list of the GraphVarialble's terminal nodes: " + terminalNode);
    }

    public void fillMuxAddrNode(GraphVariable graphVariable, Node muxAddrNode) throws Exception {
        if (!(muxAddrValueMap.containsKey(graphVariable))) throw new Exception("Todo: "); //todo...

        List<Node> terminalNodes = muxAddrValueMap.get(graphVariable);
        for (int index = 0; index < terminalNodes.size(); index++) {
            muxAddrNode.setSuccessor(index, Utility.clone(terminalNodes.get(index))); /* new Node(terminalNodes.get(index).getDependentVariable()) */
        }
    }
}
