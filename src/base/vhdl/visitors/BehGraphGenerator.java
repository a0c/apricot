package base.vhdl.visitors;

import base.vhdl.structure.Constant;
import base.vhdl.structure.OperandImpl;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.WhenNode;
import base.vhdl.structure.utils.OperandStorage;
import ui.ConfigurationHandler;
import ui.ConverterSettings;

import java.util.Collection;

/**
 * @author Anton Chepurov
 */
public class BehGraphGenerator extends GraphGenerator {

	private OperandStorage registers;

	public BehGraphGenerator(ConfigurationHandler config, ConverterSettings settings, Collection<Constant> generics, OperandStorage registers, boolean isF4RTL) {
		super(config, settings, generics, GeneratorType.Behavioural, isF4RTL);
		this.registers = registers;
	}

	public void visitProcess(base.vhdl.structure.Process process) throws Exception {

		/* (Re)Traverse the root node until all the variables
		*  that are set within this process are processed. */
		while (true) {
			if (!couldProcessNextGraphVariable(null, process.getRootNode()))
				break;
			else
				processedGraphVars.add(graphVariable.getName());

		}

	}

	protected void doCheckTruePart(IfNode ifNode) throws Exception { /* do nothing */ }

	protected void doCheckFalsePart(IfNode ifNode) throws Exception { /* do nothing */ }

	protected void doCheckWhenTransitions(WhenNode whenNode) throws Exception { /* do nothing */ }

	protected boolean isDelay(OperandImpl operand) {
		return registers.contains(operand, modelCollector);
	}

}
