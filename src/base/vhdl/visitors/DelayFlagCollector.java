package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.WhenNode;
import base.vhdl.structure.utils.OperandStorage;
import ui.ConfigurationHandler;

/**
 * Class collects those {@link Variable}-s and {@link Signal}-s
 * that should have <code>D-flag</code> set.
 * <br>For a given {@link Variable}, flag is set if the Variable:
 * <br>1) is read without being set before.
 * <br>For a given {@link Signal}, flag is set if the Signal:
 * <br>1) is set in a clocked process;
 * <br>
 * <br> <b>NB! </b> A warning is issued if a Signal is set in a
 * non-clocked process, which has a branch where the Signal is not set.
 * See {@link
 * base.hldd.structure.models.utils.AbstractModelCreator.GraphVariablesSorter#printNonInitialisedVars()
 * GraphVariablesSorter.printNonInitialisedVars()}.
 *
 * @author Anton Chepurov
 */
public class DelayFlagCollector extends AbstractVisitor {

	private final OperandStorage dFlagOperands = new OperandStorage();

	private final ConfigurationHandler config;

	public DelayFlagCollector(ConfigurationHandler config) {
		this.config = config;
	}

	public OperandStorage getDFlagOperands() {
		return dFlagOperands;
	}

	public void visitEntity(Entity entity) throws Exception {
		SignalDelayFlagCollector sigCollector = new SignalDelayFlagCollector(config);
		entity.traverse(sigCollector);
		dFlagOperands.storeAll(sigCollector.getDFlagOperands());

		VariableDelayFlagCollector varCollector = new VariableDelayFlagCollector();
		entity.traverse(varCollector);
		dFlagOperands.storeAll(varCollector.getDFlagOperands());
	}

	public void visitArchitecture(Architecture architecture) throws Exception {
	}

	public void visitProcess(Process process) throws Exception {
	}

	public void visitIfNode(IfNode ifNode) throws Exception {
	}

	public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
	}

	public void visitCaseNode(CaseNode caseNode) throws Exception {
	}

	public void visitWhenNode(WhenNode whenNode) throws Exception {
	}

}
