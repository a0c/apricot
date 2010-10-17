package base.vhdl.structure.nodes;

import base.vhdl.visitors.AbstractVisitor;
import base.vhdl.structure.AbstractOperand;
import base.vhdl.processors.AbstractProcessor;

import java.util.List;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class CaseNode extends AbstractNode {

	private AbstractOperand variableOperand;

	private List<WhenNode> conditions;

	public CaseNode(AbstractOperand variableOperand) {
		this.variableOperand = variableOperand;
		conditions = new LinkedList<WhenNode>();
	}

	public void addCondition(WhenNode newCondition) {
		conditions.add(newCondition);
	}

	public AbstractOperand getVariableOperand() {
		return variableOperand;
	}

	public List<WhenNode> getConditions() {
		return conditions;
	}

	public void traverse(AbstractVisitor visitor) throws Exception {
		visitor.visitCaseNode(this);
	}

	public void process(AbstractProcessor processor) {
		processor.processCaseNode(this);
	}

	public boolean isIdenticalTo(AbstractNode comparedNode) {
		return false;  //todo: To change body of implemented methods use File | Settings | File Templates.
	}

	public boolean isRedundantNode() {
		/* Check the number of When conditions */
		if (conditions.isEmpty()) return true;

		/* Check if all when nodes are identical */
		WhenNode firstWhenNode = conditions.get(0);
		for (WhenNode whenNode : conditions) {
			if (!firstWhenNode.isIdenticalTo(whenNode)) return false;
		}

		/* All tests passed. Case node is redundant. */
		return true;
	}

}
