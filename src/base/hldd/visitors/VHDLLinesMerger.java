package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.GraphVariable;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 13.04.2009
 * <br>Time: 17:08:48
 */
public class VHDLLinesMerger implements HLDDVisitor {
    private Node secondNode;

    public VHDLLinesMerger(Node secondNode) {
        this.secondNode = secondNode;
    }

    public void visitNode(Node node) {
        /* Add VHDL lines to node */        
        node.addVhdlLines(secondNode.getVhdlLines());
        /* For ControlNodes, merge the subtree as well */
        if (node.isControlNode()) {
            Node[] successors = node.getSuccessors();
            Node[] secondSuccessors = secondNode.getSuccessors();
            for (int i = 0; i < successors.length; i++) {
                /* Update secondNode */
                secondNode = secondSuccessors[i];
                /* Traverse successor (secondNode is now updated) */
                visitNode(successors[i]);
            }
        }
    }

    public void visitGraphVariable(GraphVariable graphVariable) throws Exception {}
}
