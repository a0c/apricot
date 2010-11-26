package base.vhdl.visitors;

import base.vhdl.processors.OperandAssignmentDetectorImpl;
import base.vhdl.structure.*;
import base.vhdl.structure.nodes.*;
import base.vhdl.structure.utils.OperandStorage;

import java.util.*;

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
	private final OperandStorage dFlagOperands = new OperandStorage();
	/**
	 * Collection of names of {@link base.vhdl.structure.Variable}-s from processed {@link base.vhdl.structure.Process}
	 */
	private Collection<String> varNames;
	/**
	 * Map of sets to speedup setting checks. Used in {@link base.vhdl.processors.OperandAssignmentDetectorImpl}.
	 */
	private Map<String, Map<OperandImpl, Set<AbstractNode>>> settingNodesByVar;

	/**
	 * Currently processed process
	 */
	private base.vhdl.structure.Process curProcess;

	public OperandStorage getDFlagOperands() {
		return dFlagOperands;
	}

	public void visitEntity(Entity entity) throws Exception {
	}

	public void visitArchitecture(Architecture architecture) throws Exception {
	}

	public void visitProcess(base.vhdl.structure.Process process) throws Exception {
		/* Clear Variable names from the previous process */
		varNames = new HashSet<String>();
		settingNodesByVar = new HashMap<String, Map<OperandImpl, Set<AbstractNode>>>();
		/* Collect Variable names */
		for (Variable variable : process.getVariables()) {
			String varName = variable.getName();
			varNames.add(varName);
			settingNodesByVar.put(varName, new HashMap<OperandImpl, Set<AbstractNode>>());
		}
//        RangeAssignmentCollector collector = new RangeAssignmentCollector();
//        process.traverse(collector);
//        Map<String, Set<OperandImpl>> rangeAssignmentsMap = collector.getRangeAssignmentsMap();
//        if (!rangeAssignmentsMap.isEmpty()) {
//            rangeAssignmentsMapByProcess.put(process, rangeAssignmentsMap);
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
		doCheckOperands(extractOperandsFrom(ifNode.getConditionExpression()), ifNode);

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
		doCheckOperands(extractOperandsFrom(transitionNode.getValueOperand()), transitionNode);
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
		doCheckOperands(Collections.singleton((OperandImpl) conditionOperand), caseNode);

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
	 * @param operands	to be checked
	 * @param operandNode node where the specified operands originate from
	 * @throws Exception if
	 *                   {@link #wasOperandSet(base.vhdl.structure.OperandImpl,base.vhdl.structure.nodes.AbstractNode)} throws an Exception.
	 */
	private void doCheckOperands(Set<OperandImpl> operands, AbstractNode operandNode) throws Exception {
		/* Check each operand to be set by this point. If operand hasn't
		been set so far, activated Delay flag for it. */
		for (OperandImpl operand : operands) {
			if (!wasOperandSet(operand, operandNode)) {
				String varName = operand.getName();
				/* Collect this operand */
				dFlagOperands.store(varName, operand);
				/* Remove this operand from varNames, to speed up the traversal */
				if (dFlagOperands.isWholeRangeSet(varName, curProcess)) {
					varNames.remove(varName);
				}
			}
		}
	}

	/**
	 * @param operand	 operand to check
	 * @param operandNode node that contains the specified operand
	 * @return <code>true</code> if the specified operand is a {@link Variable}
	 *         operand and it was set in the tree before this node, or if it is
	 *         not a {@link Variable} operand. <code>false</code> if it is a
	 *         {@link Variable} operand and it wasn't set.
	 * @throws Exception if {@link base.vhdl.processors.OperandAssignmentDetectorImpl} throws an Exception
	 */
	private boolean wasOperandSet(OperandImpl operand, AbstractNode operandNode) throws Exception {
		/* Skip those operands that are not Variables */
		if (!varNames.contains(operand.getName())) return true;

		OperandAssignmentDetectorImpl assignmentDetector
				= new OperandAssignmentDetectorImpl(operand, operandNode, curProcess, obtainSettingNodes(operand));
		assignmentDetector.detect();

		return assignmentDetector.isOperandSet();
	}

	private Set<AbstractNode> obtainSettingNodes(OperandImpl operand) {

		Map<OperandImpl, Set<AbstractNode>> settingNodesByOperand = settingNodesByVar.get(operand.getName());

		if (settingNodesByOperand.containsKey(operand)) {
			return settingNodesByOperand.get(operand);
		}
		/* Don't analyze ranges. Too complex to gain any significant speed-up. */
		Set<AbstractNode> settingNodes = new HashSet<AbstractNode>();

		settingNodesByOperand.put(operand, settingNodes);

		return settingNodes;
	}

	/**
	 * @param abstractOperand where to extract operands from
	 * @return Collection of operands in an ascending order
	 */
	static Set<OperandImpl> extractOperandsFrom(AbstractOperand abstractOperand) {
		Set<OperandImpl> operands = new TreeSet<OperandImpl>(new Comparator<OperandImpl>() {
			@Override
			public int compare(OperandImpl o1, OperandImpl o2) {
				return o1.getName().compareTo(o2.getName());
			}
		}); //todo: why ordering?! Only for UnitTests?
		if (abstractOperand instanceof OperandImpl) {
			operands.add((OperandImpl) abstractOperand);
		} else if (abstractOperand instanceof Expression) {
			for (AbstractOperand absOperand : ((Expression) abstractOperand).getOperands()) {
				operands.addAll(extractOperandsFrom(absOperand));
			}
		} else if (abstractOperand instanceof UserDefinedFunction) {
			for (AbstractOperand absOperand : ((UserDefinedFunction) abstractOperand).getOperands()) {
				operands.addAll(extractOperandsFrom(absOperand));
			}
		}
		return operands;
	}

}
