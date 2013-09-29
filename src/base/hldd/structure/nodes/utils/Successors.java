package base.hldd.structure.nodes.utils;

import base.HLDDException;
import base.SourceLocation;
import base.hldd.structure.nodes.Node;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Anton Chepurov
 */
public class Successors {

	private static final Logger LOGGER = Logger.getLogger(Successors.class.getName());

	private TreeMap<Condition, Node> successorByCondition = new TreeMap<Condition, Node>();
	/**
	 * Number of possible values of conditions (cardinality of the set of values).
	 * Used when calculating OTHERS' values.
	 */
	private int conditionValuesCount;


	public Successors(int conditionValuesCount) {
		this.conditionValuesCount = conditionValuesCount;
	}

	/**
	 * <b>NB!</b> If specified condition is a part of some complex condition available in this <tt>Successors</tt> object,
	 * <tt>null</tt> will be returned. To look inside complex conditions use {@link #getSuccessorInternal(Condition)}.
	 *
	 * @param condition exact condition to look for.
	 * @return successor node where the specified condition leads to
	 */
	public Node getSuccessor(Condition condition) {

		if (!hasExactCondition(condition)) {
			LOGGER.fine("Trying to obtain successor for non-existent condition (" + condition + ") from " + this);
			return null;
		}

		return successorByCondition.get(condition);
	}

	public Node getSuccessorInternal(Condition condition) throws HLDDException {
		if (condition.isArray()) {
			throw new HLDDException("Successors: getSuccessorInternal(): array-condition specified as a parameter("
					+ condition + "). Only scalar-conditions are allowed for fine-requests.");
		}
		// try looking for direct mapping
		if (hasExactCondition(condition)) {
			return getSuccessor(condition);
		}
		// try looking inside complex conditions
		Map.Entry<Condition, Node> entry = getInternalMapping(condition);
		return entry == null ? null : entry.getValue();
	}

	private Map.Entry<Condition, Node> getInternalMapping(Condition condition) {
		for (Map.Entry<Condition, Node> entry : successorByCondition.entrySet()) {
			Condition cond = entry.getKey();

			if (cond.contains(condition)) {
				return entry;
			}
		}
		return null;
	}

	public void setSuccessor(Condition condition, Node successor) {
		successorByCondition.put(condition, successor);
	}

	public int getConditionsCount() {
		return successorByCondition.keySet().size();
	}

	public Condition getCondition(int idx) throws HLDDException {
		ArrayList<Condition> conditionsList = new ArrayList<Condition>(successorByCondition.keySet());
		Condition.checkBoundaries(idx, 0, conditionsList.size() - 1, "Successors: getCondition(): condition index out of bounds: ");
		return conditionsList.get(idx);
	}

	public int getConditionValuesCount() {
		return conditionValuesCount;
	}

	public Condition getOthers() throws HLDDException {
		return Condition.createCondition(successorByCondition.keySet(), conditionValuesCount, false);
	}

	@Override
	public String toString() {

		// if there are only 2 successors - separate with TABS; if more than 2 - separate with SPACES:
		String delim = conditionValuesCount == 2 ? "\t" : " ";

		StringBuilder builder = new StringBuilder("(");

		for (Map.Entry<Condition, Node> entry : successorByCondition.entrySet()) {
			Condition condition = entry.getKey();
			Node successor = entry.getValue();

			builder.append(condition.toString());
			builder.append("=>");
			builder.append(successor == null ? "null" : successor.getRelativeIndex());
			builder.append(delim);

		}

		return builder.append(")").toString();
	}

	public boolean isEmpty() {
		for (Node successor : successorByCondition.values()) {
			if (successor != null) return false;
		}
		return true;
	}

	public void fillEmptyWith(Node fillingNode, SourceLocation source, boolean isF4RTL) {
		for (Map.Entry<Condition, Node> entry : successorByCondition.entrySet()) {
			Condition condition = entry.getKey();
			Node successor = entry.getValue();

			if (successor == null) {
				/* Fill missing successor with a copy of filling node, to be unique */
				Node copy = Node.clone(fillingNode);
				setSuccessor(condition, copy); // #### COPY OF FILLING_NODE!!! ###
				/* If the fillingNode is artificially created and doesn't have corresponding VHDL Lines
				* (i.e. node is obtained from
				* {@link base.vhdl.visitors.GraphGenerator.ContextManager#getDefaultValueNode()}), then copy lines from
				* Control Node being filled. It means that ControlNode is not fully covered when calculating coverage. */
				if (fillingNode.getSource() == null) {
					copy.setSource(source);
				}
			} else if (successor.isControlNode()) {
				/* Fill recursively every non-empty successor */
				successor.fillEmptySuccessorsWith(fillingNode, isF4RTL);
			}
		}
	}

	private boolean hasExactCondition(Condition condition) {
		return successorByCondition.containsKey(condition);
	}

	/**
	 * Compacts conditions. Merges conditions with the same successor into a single condition.
	 *
	 * @throws base.HLDDException
	 */
	public void compact() throws HLDDException {
		Node mergedSuccessor = null;
		LinkedList<Condition> mergedConditions = new LinkedList<Condition>();
		HashSet<Node> processedSuccessors = new HashSet<Node>();

		while (true) {
			/* Collect next set of Conditions to merge (those that link to the same successor) */
			for (Map.Entry<Condition, Node> entry : successorByCondition.entrySet()) {
				Condition condition = entry.getKey();
				Node successor = entry.getValue();

				if (processedSuccessors.contains(successor)) continue;

				if (mergedSuccessor == null) { // set new successor to track
					mergedSuccessor = successor;
					mergedConditions = new LinkedList<Condition>();
				}

				if (successor == mergedSuccessor) { // collect condition
					mergedConditions.add(condition);
				}
			}
			/* All successors have been processed. Task done. */
			if (mergedSuccessor == null) break;

			/* Finally, merge the conditions */
			if (mergedConditions.size() > 1) { // only merge conditions if there are more than 1

				/* Remove all conditions that will be merged */
				for (Condition mergedCondition : mergedConditions) {
					successorByCondition.remove(mergedCondition);
				}
				/* Create new Condition object... */
				Condition mergedCondition = Condition.createCondition(mergedConditions, conditionValuesCount, true);
				/* ... and save it */
				setSuccessor(mergedCondition, mergedSuccessor);

			}
			processedSuccessors.add(mergedSuccessor);
			mergedSuccessor = null;
		}
	}

	/**
	 * @param condition whose holder (complex condition) is to be decompacted
	 * @throws base.HLDDException if array-condition is specified as a parameter, or
	 *                            if this object doesn't contain the specified condition
	 */
	public void decompact(Condition condition) throws HLDDException {
		if (condition.isArray()) {
			throw new HLDDException("Successors: decompact(): scalar-condition expected, found: " + condition);
		}
		// skip scalar conditions
		if (hasExactCondition(condition)) {
			return; // do nothing
		}

		Map.Entry<Condition, Node> entry = getInternalMapping(condition);
		if (entry == null) {
			throw new HLDDException("Successors: decompact(): non-existent condition (" + condition + ") received as a parameter in " + this);
		}
		Condition cond = entry.getKey();
		Node successor = entry.getValue();

		successorByCondition.remove(cond);

		for (Condition scalarCondition : cond.asList()) {
			setSuccessor(scalarCondition, Node.clone(successor));
		}

	}

	public void cloneFrom(Successors sourceSuccessors) {
		conditionValuesCount = sourceSuccessors.conditionValuesCount;
		for (Map.Entry<Condition, Node> entry : sourceSuccessors.successorByCondition.entrySet()) {
			setSuccessor(entry.getKey(), Node.clone(entry.getValue()));
		}
	}

	public Collection<Node> asCollection() {
		return successorByCondition.values();
	}

	public boolean isIdenticalTo(Successors comparedSuccessors) {

		if (conditionValuesCount != comparedSuccessors.conditionValuesCount) return false;

		Set<Map.Entry<Condition, Node>> entries = successorByCondition.entrySet();
		Set<Map.Entry<Condition, Node>> compEntries = comparedSuccessors.successorByCondition.entrySet();

		if (entries.size() != compEntries.size()) return false;

		for (Iterator<Map.Entry<Condition, Node>> entryIt = entries.iterator(), compEntryIt = compEntries.iterator(); entryIt.hasNext();) {
			Map.Entry<Condition, Node> entry = entryIt.next();
			Map.Entry<Condition, Node> compEntry = compEntryIt.next();
			if (!entry.getKey().equals(compEntry.getKey())) return false; //todo: if the same successor is allocated to multiple conditions, then successors may still be identical. size check may already fail in this case.
			if (!entry.getValue().isIdenticalTo(compEntry.getValue())) return false;
		}

		return true;
	}


	public boolean hasIdenticalConditionsWith(Successors second) {

		Set<Condition> conditions1 = successorByCondition.keySet();
		Set<Condition> conditions2 = second.successorByCondition.keySet();

		return conditions1.size() == conditions2.size() && conditions1.containsAll(conditions2) && conditions2.containsAll(conditions1);

	}
}
