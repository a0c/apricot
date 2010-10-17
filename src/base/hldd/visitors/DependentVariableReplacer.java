package base.hldd.visitors;

import base.hldd.structure.models.utils.PartedVariableHolder;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.GraphVariable;

/**
 * @author Anton Chepurov
 */
public class DependentVariableReplacer implements HLDDVisitor {
	private final AbstractVariable variableToReplace;
	private final PartedVariableHolder replacingVarHolder;

	public DependentVariableReplacer(AbstractVariable variableToReplace, PartedVariableHolder replacingVarHolder) {
		this.variableToReplace = variableToReplace;
		this.replacingVarHolder = replacingVarHolder;
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
			node.setDependentVariable(replacingVarHolder.getVariable());
			if (replacingVarHolder.isParted()) {
				//todo: Indices.absoluteFor()...
				node.setPartedIndices(replacingVarHolder.getPartedIndices());
			}
		}
	}

	public void visitGraphVariable(GraphVariable graphVariable) throws Exception {
		graphVariable.getGraph().getRootNode().traverse(this);
	}
}
