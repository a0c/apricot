package base.hldd.structure.nodes.fsm;

/**
 * @author Anton Chepurov
 */
public class Transitions {
	private Integer[] transitions;

	public Transitions(int transitionsCount) {
		transitions = new Integer[transitionsCount];
	}

	public void insertTransition(Integer transitionIndex, int transitionValue) throws Exception {
		if (transitionIndex > transitions.length - 1) {
			throw new Exception(""); //todo:
		}
		transitions[transitionIndex] = transitionValue;
	}

	public boolean isIdenticalTo(Transitions transitions) {
		/* Compare NUMBER of transitions */
		if (this.transitions.length != transitions.transitions.length) return false;

		for (int index = 0; index < this.transitions.length; index++) {
			Integer transition = this.transitions[index];
			Integer comparedTransition = transitions.transitions[index];
			/* If exactly one of transitions is NULL */
			if ((transition == null) ^ (comparedTransition == null)) return false;
			/* Here both are either NULL, or instantiated */
			if (transition != null) {
				/* Compare values */
				if (!transition.equals(comparedTransition)) return false;
			}
		}

		return true;
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer("\"");

		for (int index = 0; index < transitions.length; index++) {
			Integer transition = transitions[index];
			strBuf.append(transition == null ? "X" : transition);
			strBuf.append(index == 0 ? "  " : " ");
		}

		strBuf.deleteCharAt(strBuf.length() - 1);
		strBuf.append("\"");

		return strBuf.toString();
	}

	public Transitions clone() {
		Transitions newTransitions = new Transitions(transitions.length);
		for (int index = 0; index < transitions.length; index++) {
			try {
				newTransitions.insertTransition(index, transitions[index]);
			} catch (Exception e) { /* Do nothing. There won't be any errors for sure. */ }
		}
		return newTransitions;
	}
}
