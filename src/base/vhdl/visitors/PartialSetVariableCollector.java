package base.vhdl.visitors;

import base.Type;
import base.hldd.structure.models.utils.ModelManager;
import base.hldd.structure.variables.PartedVariable;
import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.WhenNode;
import base.vhdl.structure.utils.OperandStorage;
import base.vhdl.structure.utils.SplittableOperandStorage;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author Anton Chepurov
 */
public class PartialSetVariableCollector extends AbstractVisitor {

	private final SplittableOperandStorage rangeAssignments = new SplittableOperandStorage();

	private final ModelManager modelManager;

	public PartialSetVariableCollector(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public void visitEntity(Entity entity) throws Exception {
	}

	public void visitArchitecture(Architecture architecture) throws Exception {

		architecture.getTransitions().traverse(this);

		for (ComponentInstantiation component : architecture.getComponents()) {
			for (OperandImpl rangeOutputActual : component.findPartedOutputActuals()) {
				rangeAssignments.store(rangeOutputActual);
			}
		}
	}

	public void visitProcess(Process process) throws Exception {
		process.getRootNode().traverse(this);
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
		if (varOperand.isParted() || varOperand.isDynamicRange()) {
			rangeAssignments.store(varOperand);
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

	public OperandStorage getRangeAssignments() throws Exception {

		rangeAssignments.splitOverlaps(modelManager);

		splitSignals();

		return rangeAssignments;
	}

	private void splitSignals() throws Exception {

		for (OperandStorage.Item item : rangeAssignments.getItems()) {

			String varName = item.name;
			Set<OperandImpl> rangeAssignments = item.operands;

			/* split */
			Type varType = modelManager.resolveType(varName);

			for (OperandImpl rangeAssignment : rangeAssignments) {

				PartedVariable rangeVariable = new PartedVariable(varName, varType, rangeAssignment.getPartedIndices());

				modelManager.addVariable(rangeVariable);

			}

			modelManager.concatenateRangeAssignments(varName, new ArrayList<OperandImpl>(rangeAssignments));
		}
	}
}
