package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.GraphVariable;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 15.02.2008
 * <br>Time: 21:54:49
 */
public class DependentVariableReplacer implements HLDDVisitor {
    private final AbstractVariable variableToReplace;
    private final AbstractVariable replacingVariable;

    public DependentVariableReplacer(AbstractVariable variableToReplace, AbstractVariable replacingVariable) {
        this.variableToReplace = variableToReplace;
        this.replacingVariable = replacingVariable;
    }

    public void visitNode(Node node) throws Exception {
        replaceNode(node);
        if (node.isControlNode()) {
            for (Node successor : node.getSuccessors()) {
                successor.traverse(this);
            }
        }
    }

    private void replaceNode(Node node) {
        if (node.getDependentVariable() == variableToReplace) {
            node.setDependentVariable(replacingVariable);
        }
    }

    public void visitGraphVariable(GraphVariable graphVariable) throws Exception {
        graphVariable.getGraph().getRootNode().traverse(this);
    }
}
