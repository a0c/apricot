package base.hldd.visitors;

import base.SourceLocation;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.GraphVariable;

import java.util.Set;
import java.util.HashSet;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 10.04.2009
 * <br>Time: 19:10:06
 */
public class SourceLocationCollector implements HLDDVisitor {
    private Set<Node> visitedNodes;
    private int currentGraphIndex;

    private StringBuilder sourceBuilder = new StringBuilder();

	public void visitNode(Node node) throws Exception {
        if (!visitedNodes.contains(node)) {
            /* Mark as visited */
            visitedNodes.add(node);
            if (node.isTerminalNode()) {
				SourceLocation source = node.getSource();
                if (source != null) {
					sourceBuilder.append(currentGraphIndex).append(" ").append(node.getRelativeIndex());
                    sourceBuilder.append(": ").append(source.toString()).append("\n");
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

    public String getSourceAsString() {
        return sourceBuilder.toString();
    }
}
