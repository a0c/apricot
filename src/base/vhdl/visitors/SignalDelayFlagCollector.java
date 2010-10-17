package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.WhenNode;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class searches for clocked processes using their sensitivity lists.
 * If a clocked process is found, then all the signals set below the clocking
 * signal are marked with Delay flag.
 *
 * @author Anton Chepurov
 */
public class SignalDelayFlagCollector extends AbstractVisitor {
	private boolean isInsideClock;
	/**
	 * Collection of names of {@link base.vhdl.structure.Signal}-s with activated D-flag
	 */
	private final Collection<String> dFlagNames = new HashSet<String>();
	/**
	 * Collection of names of {@link base.vhdl.structure.Signal}-s from processed {@link base.vhdl.structure.Architecture}
	 */
	private final Collection<String> sigNames = new HashSet<String>();

	public Collection<String> getDFlagNames() {
		return dFlagNames;
	}

	public void visitEntity(Entity entity) throws Exception {
		/* Collect OUTPUT ports as Signal names */
		for (Port port : entity.getPorts()) {
			if (port.isOutput()) {
				sigNames.add(port.getName());
			}
		}
	}

	public void visitArchitecture(Architecture architecture) throws Exception {
		/* Collect Signal names */
		for (Signal signal : architecture.getSignals()) {
			sigNames.add(signal.getName());
		}

	}

	public void visitProcess(base.vhdl.structure.Process process) throws Exception {
		/* If architecture declares no signals or all the signals are processed, skip processes */
		if (sigNames.isEmpty()) return;
		/* Only check processes that have the Clocking signal in their sensitivity list */
		if (!hasClockInSensitivityList(process.getSensitivityList())) return;
		/* Traverse the tree */
		isInsideClock = false;
		process.getRootNode().traverse(this);
	}

	public void visitIfNode(IfNode ifNode) throws Exception {
		/* Exit when all Signals have been processed */
		if (sigNames.isEmpty()) return;

		/* Record the state of being inside clock */
		boolean wasInsideClock = isInsideClock;

		/* Process CONDITION */
		if (ClockEventRemover.isClockingExpression(ifNode.getConditionExpression())) {
			isInsideClock = true;
		}

		/* Process TRUE part */
		ifNode.getTruePart().traverse(this);

		/* Exit when all Signals have been processed */
		if (sigNames.isEmpty()) return;

		/* Restore the state of being inside clock */
		isInsideClock = wasInsideClock;

		/* Process FALSE part */
		if (ifNode.getFalsePart() != null) {
			ifNode.getFalsePart().traverse(this);
		}

	}

	public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
		/* Exit when all Signals have been processed */
		if (sigNames.isEmpty()) return;
		if (isInsideClock) {
			if (transitionNode.isNull()) return;
			String operandName = transitionNode.getTargetOperand().getName();
			if (sigNames.contains(operandName)) {
				/* Collect this operand */
				dFlagNames.add(operandName);
				/* Remove this operand from varNames, to speed up the traversal */
				sigNames.remove(operandName);
			}
		}
	}

	public void visitCaseNode(CaseNode caseNode) throws Exception {
		/* Exit when all Signals have been processed */
		if (sigNames.isEmpty()) return;
		for (WhenNode whenNode : caseNode.getConditions()) {
			whenNode.traverse(this);
			/* Exit when all Signals have been processed */
			if (sigNames.isEmpty()) return;
		}
	}

	public void visitWhenNode(WhenNode whenNode) throws Exception {
		whenNode.getTransitions().traverse(this);
	}

	private boolean hasClockInSensitivityList(Collection<String> sensitivityList) {
		for (String signalName : sensitivityList) {
			if (GraphGenerator.isClockName(signalName)) return true;
		}
		return false;
	}

}
