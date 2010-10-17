package base.vhdl.visitors;

import base.vhdl.structure.*;
import base.vhdl.structure.nodes.*;
import base.vhdl.processors.VariableSettingDetectorImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.HashMap;

/**
 * Class traverses the tree and checks the usages of {@link Variable}-s.
 * <br>Variable is used if:
 * <br>1) it resides in the condition of an {@link IfNode};
 * <br>2) it resides in the condition of a {@link CaseNode};
 * <br>3) it resides in the RHS of a {@link TransitionNode}.
 * <br>
 * <br> Every used variable is then checked to be set before use. If it
 * wasn't then the D-flag of the Variable is activated.
 *
 * @author Anton Chepurov
 */
public class VariableDelayFlagCollector extends AbstractVisitor {
	/**
	 * Collection of names of {@link base.vhdl.structure.Variable}-s with activated D-flag
	 */
	private final Collection<String> dFlagNames = new HashSet<String>();
	/**
	 * Collection of names of {@link base.vhdl.structure.Variable}-s from processed {@link base.vhdl.structure.Process}
	 */
	private Collection<String> varNames;
	/**
	 * Map of sets to speedup setting checks. Used in {@link base.vhdl.processors.VariableSettingDetectorImpl}.
	 */
	private HashMap<String, HashSet<AbstractNode>> completelySettingNodesByVar;

	/**
	 * Currently processed entity
	 */
	private Entity entity;
	/**
	 * Currently processed architecture
	 */
	private Architecture architecture;
	/**
	 * Currently processed process
	 */
	private base.vhdl.structure.Process curProcess;

	public Collection<String> getDFlagNames() {
		return dFlagNames;
	}

	public void visitEntity(Entity entity) throws Exception {
		this.entity = entity;
	}

	public void visitArchitecture(Architecture architecture) throws Exception {
		this.architecture = architecture;
	}

	public void visitProcess(base.vhdl.structure.Process process) throws Exception {
		/* Clear Variable names from the previous process */
		varNames = new HashSet<String>();
		completelySettingNodesByVar = new HashMap<String, HashSet<AbstractNode>>();
		/* Collect Variable names */
		for (Variable variable : process.getVariables()) {
			String varName = variable.getName();
			varNames.add(varName);
			completelySettingNodesByVar.put(varName, new HashSet<AbstractNode>());
		}
//        PartialSetVariableCollector collector = new PartialSetVariableCollector();
//        process.traverse(collector);
//        Map<String, Set<OperandImpl>> partialSettingsMap = collector.getPartialSettingsMap();
//        if (!partialSettingsMap.isEmpty()) {
//            partialSettingsMapByProcess.put(process, partialSettingsMap);
//        }


		/* If process declares no variables, skip the process */
		if (varNames.isEmpty()) return;
		/* Remember process */
		curProcess = process;
		/* Traverse the tree */
		process.getRootNode().traverse(this);
	}

	public void visitIfNode(IfNode ifNode) throws Exception {
		/* Exit when all Variables have been processed. */
		if (varNames.isEmpty()) return;
		/* Process CONDITION */
		/* Extract operand names from condition expression and check them */
		doCheckOperands(extractSetOfOperandNamesFrom(ifNode.getConditionExpression()), ifNode);

		/* Process TRUE PART */
		ifNode.getTruePart().traverse(this);
		/* Exit when all Variables have been processed. */
		if (varNames.isEmpty()) return;

		/* Process FALSE PART */
		if (ifNode.getFalsePart() != null) {
			ifNode.getFalsePart().traverse(this);
		}
	}

	public void visitTransitionNode(TransitionNode transitionNode) throws Exception {
		/* Exit when all Variables have been processed. */
		if (varNames.isEmpty()) return;
		/* Extract operand names from value transition and check them */
		doCheckOperands(extractSetOfOperandNamesFrom(transitionNode.getValueOperand()), transitionNode);
	}

	public void visitCaseNode(CaseNode caseNode) throws Exception {
		/* Exit when all Variables have been processed. */
		if (varNames.isEmpty()) return;
		/* Process CONDITION */
		AbstractOperand conditionOperand = caseNode.getVariableOperand();
		if (!(conditionOperand instanceof OperandImpl))
			throw new Exception("Expression conditions in CaseNode are not currently supported by "
					+ VariableDelayFlagCollector.class.getSimpleName()
					+ "\nCaseNode condition: " + conditionOperand.toString());
		doCheckOperands(java.util.Collections.singleton(((OperandImpl) conditionOperand).getName()), caseNode);

		/* Process CONDITIONS */
		for (WhenNode whenNode : caseNode.getConditions()) {
			whenNode.traverse(this);
			/* Exit when all Variables have been processed. */
			if (varNames.isEmpty()) return;
		}
	}

	public void visitWhenNode(WhenNode whenNode) throws Exception {
		whenNode.getTransitions().traverse(this);
	}

	/**
	 * Checks whether the Variables with the names specified by operandNames are
	 * set in the tree before the specified operandNode. If they weren't then
	 * the operands' D-flags are activated and the operands are removed from
	 * varNames.
	 *
	 * @param operandNames names of the operands to check
	 * @param operandNode  node where the specified operands originate from
	 * @throws Exception if
	 * 					{@link #wasVariableSet(String, base.vhdl.structure.nodes.AbstractNode)} throws an Exception.
	 */
	private void doCheckOperands(Collection<String> operandNames, AbstractNode operandNode) throws Exception {
		/* Check each operand to be set by this point. If operand hasn't
		been set so far, activated Delay flag for it. */
		for (String operandName : operandNames) {
			if (!wasVariableSet(operandName, operandNode)) {
				/* Collect this operand */
				dFlagNames.add(operandName);
				/* Remove this operand from varNames, to speed up the traversal */
				varNames.remove(operandName);
			}
		}
	}

	/**
	 * @param operandName name of the operand to check
	 * @param operandNode node that contains the specified operand
	 * @return <code>true</code> if the specified operand is a {@link Variable}
	 * 		   operand and it was set in the tree before this node, or if it is
	 * 		   not a {@link Variable} operand. <code>false</code> if it is a
	 * 		   {@link Variable} operand and it wasn't set.
	 * @throws Exception if {@link base.vhdl.processors.VariableSettingDetectorImpl} throws an Exception
	 */
	private boolean wasVariableSet(String operandName, AbstractNode operandNode) throws Exception {
		/* Skip those operands that are not Variables */
		if (!varNames.contains(operandName)) return true;

		VariableSettingDetectorImpl operandSetDetector
				= new VariableSettingDetectorImpl(operandName, operandNode, entity, architecture, curProcess, completelySettingNodesByVar.get(operandName));
		operandSetDetector.detect();

		return operandSetDetector.isOperandSet();
	}

	/**
	 * @param abstractOperand where to extract operands from
	 * @return Collection of operand names in an ascending order
	 */
	static Collection<String> extractSetOfOperandNamesFrom(AbstractOperand abstractOperand) {
		Collection<String> operandNamesSet = new TreeSet<String>();
		if (abstractOperand instanceof OperandImpl) {
			OperandImpl operand = (OperandImpl) abstractOperand;
			operandNamesSet.add(operand.getName());
		} else if (abstractOperand instanceof Expression) {
			for (AbstractOperand absOperand : ((Expression) abstractOperand).getOperands()) {
				operandNamesSet.addAll(extractSetOfOperandNamesFrom(absOperand));
			}
		} else if (abstractOperand instanceof UserDefinedFunction) {
			for (AbstractOperand absOperand : ((UserDefinedFunction) abstractOperand).getOperands()) {
				operandNamesSet.addAll(extractSetOfOperandNamesFrom(absOperand));
			}
		}
		return operandNamesSet;
	}

}
