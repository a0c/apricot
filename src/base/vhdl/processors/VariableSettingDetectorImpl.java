package base.vhdl.processors;

import base.vhdl.structure.nodes.*;
import base.vhdl.structure.*;
import base.Type;
import base.Indices;

import java.util.List;
import java.util.HashSet;
import java.math.BigInteger;

import parsers.vhdl.PackageParser;

/**
 * For the Variable with the specified variable name, checks if this Variable
 * was set (initialized) in the tree before the specified starting node.
 * <p/>
 * <b>Implementational issues.</b><br>
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
public class VariableSettingDetectorImpl extends AbstractProcessor {
	private boolean isOperandSet = false;
	private final String variableName;
	private AbstractNode terminatingNode;
	/* Currently processed Entity, Architecture, and Process */
	private final Entity entity;
	private final Architecture architecture;
	private final base.vhdl.structure.Process process;
	private final HashSet<AbstractNode> completelySettingNodes;

	/**
	 * @param variableName		   name of the Variable to check
	 * @param startingNode		   node where to start from
	 * @param entity				 currently processed entity
	 * @param architecture		   currently processed architecture
	 * @param process				currently processed process
	 * @param completelySettingNodes set of nodes that set the specified variable completely.
	 * 								 Used for speedup in case of costly-to-process nodes (CaseNodes and IfNodes).
	 */
	public VariableSettingDetectorImpl(String variableName, AbstractNode startingNode,
									   Entity entity, Architecture architecture, base.vhdl.structure.Process process,
									   HashSet<AbstractNode> completelySettingNodes) {
		this.variableName = variableName;
		this.terminatingNode = startingNode;
		this.entity = entity;
		this.architecture = architecture;
		this.process = process;
		this.completelySettingNodes = completelySettingNodes;
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
		throw new RuntimeException(getClass().getSimpleName() + ": CaseNode is processed. WhenNode should've backtracked 2 levels.");
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
			* Parted settings are still in ToDo list. Also Consider using PartialSetVariableCollector in VariableDelayFlagCollector.visitProcess() ==> commented code.
			* Todo: PartialSetVariableCollector itself doesn't have to be used in OperandSettingDetectorImpl. Here only check that required part of the variable was previously set. */
			TransitionNode transitionNode = (TransitionNode) abstractNode;
			if (transitionNode.isNull()) return false;

			OperandImpl operand = transitionNode.getTargetOperand();
			// todo: commented for Elsevier paper... (hc11 CPU)
//			if (operand.isParted())
//				throw new RuntimeException(OperandSettingDetectorImpl.class.getSimpleName() + " doesn't currently support parted operands"); //todo: also see DelayFLagCollector

			return operand.getName().equals(variableName);
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
			Indices valueRange = caseType.isEnum() ? caseType.getValueRange() : caseType.getLength().deriveValueRange();
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

	private boolean areAllValuesListed(Indices valueRange, List<WhenNode> conditions) {
		/* All values are definitely listed, if When-Node with Others exist.
		* So check the last condition */
		if (conditions.get(conditions.size() - 1).isOthers()) return true;
		/* Unfortunately have to check all the values one by one. */
		/* Init all unlisted values. */
		HashSet<BigInteger> unlistedValues = new HashSet<BigInteger>(valueRange.length());
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
					Constant constant = resolveConstant(conditionAsString);
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

	private Constant resolveConstant(String constantName) {
		Constant constant;
		if ((constant = process.resolveConstant(constantName)) != null) {
			return constant;
		} else if ((constant = architecture.resolveConstant(constantName)) != null) {
			return constant;
		} else if ((constant = entity.resolveConstant(constantName)) != null) {
			return constant;
		}
		return null;
	}

	private void doBacktrack(AbstractNode newTerminatingNode) {
		terminatingNode = newTerminatingNode;
	}

	private Type resolveType(AbstractOperand abstractOperand) {
		if (abstractOperand instanceof OperandImpl) {
			OperandImpl operand = (OperandImpl) abstractOperand;
			String operandName = operand.getName();
			/* ... */
			Indices partedIndices = operand.getPartedIndices();
			Type operandType;

			/* Resolve the variable/signal/port by name and get its type */
			Variable variable;
			Signal signal;
			Port port;
			if ((variable = process.resolveVariable(operandName)) != null) {
				operandType = variable.getType();
			} else if ((signal = architecture.resolveSignal(operandName)) != null) {
				operandType = signal.getType();
			} else if ((port = entity.resolvePort(operandName)) != null) {
				operandType = port.getType();
			} else throw new RuntimeException(getClass().getSimpleName() + ": cannot resolve operand: " + operand);
			if (operandType == null)
				throw new RuntimeException(getClass().getSimpleName() + ": empty type for operand " + operand);
			/* Derive parted type */
			return operandType.derivePartedType(partedIndices);

		} else
			throw new RuntimeException(getClass().getSimpleName() + ": cannot resolve type for " + abstractOperand.getClass().getSimpleName());
	}

}
