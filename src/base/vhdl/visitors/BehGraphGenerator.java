package base.vhdl.visitors;

import base.vhdl.structure.Constant;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.WhenNode;
import ui.ConfigurationHandler;
import ui.ConverterSettings;

import java.util.Collection;

/**
 * @author Anton Chepurov
 */
public class BehGraphGenerator extends GraphGenerator {
	private Collection<String> registers;

	public BehGraphGenerator(ConfigurationHandler config, ConverterSettings settings, Collection<Constant> generics, Collection<String> registers) {
		super(config, settings, generics, GeneratorType.Behavioural);
		this.registers = registers;
	}

	public void visitProcess(base.vhdl.structure.Process process) throws Exception {

		/* At first, process partial settings, like "Parity(7) <= something;" */
		processPartialSettings(modelCollector.getPartialAssignmentsFor(process), process.getRootNode());

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

	protected boolean isDelay(String variableName) {
		return registers.contains(variableName);
	}

}
