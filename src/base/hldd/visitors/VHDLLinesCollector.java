package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.GraphVariable;

import java.util.Set;
import java.util.HashSet;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 10.04.2009
 * <br>Time: 19:10:06
 */
public class VHDLLinesCollector implements HLDDVisitor {
    private Set<Node> visitedNodes;
    private int currentGraphIndex;

    private StringBuilder vhdlLinesBuilder = new StringBuilder();

    public void visitNode(Node node) throws Exception {
        if (!visitedNodes.contains(node)) {
            /* Mark as visited */
            visitedNodes.add(node);
            if (node.isTerminalNode()) {
                /* Collect into localStringBuilder and then into main one */
                StringBuilder localCollector = new StringBuilder();
                for (int vhdlLine : node.getVhdlLines()) localCollector.append(vhdlLine).append(", ");
                if (localCollector.length() > 0) {
                    vhdlLinesBuilder.append(currentGraphIndex).append(" ").append(node.getRelativeIndex());
                    vhdlLinesBuilder.append(": ").append(localCollector.substring(0, localCollector.length() - 2));
                    vhdlLinesBuilder.append("\n");
                }
            } else {
                for (Node successor : node.getSuccessors()) {
                    successor.traverse(this);
                }
            }
        }
    }

    public void visitGraphVariable(GraphVariable graphVariable) throws Exception {
        /* Initialize index of graph under processing*/
        currentGraphIndex = graphVariable.getGraph().getIndex();
        /* Clear set of visited nodes */
        visitedNodes = new HashSet<Node>();
        /* Traverse root node */
        graphVariable.getGraph().getRootNode().traverse(this);
    }

    public String getVhdlLinesAsString() {
        return vhdlLinesBuilder.toString();
    }
}
