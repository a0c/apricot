package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.WhenNode;
import base.Indices;

import java.util.*;

/**
 * @author Anton Chepurov
 */
public class PartialSetVariableCollector extends AbstractVisitor {

	private Map<String, Set<OperandImpl>> partialSettingsMap;

	private final Map<base.vhdl.structure.Process, Map<String, Set<OperandImpl>>> partialAssignmentsByProcess = new HashMap<Process, Map<String, Set<OperandImpl>>>();

	private Map.Entry<Architecture, Map<String, Set<OperandImpl>>> concurrentPartialAssignments = null;

	public void visitEntity(Entity entity) throws Exception {
	}

	public void visitArchitecture(Architecture architecture) throws Exception {

		clearSettings();

		architecture.getTransitions().traverse(this);

		if (hasPartialSettings()) {

			concurrentPartialAssignments = new AbstractMap.SimpleImmutableEntry<Architecture, Map<String, Set<OperandImpl>>>
					(architecture, getPartialSettings());
		}

	}

	public void visitProcess(Process process) throws Exception {

		clearSettings();

		process.getRootNode().traverse(this);

		if (hasPartialSettings()) {

			partialAssignmentsByProcess.put(process, getPartialSettings());
		}
	}

	private void clearSettings() {
		partialSettingsMap = new HashMap<String, Set<OperandImpl>>();
	}

	private boolean hasPartialSettings() {
		return !partialSettingsMap.isEmpty();
	}

	public void visitIfNode(IfNode ifNode) throws Exception {
		ifNode.getTruePart().traverse(this);
		if (ifNode.getFalsePart() != null) {
			ifNode.getFalsePart().traverse(this);
		}
	}

	public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
		if (transitionNode.isNull()) return;
		OperandImpl varOperand = transitionNode.getTargetOperand();
		if (varOperand.isParted()) {
			/* Get the set of parted variables */
			Set<OperandImpl> partedVarSet;
			/* Check that variable is already mapped and map it otherwise */
			String varOperandName = varOperand.getName();
			if (partialSettingsMap.containsKey(varOperandName)) {
				partedVarSet = partialSettingsMap.get(varOperandName);
			} else {
				partedVarSet = new HashSet<OperandImpl>();
				partialSettingsMap.put(varOperandName, partedVarSet);
			}
			/* Add operand to set */
			partedVarSet.add(varOperand);
		}
	}

	public void visitCaseNode(CaseNode caseNode) throws Exception {
		for (WhenNode whenNode : caseNode.getConditions()) {
			whenNode.traverse(this);
		}
	}

	public void visitWhenNode(WhenNode whenNode) throws Exception {
		whenNode.getTransitions().traverse(this);
	}

	private Map<String, Set<OperandImpl>> getPartialSettings() {
		doDisinterlapPartialSettings();
		return partialSettingsMap;
	}

	/**
	 * Splits partial settings into non-interlaping regions
	 */
	private void doDisinterlapPartialSettings() {

		/* For each parted variable... */
		for (String varName : partialSettingsMap.keySet()) {
			/* create a new set of parted variable operands... */
			HashSet<OperandImpl> newPartedVarSet = new HashSet<OperandImpl>();
			Set<OperandImpl> oldPartedVarSet = partialSettingsMap.get(varName);
			/* and fill it with non-intersecting parted variable operands. */

			/* Use 2 SortedSets: a separate one for START and END markers.
			* 1) Place START and END markers into 2 SortedSets according to the following:
			*      a) Starting index -> "i" for START and "i - 1" for END
			*      b) Ending index   -> "i" for END and "i + 1" for START
			* 2) Create intervals: take next marker from either SortedSet. Number of markers in both sets is equal.
			* */
			SortedSet<Integer> startsSet = new TreeSet<Integer>();
			SortedSet<Integer> endsSet = new TreeSet<Integer>();
			int lowerBound = getLowest(oldPartedVarSet);
			int upperBound = getHighest(oldPartedVarSet);
			/* Fill SortedSets */
			for (OperandImpl partialSetOperand : oldPartedVarSet) {
				Indices partedIndices = partialSetOperand.getPartedIndices();
				/* Starting index */
				int start = partedIndices.getLowest();
				startsSet.add(start);
				if (start - 1 >= lowerBound) {
					endsSet.add(start - 1);
				}
				/* Ending index */
				int end = partedIndices.getHighest();
				endsSet.add(end);
				if (end + 1 <= upperBound) {
					startsSet.add(end + 1);
				}
			}
			/* Check number of markers */
			if (startsSet.size() != endsSet.size()) throw new RuntimeException("Unexpected bug occurred while " +
					"extracting non-intersecting regions for partial setting variables:" +
					"\n Amounts of START and END markers are different.");
			/* Create intervals */
			Integer[] startsArray = new Integer[startsSet.size()];
			startsSet.toArray(startsArray);
			Integer[] endsArray = new Integer[endsSet.size()];
			endsSet.toArray(endsArray);
			for (int i = 0; i < startsArray.length; i++) {
				newPartedVarSet.add(new OperandImpl(varName, new Indices(endsArray[i], startsArray[i]), false));
			}

			/* Replace the variable set in partialSettingsMap with the new one */
			partialSettingsMap.put(varName, newPartedVarSet);
		}

	}

	public boolean hasPartialAssignmentsIn(Process process) {
		return partialAssignmentsByProcess.containsKey(process);
	}

	public Map<String, Set<OperandImpl>> getPartialAssignmentsFor(Object astObject) {

		if (astObject instanceof Process) {

			Process process = (Process) astObject;

			if (partialAssignmentsByProcess.containsKey(process)) {

				return partialAssignmentsByProcess.get(process);

			}

		} else if (astObject instanceof Architecture) {

			Architecture architecture = (Architecture) astObject;

			if (concurrentPartialAssignments != null && concurrentPartialAssignments.getKey() == architecture) {

				return concurrentPartialAssignments.getValue();

			}

		}

		return Collections.emptyMap();
	}

	private static int getHighest(Set<OperandImpl> operands) {
		int max = -1;
		for (OperandImpl operand : operands) {
			int highest = operand.getPartedIndices().getHighest();
			if (highest > max) {
				max = highest;
			}
		}
		if (max == -1) {
			throw new RuntimeException("Possibly failed to calculate upperBound while disinterlaping partial settings" +
					": Upper bound = -1");
		}
		return max;
	}

	private static int getLowest(Set<OperandImpl> operands) {
		int min = Integer.MAX_VALUE;
		for (OperandImpl operand : operands) {
			int lowest = operand.getPartedIndices().getLowest();
			if (lowest < min) {
				min = lowest;
			}
		}
		if (min == Integer.MAX_VALUE) {
			throw new RuntimeException("Possibly failed to calculate lowerBound while disinterlaping partial settings" +
					": Lower bound = Integer.MAX_VALUE");
		}
		return min;
	}
}
