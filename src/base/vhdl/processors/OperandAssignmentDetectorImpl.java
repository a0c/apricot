package base.vhdl.processors;

import base.Range;
import base.Type;
import base.vhdl.structure.AbstractOperand;
import base.vhdl.structure.Constant;
import base.vhdl.structure.OperandImpl;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.*;
import parsers.vhdl.PackageParser;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * For the Variable with the specified variable name, checks if this Variable
 * was set (initialized) in the tree before the specified starting node.
 * <p/>
 * <b>Implementation issues.</b><br>
 * Search for the initialization of the specified variable is performed using
 * <i>backtrack</i>, starting from startingNode. To perform backtrack, dynamic
 * terminating node is used (at the beginning, terminating node is set to
 * starting node received as a constructor parameter):
 * <br>1) For terminating node, its parent is obtained.
 * <br>2) The parent's children (siblings of terminating node) are searched for
 * variable initialization up to this terminating node.
 * <br>3) After all the children have been processed, backtrack is performed by
 * setting the terminating node to (generally) the parent node. In some cases (
 * WhenNode) grandparent node is used.
 *
 * @author Anton Chepurov
 */
public class OperandAssignmentDetectorImpl extends AbstractProcessor {
	private boolean isOperandSet = false;
	private final OperandImpl operandToCheck;
	private AbstractNode terminatingNode;
	/* Currently processed Process */
	private final Process process;
	private final Set<AbstractNode> completelySettingNodes;

	/**
	 * @param operandToCheck to be checked
	 * @param startingNode   node where to start from
	 * @param process		currently processed process
	 * @param settingNodes   set of nodes that set the specified variable completely.
	 */
	public OperandAssignmentDetectorImpl(OperandImpl operandToCheck, AbstractNode startingNode, Process process,
									   Set<AbstractNode> settingNodes) {
		this.operandToCheck = operandToCheck;
		this.terminatingNode = startingNode;
		this.process = process;
		this.completelySettingNodes = settingNodes;
	}

	public boolean isOperandSet() {
		return isOperandSet;
	}

	public void detect() throws Exception {
		/* startingNode (terminatingNode) can be either IfNode, CaseNode or TransitionNode */
		AbstractNode parentNode;
		while ((parentNode = terminatingNode.getParentNode()) != null) {
			parentNode.process(this);
			/* If setting is detected, stop search */
			if (isOperandSet) break;
		}
		/* Check preceding siblings */
		if (!isOperandSet) {
			process.getRootNode().process(this);
		}

	}

	public void processIfNode(IfNode ifNode) {
		if (ifNode.hasFalseChild(terminatingNode)) {
			ifNode.getFalsePart().process(this);
		} else {
			ifNode.getTruePart().process(this);
		}
		/* If setting is detected, stop search */
		if (isOperandSet) return;

		/* Go one level up */
		doBacktrack(ifNode);
	}

	public void processCompositeNode(CompositeNode compositeNode) {
		/* Search amongst siblings */
		for (AbstractNode childNode : compositeNode.getChildren()) {
			/* Exit, if the terminatingNode is reached */
			if (childNode == terminatingNode) return;
			/* If setting is detected, stop search */
			if (isSettingDetected(childNode)) {
				isOperandSet = true;
				return;
			}
		}
	}

	/* Will never occur, since WhenNode backtracks 2 levels */

	public void processCaseNode(CaseNode caseNode) {
		throw new RuntimeException(getClass().getSimpleName() + ": CaseNode is processed. WhenNode should have backtracked 2 levels.");
	}

	public void processWhenNode(WhenNode whenNode) {
		/* Search amongst siblings */
		whenNode.getTransitions().process(this);

		/* If setting is detected, stop search */
		if (isOperandSet) return;

		/* Go 2 levels up */
		doBacktrack(whenNode.getParentNode());
	}

	public void processTransitionNode(TransitionNode transitionNode) throws Exception {
	}

	/**
	 * @param abstractNode node to check for variable setting
	 * @return <tt>true</tt> if the specified node completely sets the variable. <tt>false</tt> otherwise.
	 */
	private boolean isSettingDetected(AbstractNode abstractNode) {
		/* For control nodes => check to be set in every branch, otherwise not set. */
		if (abstractNode instanceof TransitionNode) {
			/* TransitionNode defines the variable completely (if it defines the specified variable at all).
//			* Range assignments are still in ToDo list. (Is it???) Also Consider using RangeAssignmentCollector in VariableDelayFlagCollector.visitProcess() ==> commented code.
			* Todo: RangeAssignmentCollector itself doesn't have to be used in OperandSettingDetectorImpl. Here only check that required range of the variable was previously set. */
			TransitionNode transitionNode = (TransitionNode) abstractNode;
			if (transitionNode.isNull()) return false;

			OperandImpl operand = transitionNode.getTargetOperand();
			// todo: commented for Elsevier paper... (hc11 CPU)
//			if (operand.isRange())
//				throw new RuntimeException(OperandSettingDetectorImpl.class.getSimpleName() + " doesn't currently support range operands"); //todo: also see DelayFLagCollector

			return operand.contains(operandToCheck, process);

		} else if (abstractNode instanceof CaseNode) {
			/* Speed up with previous results */
			if (completelySettingNodes.contains(abstractNode)) {
				return true;
			}
			/* CaseNode defines the variable completely, if all the following conditions hold:
			* 1) all conditions of its dependent variable are listed in WhenNodes;
			* 2) CaseNode sets the variable in all its WhenNode-s. */
			CaseNode caseNode = (CaseNode) abstractNode;
			/* Resolve CaseType */
			Type caseType = resolveType(caseNode.getVariableOperand());
			/* Check that all conditions of CaseNode dependent variable are listed in WhenNodes */
			Range valueRange = caseType.resolveValueRange();
			if (!areAllValuesListed(valueRange, caseNode.getConditions()))
				return false; //todo: this check will be incomplete, if subsequent setting CaseNodes are applied
			/* Check all conditions to set the variable */
			for (WhenNode whenNode : caseNode.getConditions()) {
				if (!isSettingDetected(whenNode.getTransitions())) return false;
			}
			/* Remember the completely setting node for further speedup */
			completelySettingNodes.add(caseNode);
			return true;

		} else if (abstractNode instanceof CompositeNode) {
			/* CompositeNode defines the variable completely, if at least one of its children sets the variable completely */
			CompositeNode compositeNode = (CompositeNode) abstractNode;
			for (AbstractNode child : compositeNode.getChildren()) {
				if (isSettingDetected(child)) return true;
			}
			return false;

		} else if (abstractNode instanceof IfNode) {
			/* Speed up with previous results */
			if (completelySettingNodes.contains(abstractNode)) {
				return true;
			}
			/* IfNode defines the variable completely, if it sets the variable both in its TRUE_PART and FALSE_PART. */
			//todo: this check will be incomplete, if subsequent setting IfNodes are applied
			IfNode ifNode = (IfNode) abstractNode;
			/* If FALSE_PART is missing, IfNode doesn't define the variable completely */
			if (ifNode.getFalsePart() == null) return false;
			/* Check FALSE_PART to define the variable completely */
			if (!isSettingDetected(ifNode.getFalsePart())) return false;
			/* Check TRUE_PART to define the variable completely */
			if (!isSettingDetected(ifNode.getTruePart())) return false;
			/* Remember the completely setting node for further speedup */
			completelySettingNodes.add(ifNode);
			return true;
		}
		throw new RuntimeException(getClass().getSimpleName() + ": cannot detect variable setting in following node: " + abstractNode);
	}

	private boolean areAllValuesListed(Range valueRange, List<WhenNode> conditions) {
		/* All values are definitely listed, if When-Node with Others exist.
		* So check the last condition */
		if (conditions.get(conditions.size() - 1).isOthers()) return true;
		/* Unfortunately have to check all the values one by one. */
		/* Init all unlisted values. */
		Set<BigInteger> unlistedValues = new HashSet<BigInteger>(valueRange.length());
		for (int condition = valueRange.getLowest(); condition <= valueRange.getHighest(); condition++)
			unlistedValues.add(BigInteger.valueOf(condition));
		/* Iterate listed conditions and remove listed ones from the set of unlisted values */
		for (WhenNode whenNode : conditions) {
			for (OperandImpl conditionOperand : whenNode.getConditionOperands()) {
				String conditionAsString = conditionOperand.getName();
				/* Try parsing constant */
				BigInteger conditionValue = PackageParser.parseConstantValue(conditionAsString);
				if (conditionValue == null) {
					/* Try resolving named constant */
					Constant constant = process.resolveConstant(conditionAsString);
					if (constant == null)
						throw new RuntimeException(getClass().getSimpleName() + ": cannot resolve named constant: " + conditionAsString);
					conditionValue = constant.getValue();
				}
				/* Remove condition value from the set of unlisted values */
				unlistedValues.remove(conditionValue);
			}
		}
		/* All values are listed, if all values were listed by WhenNode conditions. */
		return unlistedValues.isEmpty();
	}

	private void doBacktrack(AbstractNode newTerminatingNode) {
		terminatingNode = newTerminatingNode;
	}

	private Type resolveType(AbstractOperand abstractOperand) {
		if (abstractOperand instanceof OperandImpl) {
			OperandImpl operand = (OperandImpl) abstractOperand;

			/* Resolve the variable/signal/port by name and get its type */
			Type operandType = process.resolveType(operand.getName());
			if (operandType == null)
				throw new RuntimeException(getClass().getSimpleName() + ": empty type for operand " + operand);
			/* Derive range type */
			return operandType.deriveRangeType(operand.getRange());

		} else
			throw new RuntimeException(getClass().getSimpleName() + ": cannot resolve type for " + abstractOperand.getClass().getSimpleName());
	}

}
