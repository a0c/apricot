package base.vhdl.visitors;

import base.vhdl.structure.nodes.*;
import ui.ConfigurationHandler;
import ui.ConverterSettings;

import java.util.Collection;

/**
 * @author Anton Chepurov
 */
public class BehGraphGenerator extends GraphGenerator {
	private Collection<String> registers;

	public BehGraphGenerator(ConfigurationHandler config, ConverterSettings settings, Collection<String> registers) {
		super(config, settings, GeneratorType.Behavioural);
		this.registers = registers;
	}

	public void visitProcess(base.vhdl.structure.Process process) throws Exception {

		if (modelCollector.hasPartialAssignmentsIn(process)) {
			/* At first, process partial settings, like "Parity(7) <= something;" */
			processPartialSettings(process);
		}

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
