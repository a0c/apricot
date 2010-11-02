package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.WhenNode;
import ui.ConfigurationHandler;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class collects those {@link Variable}-s and {@link Signal}-s
 * that should have <code>D-flag</code> set.
 * <br>For a given {@link Variable}, flag is set if the Variable:
 * <br>1) is read without being set before.
 * <br>For a given {@link Signal}, flag is set if the Signal:
 * <br>1) is set in a clocked process;
 * <br>2) is set in a non-clocked process, which has a branch
 * where the Signal is not set. <b>NB! </b> Currently an exception
 * (warning) is thrown for the second case.
 *
 * @author Anton Chepurov
 */
public class DelayFlagCollector extends AbstractVisitor {
	private final Collection<String> dFlagNames = new HashSet<String>();
	private final ConfigurationHandler config;

	public DelayFlagCollector(ConfigurationHandler config) {
		this.config = config;
	}

	public Collection<String> getDFlagNames() {
		return dFlagNames;
	}

	public void visitEntity(Entity entity) throws Exception {
		/* ##########################
		###### Collect SIGNALS ######
		###########################*/
		/* Traverse the architecture with a new SignalDelayFlagCollector */
		SignalDelayFlagCollector sigCollector = new SignalDelayFlagCollector(config);
		entity.traverse(sigCollector);
		/* Add collected Signal dFlagNames to the global dFlagNames collection */
		dFlagNames.addAll(sigCollector.getDFlagNames());

		/* ##########################
		##### Collect VARIABLES #####
		###########################*/
		/* Traverse the process with a new VariableDelayFlagCollector */
		VariableDelayFlagCollector varCollector = new VariableDelayFlagCollector();
		entity.traverse(varCollector);
		/* Add collected Variable dFlagNames to the global dFlagNames collection */
		dFlagNames.addAll(varCollector.getDFlagNames());
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
