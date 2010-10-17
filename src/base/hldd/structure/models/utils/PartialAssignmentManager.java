package base.hldd.structure.models.utils;

import base.Indices;
import base.Type;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.PartedVariable;
import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.visitors.PartialSetVariableCollector;

import java.util.*;

/**
 * @author Anton Chepurov
 */
class PartialAssignmentManager {

	private Map<base.vhdl.structure.Process, Map<String, Set<OperandImpl>>> partialAssignmentsByProcess = new HashMap<Process, Map<String, Set<OperandImpl>>>();

	private final ModelManager modelManager;

	PartialAssignmentManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

	void collectPartialSettings(Process process) throws Exception {

		PartialSetVariableCollector partAssignCollector = new PartialSetVariableCollector(modelManager);

		process.traverse(partAssignCollector);

		Map<String, Set<OperandImpl>> partialSettingsMap = partAssignCollector.getPartialSettingsMap();
		if (!partialSettingsMap.isEmpty()) {
			partialAssignmentsByProcess.put(process, partialSettingsMap);
		}

	}

	boolean hasPartialAssignmentsIn(Process process) {
		return partialAssignmentsByProcess.containsKey(process);
	}

	Map<String, Set<OperandImpl>> getPartialAssignmentsFor(Process process) {
		return partialAssignmentsByProcess.get(process);
	}

	static void finalizeAndCheckForCompleteness(Set<OperandImpl> partialSets, Indices wholeLength, String varName) throws Exception {
		boolean[] bits = new boolean[wholeLength.length()];
		/* Check intersections: if any, inform about them */
		for (OperandImpl partSetOperand : partialSets) {
			if (!partSetOperand.isParted())
				throw new Exception("Partial setting operand doesn't contain parted indices: " + partSetOperand);
			Indices partedIndices = partSetOperand.getPartedIndices();
			for (int index = partedIndices.getLowest(); index <= partedIndices.getHighest(); index++) {
				/* If this bit has already been set, inform about intersection */
				if (bits[index]) throw new Exception("Intersection of partial setting operands:" +
						"\nBit " + index + " has already been set for operand " + partSetOperand.getName());
				bits[index] = true;
			}
		}
		/* Check missing partial setting variables: if any, fill the set with missing variables */
		Collection<Indices> unsetIndicesCollect = extractUnsetIndices(bits);
		for (Indices unsetIndices : unsetIndicesCollect) { //todo: It may occur, that the whole variable is unset. Consider this.
			partialSets.add(new OperandImpl(varName, unsetIndices, false));
		}
	}

	static Collection<Indices> extractUnsetIndices(boolean[] setBits) {
		List<Indices> indicesList = new LinkedList<Indices>();
		int lowest = -1, highest = -1;
		for (int i = 0; i < setBits.length; i++) {
			if (!setBits[i]) {
				if (lowest == -1) {
					lowest = i;
				}
				highest = i;
			} else {
				if (lowest != -1) {
					indicesList.add(new Indices(highest, lowest));
					lowest = -1;
					highest = -1;
				}
			}
		}
		if (lowest != -1) {
			indicesList.add(new Indices(highest, lowest));
		}
		return indicesList;
	}

	void splitPartiallyAssignedSignals(Set<ComponentInstantiation> components) throws Exception {

		Map<String, Set<OperandImpl>> partialSetsByName = new HashMap<String, Set<OperandImpl>>();
		/* Collect partial sets by name */
		for (ComponentInstantiation component : components) {

			List<OperandImpl> partedActuals = component.findPartedOutputActuals();

			for (OperandImpl partedActual : partedActuals) {

				String name = partedActual.getName();

				Set<OperandImpl> partialSets;
				if (partialSetsByName.containsKey(name)) {

					partialSets = partialSetsByName.get(name);

				} else {

					partialSets = new HashSet<OperandImpl>();

					partialSetsByName.put(name, partialSets);
				}

				partialSets.add(partedActual);
			}
		}
		/* Finalize partial sets and split signals/variables */
		for (Map.Entry<String, Set<OperandImpl>> entry : partialSetsByName.entrySet()) {

			String varName = entry.getKey();
			Set<OperandImpl> partialSets = entry.getValue();

			AbstractVariable wholeVariable = modelManager.getVariable(varName);

			finalizeAndCheckForCompleteness(partialSets, wholeVariable.getLength(), varName);

			/* split */
			split(wholeVariable, partialSets);
		}


	}

	private void split(AbstractVariable wholeVariable, Set<OperandImpl> partialSets) throws Exception {

		final String varName = wholeVariable.getName();
		final Type varType = wholeVariable.getType();

		for (OperandImpl partialAssignment : partialSets) {

			PartedVariable partedVariable = new PartedVariable(varName, varType, partialAssignment.getPartedIndices());

			modelManager.addVariable(partedVariable);

		}

		modelManager.concatenatePartialAssignments(wholeVariable, new ArrayList<OperandImpl>(partialSets), false);

	}

}
