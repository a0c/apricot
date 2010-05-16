package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.GraphVariable;
import base.hldd.structure.Graph;

/**
 * Visitor removes obsolete resets, i.e. those resets
 * that actually retain the value of the register
 * instead of resetting it.
 * <p>
 * <b>NB!</b> Visitor should only traverse an <i>
 * unindexed</i> tree. Otherwise reindexation is
 * needed, since the old indices become invalid.
 * For indexed tree an Exception is thrown. 
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 09.10.2008
 * <br>Time: 14:19:30
 */
public class ObsoleteResetRemoverImpl implements HLDDVisitor {
    private final GraphVariable currentGraphVariable;
    private boolean doRemove = true;

    public ObsoleteResetRemoverImpl(GraphVariable currentGraphVariable) {
        this.currentGraphVariable = currentGraphVariable;
    }

    public void visitGraphVariable(GraphVariable graphVariable) throws Exception {}

    public void visitNode(Node node) throws Exception {
        while (doRemove) {
            /* Only process unindexed tree*/
            if (node.getRelativeIndex() != -1)
                throw new Exception("Indexed tree is being traversed with " + ObsoleteResetRemoverImpl.class.getSimpleName() +
                        "\nThis visitor can only traverse unindixed tree," +
                        "\nsince after removal of resets old indices become " +
                        "\ninvalid.");
            /* Only process the 1st control node */
            if (node.isControlNode()) {
                if (node.getDependentVariable().isReset()) {
                    /* If resetting value actually retains the value of GraphVariable,
                     * then it's a redundant RESETTING control node. */
                    if (node.getSuccessor(Condition.TRUE).getDependentVariable() == currentGraphVariable) {
                        /* Set new rootNode */
                        currentGraphVariable.setGraph(new Graph(node.getSuccessor(Condition.FALSE)));
                        /* Check the new rootNode as well */
                        node = currentGraphVariable.getGraph().getRootNode();
                        doRemove = true;
                        continue;
                    }
                }
            }
            doRemove = false;
        }
    }
}
