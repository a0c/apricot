package base.hldd.visitors;

import base.HLDDException;
import base.hldd.structure.models.utils.PartedVariableHolder;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.FunctionVariable;
import base.hldd.structure.variables.GraphVariable;

import java.util.HashSet;

/**
 * Collects functions used by graphs.
 *
 * @author Anton Chepurov
 */
public class UsedFunctionsCollectorImpl implements HLDDVisitor {
	HashSet<FunctionVariable> usedFunctions = new HashSet<FunctionVariable>();

	@Override
	public void visitNode(Node node) throws Exception {

		addAndRecur(node.getDependentVariable());

		if (node.isControlNode()) {
			for (Node successor : node.getSuccessors()) {
				visitNode(successor);
			}
		}
	}

	private void addAndRecur(AbstractVariable depVar) {
		if (depVar instanceof FunctionVariable) {
			FunctionVariable funVar = (FunctionVariable) depVar;

			usedFunctions.add(funVar);

			for (PartedVariableHolder operand : funVar.getOperands()) {
				addAndRecur(operand.getVariable());
			}
		}
	}

	@Override
	public void visitGraphVariable(GraphVariable graphVariable) throws Exception {
		Node rootNode = graphVariable.getGraph().getRootNode();
		if (graphVariable.isExpansion()) {
			// speedup
			visitConditionalGraph(rootNode);
		} else {
			rootNode.traverse(this);
		}
	}

	private void visitConditionalGraph(Node node) throws HLDDException {
		if (node.isControlNode()) {
			addAndRecur(node.getDependentVariable());

			Node firstSuccessor = node.getSuccessor(node.getCondition(0));
			visitConditionalGraph(firstSuccessor);
		}
	}

	public boolean isUsed(FunctionVariable functionVariable) {
		return usedFunctions.contains(functionVariable);
	}
}
