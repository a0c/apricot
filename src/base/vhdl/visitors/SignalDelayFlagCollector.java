package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.WhenNode;
import base.vhdl.structure.utils.OperandStorage;
import ui.ConfigurationHandler;

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
	 * Collection of {@link base.vhdl.structure.OperandImpl}-s with activated D-flag
	 */
	private final OperandStorage dFlagOperands = new OperandStorage();
	/**
	 * Collection of names of {@link base.vhdl.structure.Signal}-s from processed {@link base.vhdl.structure.Architecture}
	 */
	private final Collection<String> pendingSignals = new HashSet<String>();

	private Architecture currentArchitecture;

	private final ConfigurationHandler config;

	private final ClockEventRemover clockEventRemover;

	public SignalDelayFlagCollector(ConfigurationHandler config) {
		this.config = config;
		clockEventRemover = new ClockEventRemover(config);
	}

	public OperandStorage getDFlagOperands() {
		return dFlagOperands;
	}

	public void visitEntity(Entity entity) throws Exception {
		/* Collect OUTPUT ports as Signal names */
		for (Port port : entity.getPorts()) {
			if (port.isOutput()) {
				pendingSignals.add(port.getName());
			}
		}
	}

	public void visitArchitecture(Architecture architecture) throws Exception {
		currentArchitecture = architecture;
		/* Collect Signal names */
		for (Signal signal : architecture.getSignals()) {
			pendingSignals.add(signal.getName());
		}
	}

	public void visitProcess(base.vhdl.structure.Process process) throws Exception {
		/* If architecture declares no signals or all the signals are processed, skip processes */
		if (pendingSignals.isEmpty()) return;
		/* Only check processes that have the Clocking signal in their sensitivity list */
		if (!hasClockInSensitivityList(process.getSensitivityList())) return;
		/* Traverse the tree */
		isInsideClock = false;
		process.getRootNode().traverse(this);
	}

	public void visitIfNode(IfNode ifNode) throws Exception {
		/* Exit when all Signals have been processed */
		if (pendingSignals.isEmpty()) return;

		/* Record the state of being inside clock */
		boolean wasInsideClock = isInsideClock;

		/* Process CONDITION */
		if (!isInsideClock && clockEventRemover.isClockingExpression(ifNode.getConditionExpression())) {
			isInsideClock = true;
		}

		/* Process TRUE part */
		ifNode.getTruePart().traverse(this);

		/* Exit when all Signals have been processed */
		if (pendingSignals.isEmpty()) return;

		/* Restore the state of being inside clock */
		isInsideClock = wasInsideClock;

		/* Process FALSE part */
		if (ifNode.getFalsePart() != null) {
			ifNode.getFalsePart().traverse(this);
		}

	}

	public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
		/* Exit when all Signals have been processed */
		if (pendingSignals.isEmpty()) return;
		if (isInsideClock) {
			if (transitionNode.isNull()) return;
			OperandImpl targetOperand = transitionNode.getTargetOperand();
			if (pendingSignals.contains(targetOperand.getName())) {
				/* Collect this operand */
				dFlagOperands.store(targetOperand);
				/* Remove this operand from varNames, to speed up the traversal */
				updatePendingSignals(targetOperand);
			}
		}
	}

	public void visitCaseNode(CaseNode caseNode) throws Exception {
		/* Exit when all Signals have been processed */
		if (pendingSignals.isEmpty()) return;
		for (WhenNode whenNode : caseNode.getConditions()) {
			whenNode.traverse(this);
			/* Exit when all Signals have been processed */
			if (pendingSignals.isEmpty()) return;
		}
	}

	public void visitWhenNode(WhenNode whenNode) throws Exception {
		whenNode.getTransitions().traverse(this);
	}

	private boolean hasClockInSensitivityList(Collection<String> sensitivityList) {
		for (String signalName : sensitivityList) {
			if (config.isClockName(signalName)) return true;
		}
		return false;
	}

	private void updatePendingSignals(OperandImpl dFlagOperand) {
		String signalName = dFlagOperand.getName();
		if (dFlagOperand.isWhole()) {
			pendingSignals.remove(signalName);
		} else {
			/* check the whole range to be set */
			if (dFlagOperands.isWholeRangeSet(signalName, currentArchitecture)) {
				pendingSignals.remove(signalName);
			}
		}
	}

}
