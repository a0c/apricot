package base.hldd.structure.nodes;

import base.hldd.structure.nodes.fsm.Transitions;

/**
 * @author Anton Chepurov
 */
public class FSMNode extends Node {

	/**
	 * Transitions that consist of STATE transitions and CONTROL PART OUTPUTS transitions
	 */
	private Transitions transitions;

	public FSMNode(Transitions transitions) {
		this.transitions = transitions;
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append("  ").
				append(getAbsoluteIndex()).
				append("\t").
				append(getRelativeIndex()).
				append(":  (__v_) (\t0\t0)\tVEC = ").
				append(transitions);

		return strBuf.toString();
	}

	public FSMNode clone() {
		return new FSMNode(transitions.clone());
	}

	public boolean isTerminalNode() {
		return true;
	}

	public boolean isIdenticalTo(Node comparedNode) {
		if (!(comparedNode instanceof FSMNode)) return false;
		/* Compare Transitions */
		return transitions.isIdenticalTo(((FSMNode) comparedNode).getTransitions());
	}

	public Transitions getTransitions() {
		return transitions;
	}
}
