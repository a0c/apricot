package base.hldd.visitors;

import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.GraphVariable;

import java.util.HashMap;

/**
 * @author Anton Chepurov
 */
public class ResetInspector implements HLDDVisitor {
	/* RESET mapping, where:
	* key is GraphVariable and value is resetting node variable */
	private HashMap<GraphVariable, AbstractVariable> resetMap;

	private GraphVariable graphVariable;
	private boolean doVisit;

	public ResetInspector() {
		resetMap = new HashMap<GraphVariable, AbstractVariable>();
	}

	public void visitNode(Node node) throws Exception {
		if (!doVisit) return;

		if (node.isControlNode()) {
			if (node.getDependentVariable().isReset()) {
				resetMap.put(graphVariable, node.getSuccessor(Condition.TRUE).getDependentVariable());
				doVisit = false;
				return;
			}
			for (Node successor : node.getSuccessors()) {
				successor.traverse(this);
			}
		}

	}

	public void visitGraphVariable(GraphVariable graphVariable) throws Exception {
		this.graphVariable = graphVariable;
		doVisit = true;
		graphVariable.getGraph().getRootNode().traverse(this);
	}

	public boolean isResettableVariable(GraphVariable graphVariable) {
		return resetMap.containsKey(graphVariable);
	}

	public AbstractVariable getResettingVariable(GraphVariable graphVariable) throws Exception {
		if (resetMap.containsKey(graphVariable)) {
			return resetMap.get(graphVariable);
		} else throw new Exception("The specified GraphVariable (" + graphVariable.getName() + ") is not resetable.");
	}

}
