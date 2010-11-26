package base.hldd.visitors;

import base.hldd.structure.models.utils.ModelManager;
import base.hldd.structure.models.utils.RangeVariableHolder;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.GraphVariable;

/**
 * @author Anton Chepurov
 */
public class DependentVariableReplacer implements HLDDVisitor {
	protected final AbstractVariable variableToReplace;
	private final RangeVariableHolder replacingVarHolder;

	public DependentVariableReplacer(AbstractVariable variableToReplace, RangeVariableHolder replacingVarHolder) {
		this.variableToReplace = variableToReplace;
		this.replacingVarHolder = replacingVarHolder;
	}

	public void visitNode(Node node) throws Exception {
		if (node.getDependentVariable() == variableToReplace) {
			replaceNode(node);
		}

		if (node.isControlNode()) {
			for (Node successor : node.getSuccessors()) {
				successor.traverse(this);
			}
		}
	}

	protected void replaceNode(Node node) {
		node.setDependentVariable(replacingVarHolder.getVariable());
		if (replacingVarHolder.isRange()) {
			//todo: Range.absoluteFor()...
			node.setRange(replacingVarHolder.getRange());
		}
	}

	public void visitGraphVariable(GraphVariable graphVariable) throws Exception {
		graphVariable.getGraph().getRootNode().traverse(this);
	}

	public static class FlattenerToBits extends DependentVariableReplacer {

		private final ModelManager modelManager;

		public FlattenerToBits(AbstractVariable variableToReplace, ModelManager modelManager) {
			super(variableToReplace, null);
			this.modelManager = modelManager;
		}

		@Override
		protected void replaceNode(Node node) {
			node.setDependentVariable(modelManager.generateBitRangeVariable(variableToReplace, node.getRange()));
			node.setRange(null);
		}
	}
}
