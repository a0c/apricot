package base.vhdl.structure.nodes;

import base.Range;
import base.vhdl.processors.AbstractProcessor;
import base.vhdl.structure.AbstractOperand;
import base.vhdl.structure.OperandImpl;
import base.vhdl.structure.Transition;
import base.vhdl.visitors.AbstractVisitor;

/**
 * @author Anton Chepurov
 */
public class TransitionNode extends AbstractNode {

	private Transition transition;

	public TransitionNode(Transition transition) {
		this.transition = transition;
	}

	public boolean isNull() {
		return transition.isNull();
	}

	public OperandImpl getTargetOperand() {
		return transition.getTargetOperand();
	}

	public AbstractOperand getValueOperand() {
		return transition.getValueOperand();
	}

	/**
	 * @return <code>null</code> if it is a <code>Null-transition</code>
	 *         or if valueOperand is not a range. Range, if the
	 *         valueOperand is a range operand.
	 */
	public Range getValueOperandRange() {
		return isNull() ? null : getValueOperand().getRange();
	}

	public void traverse(AbstractVisitor visitor) throws Exception {
		visitor.visitTransitionNode(this);
	}

	public void process(AbstractProcessor processor) throws Exception {
		processor.processTransitionNode(this);
	}

	public boolean isIdenticalTo(AbstractNode comparedNode) {
		return comparedNode instanceof TransitionNode && transition.isIdenticalTo(((TransitionNode) comparedNode).transition);
	}

	public String toString() {
		return transition.toString();
	}

}
