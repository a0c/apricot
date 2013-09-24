package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.*;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.logging.Logger;

import ui.ConfigurationHandler;

/**
 * @author Anton Chepurov
 */
public class VariableNameReplacerImpl extends AbstractVisitor {

	private static final Logger LOGGER = Logger.getLogger(VariableNameReplacerImpl.class.getName());

	private static final Pattern PROCESS_NAME_PATTERN = Pattern.compile("^PROCESS_\\d+$", Pattern.CASE_INSENSITIVE);

	/**
	 * Holds the name of the currently traversed process. Used for creation of names of variables.
	 */
	private String prefix;

	private int nextProcessIndex = 1;

	private Set<String> variableNames;

	private String stateVarName;
	private String stateProcessName = null;
	private int stateProcessOrder = -1;

	private boolean isStateVarSet = false;
	private boolean isInStateProcess;
	private ConfigurationHandler config;

	public VariableNameReplacerImpl(ConfigurationHandler config) {
		this.config = config;
		readStateName();
	}

	private void readStateName() {
		stateVarName = config.getStateVarName();
		if (stateVarName != null && stateVarName.length() > 0) {
			if (stateVarName.contains(".")) {
				// extract process name or order
				String[] tokens = stateVarName.split("\\.");
				if (tokens.length == 2) {
					stateVarName = tokens[1].trim();
					stateProcessName = tokens[0].trim();
					if (PROCESS_NAME_PATTERN.matcher(stateProcessName).matches()) {
						// extract order of the process
						stateProcessOrder = Integer.parseInt(stateProcessName.substring(stateProcessName.lastIndexOf("_") + 1).trim());
						stateProcessName = null;
					}
				} else {
					LOGGER.finer("STATE variable name contains " + tokens.length + " dots");
					throw new RuntimeException("Don't know how to process specified STATE variable name: " + stateVarName +
							"\nSupported formats:" +
							"\nPROCESS_XXX.<state_var_name>, where XXX is the order of the process in VHDL file starting from 1" +
							"\n<process_name>.<state_var_name>" +
							"\n<state_var_name>");
				}
			} else {
				// do nothing, leave stateVarName as is
			}
		} else {
			LOGGER.finer("STATE variable not specified");
		}
	}

	/**
	 * E.g.:
	 * <br>     "#PROCESS_1#__STATE"   ==> for process without name
	 * <br>     "#PREFIX#__STATE"   ===> for process with name PREFIX
	 *
	 * @param process to extract name from
	 * @return process name or stub name wrapped into "#"-s
	 */
	String createPrefix(Process process) {
		StringBuilder builder = new StringBuilder("#");

		String processName = process.getName();
		if (processName == null || processName.length() == 0) {
			/* Generate stub name */
			processName = "PROCESS_" + nextProcessIndex++;
		}
		builder.append(processName).append("#__");

		return builder.toString();
	}

	public void visitEntity(Entity entity) throws Exception {
	}

	public void visitArchitecture(Architecture architecture) throws Exception {
	}

	public void visitProcess(Process process) throws Exception {
		prefix = createPrefix(process);
		variableNames = new HashSet<String>();
		isInStateProcess = isInStateProcess(process);
		for (Variable variable : process.getVariables()) {
			String varNameOrig = variable.getName();

			variableNames.add(varNameOrig);
			variable.setName(newName(varNameOrig));

			updateStateVariableName(variable, varNameOrig);
		}
		process.getRootNode().traverse(this);
	}

	private void updateStateVariableName(Variable variable, String varNameOrig) throws Exception {
		if (isInStateProcess && varNameOrig.equalsIgnoreCase(stateVarName)) {
			// do it only once
			if (!isStateVarSet) {
				config.setStateVarName(variable.getName());
				isStateVarSet = true;
			} else {
				String message = "Trying to update the name of STATE variable twice." +
						"\n" +
						"\nPossible reason: " +
						"\nSTATE variable name is not specified strictly enough in .config file, " +
						"\nso that variables from different processes match with the specification." +
						"\n" +
						"\nYou should probably prefix the STATE variable name with a process name followed by a dot (.), e.g:" +
						"\nPROCESS_1.<state_var_name>,      where 1 is the order of the process in VHDL file starting from 1" +
						"\n<process_name>.<state_var_name>";
				LOGGER.finer(message);
				throw new Exception(message);
			}
		}
	}

	@SuppressWarnings({"SimplifiableIfStatement"})
	private boolean isInStateProcess(Process curProcess) {
		if (stateProcessName != null) {
			return curProcess.getName().equalsIgnoreCase(stateProcessName); // names match
		} else if (stateProcessOrder != -1) {
			return (nextProcessIndex - 1) == stateProcessOrder; // orders match
		} else return true; // process not specified in config file
	}

	private String newName(String oldName) {
		return prefix + oldName;
	}

	public void visitIfNode(IfNode ifNode) throws Exception {
		/* Replace variable name in condition */
		replaceOperand(ifNode.getConditionExpression());
		/* Replace variable name in TRUE PART */
		ifNode.getTruePart().traverse(this);
		/* Replace variable name in FALSE PART */
		if (ifNode.getFalsePart() != null) {
			ifNode.getFalsePart().traverse(this);
		}
	}

	private void replaceOperand(AbstractOperand abstractOperand) {
		if (abstractOperand instanceof Expression) {
			Expression expression = (Expression) abstractOperand;
			for (AbstractOperand operand : expression.getOperands()) {
				replaceOperand(operand);
			}
		} else if (abstractOperand instanceof OperandImpl) {
			OperandImpl operand = (OperandImpl) abstractOperand;
			if (variableNames.contains(operand.getName())) {
				operand.setName(newName(operand.getName()));
			}
			if (operand.isDynamicRange()) {
				OperandImpl dynamicRange = operand.getDynamicRange();
				if (variableNames.contains(dynamicRange.getName())) {
					dynamicRange.setName(newName(dynamicRange.getName()));
				}
			}
		}
	}

	public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
		/* Replace variable name in target operand */
		replaceOperand(transitionNode.getTargetOperand());
		/* Replace variable name in value operand */
		replaceOperand(transitionNode.getValueOperand());
	}

	public void visitCaseNode(CaseNode caseNode) throws Exception {
		/* Replace variable name in condition operand */
		replaceOperand(caseNode.getVariableOperand());
		/* Replace variable name in when-conditions */
		for (WhenNode whenNode : caseNode.getConditions()) {
			whenNode.traverse(this);
		}
	}

	public void visitWhenNode(WhenNode whenNode) throws Exception {
		whenNode.getTransitions().traverse(this);
	}
}
