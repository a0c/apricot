package base.vhdl.visitors;

import base.vhdl.structure.Architecture;
import base.vhdl.structure.Constant;
import base.vhdl.structure.Entity;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.*;
import ui.ConfigurationHandler;
import ui.ConverterSettings;

import java.util.Collection;

/**
 * @author Anton Chepurov
 */
public class BehDDGraphGenerator extends GraphGenerator {

	public BehDDGraphGenerator(ConfigurationHandler config, ConverterSettings settings, Collection<Constant> generics) {
		super(config, settings, generics, GeneratorType.BehaviouralDD);
	}

	public void visitProcess(base.vhdl.structure.Process process) throws Exception {

		if (modelCollector.hasPartialAssignmentsIn(process)) {
			/* Process partial settings, like "Parity(7) <= something;" */
			processPartialSettings(modelCollector.getPartialAssignmentsFor(process), process.getRootNode());
		} else {
			/* Process process NAME */
			String graphVarName = extractVariableName(process);
			if (graphVarName == null)
				throw new Exception("Could not extract GraphVariable name for the process with name \"" + process.getName() + "\"");

			couldProcessNextGraphVariable(modelCollector.getVariable(graphVarName), process.getRootNode());
		}
	}

	static String extractVariableName(Process process) throws Exception {
		VariableNameExtractor varNameExtractor = new VariableNameExtractor();
		process.traverse(varNameExtractor);
		return varNameExtractor.getVariableName();
	}

	protected void doCheckTruePart(IfNode ifNode) throws Exception {
		doCheckChildrenOf(ifNode.getTruePart(), "(IF): " + ifNode.getConditionExpression());
	}

	protected void doCheckFalsePart(IfNode ifNode) throws Exception {
		doCheckChildrenOf(ifNode.getFalsePart(), "(ELSIF): " + ifNode.getConditionExpression());
	}

	protected void doCheckWhenTransitions(WhenNode whenNode) throws Exception {
		doCheckChildrenOf(whenNode.getTransitions(), "(WHEN): " + java.util.Arrays.toString(whenNode.getConditionOperands()));
	}

	protected boolean isDelay(String variableName) {
		/* For Beh DD Trees no information is available regarding Delay flags, so set it active... */
		return true;
	}

	private void doCheckChildrenOf(CompositeNode compositeNode, String conditionForError) throws Exception {
		if (!isSingleVarSet(compositeNode)) {
			throw new Exception("Error in VHDL structure: DD Tree can set only 1 variable in process." +
					"\nCondition " + conditionForError +
					"\nNodes\n" + compositeNode);
		}
	}

	private boolean isSingleVarSet(CompositeNode compositeNode) {
		for (AbstractNode child : compositeNode.getChildren()) {
			if (child instanceof TransitionNode) {
				TransitionNode transitionNode = (TransitionNode) child;
				if (transitionNode.isNull()) continue;
				if (!transitionNode.getTargetOperand().getName().equalsIgnoreCase(graphVariable.getName())) {
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * Extracts name of the Variable that is set in the specified process.
	 * In BehDD, a single process can set one Variable only.
	 */
	private static class VariableNameExtractor extends AbstractVisitor {
		private String variableName = null;
		private boolean stop = false;


		public String getVariableName() {
			return variableName;
		}

		public void visitEntity(Entity entity) throws Exception {
		}

		public void visitArchitecture(Architecture architecture) throws Exception {
		}

		public void visitProcess(Process process) throws Exception {
			process.getRootNode().traverse(this);
		}

		public void visitIfNode(IfNode ifNode) throws Exception {
			if (stop) return;
			ifNode.getTruePart().traverse(this);
			if (ifNode.getFalsePart() != null) {
				ifNode.getFalsePart().traverse(this);
			}
		}

		public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
			if (stop) return;
			if (!transitionNode.isNull()) {
				variableName = transitionNode.getTargetOperand().getName();
				stop = true;
			}
		}

		public void visitCaseNode(CaseNode caseNode) throws Exception {
			if (stop) return;
			for (WhenNode whenNode : caseNode.getConditions()) {
				whenNode.traverse(this);
			}
		}

		public void visitWhenNode(WhenNode whenNode) throws Exception {
			if (stop) return;
			whenNode.getTransitions().traverse(this);
		}
	}
}
