package base.hldd.structure.models.utils;

import base.HLDDException;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.utils.Condition;
import base.hldd.structure.variables.*;
import base.hldd.structure.variables.Variable;
import base.hldd.visitors.DependentVariableReplacer;
import base.vhdl.structure.*;
import base.Indices;
import base.Type;

import java.util.*;
import java.math.BigInteger;

import base.vhdl.structure.Process;
import parsers.vhdl.PackageParser;

/**
 * @author Anton Chepurov
 */
public class ModelManager {
	private VariableManager variableManager;
	private PartialAssignmentManager partialAssignmentManager;
	private ConstantVariable constant0;
	private ConstantVariable constant1;

	public ModelManager() {
		variableManager = new VariableManager();
	}

	public void addVariable(AbstractVariable newVariable) {
		variableManager.addVariable(newVariable);
	}

	public void addVariable(String varName, AbstractVariable newVariable) {
		variableManager.addVariable(varName, newVariable);
	}

	public void removeVariable(AbstractVariable variableToRemove) {
		variableManager.removeVariable(variableToRemove);
	}

	public ConstantVariable getConstant0() throws Exception {
		if (constant0 == null) {
			constant0 = (ConstantVariable) convertOperandToVariable(new OperandImpl("0"), Indices.BIT_INDICES, false);
		}
		return constant0;
	}

	public ConstantVariable getConstant1() throws Exception {
		if (constant1 == null) {
			constant1 = (ConstantVariable) convertOperandToVariable(new OperandImpl("1"), Indices.BIT_INDICES, false);
		}
		return constant1;
	}

	public void rebase(AbstractVariable variableToRebase, AbstractVariable newBaseVariable) {

		/* Remove old variable from hash */
		removeVariable(variableToRebase);
		/* Remove base variable from hash */
		removeVariable(newBaseVariable);

		/* Replace base variable */
		((GraphVariable) variableToRebase).setBaseVariable(newBaseVariable);
		/* ReAdd updated old variable to hash */
		addVariable(variableToRebase);

		replaceInModel(newBaseVariable, new PartedVariableHolder(variableToRebase, null));
	}

	public void replaceWithIndices(AbstractVariable variableToReplace, PartedVariableHolder replacingVarHolder) {
		AbstractVariable replacingVariable = replacingVarHolder.getVariable();

		/* Remove old variable from hash */
		removeVariable(variableToReplace);
		/* Add new variable to hash */
		addVariable(replacingVariable);

		/* Replace old variable with new one in Functions and GraphVariables */
		replaceInModel(variableToReplace, replacingVarHolder);
	}

	private void replaceInModel(AbstractVariable variableToReplace, PartedVariableHolder replacingVarHolder) {

		AbstractVariable replacingVariable = replacingVarHolder.getVariable();
		/* Replace old variable with new one in Functions and GraphVariables */
		for (AbstractVariable absVariable : variableManager.getVariables()) {

			if (absVariable instanceof FunctionVariable) {
				FunctionVariable functionVariable = (FunctionVariable) absVariable;
				for (PartedVariableHolder operand : functionVariable.getOperands()) {
					if (operand.getVariable() == variableToReplace) {
						operand.setVariable(replacingVariable);
						if (replacingVarHolder.isParted()) {
							//todo: Indices.absoluteFor()...
							operand.setPartedIndices(replacingVarHolder.getPartedIndices());
						}
					}
				}

			} else if (absVariable instanceof GraphVariable) {
				GraphVariable graphVariable = (GraphVariable) absVariable;
				try {
					graphVariable.traverse(new DependentVariableReplacer(variableToReplace, replacingVarHolder));
				} catch (Exception e) {
					throw new RuntimeException("Error while traversing GraphVariable with DependentVariableReplacer: "
							+ e.getMessage(), e); /* should never happen */
				}
			}
		}
	}

	public PartedVariableHolder convertConditionalStmt(Expression conditionalStmt, boolean flattenCondition) throws Exception {
		boolean inverted = conditionalStmt.isInverted();

		/* Create FUNCTION */
		FunctionVariable functionVariable;
		if (flattenCondition) {
			functionVariable = createCompositeFunction(conditionalStmt);
			if (functionVariable != null) {
				return new PartedVariableHolder(functionVariable, null, adjustBooleanCondition(1, inverted));
			}
		}

		functionVariable = createFunction(conditionalStmt, false);
		return detectTrueValueAndSimplify(functionVariable, inverted); //todo: don't represent NOT as isInverted(). Use Function instead. And remove isInverted() parameter from here and further on. But think carefully: it may be a deeply internal INV in condition (there INV-s are preserved as functions)
	}

	/**
	 * Both, obtains correct true value for the functionVariable (puts it into the returned object),
	 * and tries to simplify boolean condition. Simplification is possible is EQ/NEQ compares
	 * a variable to a 1-bit constant.
	 *
	 * @param functionVariable functions whose true value is to be detected
	 * @param inverted		 is the source {@link base.vhdl.structure.Expression} is inverted
	 * @return holder of a variable, its indices and its true value
	 * @throws HLDDException if CompositeFunctionVariable is specified as a parameter
	 */
	private PartedVariableHolder detectTrueValueAndSimplify(FunctionVariable functionVariable, boolean inverted) throws HLDDException {

		if (functionVariable instanceof CompositeFunctionVariable) {
			throw new HLDDException("ModelManager: detectTrueValueAndSimplify(): FunctionVariable is expected as a parameter. Received: CompositeFunctionVariable.");
		}
		PartedVariableHolder originalFunction = new PartedVariableHolder(functionVariable, null, adjustBooleanCondition(1, inverted));

		Operator operator = functionVariable.getOperator();
		List<PartedVariableHolder> operands = functionVariable.getOperands();

		boolean isNEQ = operator == Operator.NEQ;
		if (operator == Operator.EQ || isNEQ) {
			ConstantVariable constantOperand;
			PartedVariableHolder variableOperand;

			PartedVariableHolder leftOp = operands.get(0);
			PartedVariableHolder rightOp = operands.get(1);
			if (rightOp.getVariable() instanceof ConstantVariable) {
				constantOperand = (ConstantVariable) rightOp.getVariable();
				variableOperand = leftOp;
			} else if (leftOp.getVariable() instanceof ConstantVariable) {
				constantOperand = (ConstantVariable) leftOp.getVariable();
				variableOperand = rightOp;
			} else {
				return originalFunction; // not compared to a Constant
			}

			if (constantOperand.getLength().length() > 1) { // more than a single bit is used
				return originalFunction;
			}

			// Remove EQ/NEQ FunctionVariable, for it is not needed
			removeVariable(functionVariable);

			int initialCondition = constantOperand.getValue().intValue();
			return new PartedVariableHolder(variableOperand.getVariable(),
					variableOperand.getPartedIndices(), adjustBooleanCondition(initialCondition, inverted ^ isNEQ));
		} else {
			return originalFunction;
		}

	}

	public PartedVariableHolder extractBooleanDependentVariable(AbstractOperand abstractOperand, boolean useComposites) throws Exception {
		if (abstractOperand instanceof Expression) {
			return convertConditionalStmt((Expression) abstractOperand, useComposites);
		} else if (abstractOperand instanceof OperandImpl) {
			OperandImpl operand = (OperandImpl) abstractOperand;
			return new PartedVariableHolder(getVariable(operand.getName()), operand.getPartedIndices(), getBooleanValueFromOperand(operand));
		}
		throw new Exception("Dependent variable is being extracted from : " + abstractOperand +
				"\nCurrently extraction of Dependent Variables is only supported for " +
				Expression.class.getSimpleName() + " and " + OperandImpl.class.getSimpleName());
	}

	private CompositeFunctionVariable createCompositeFunction(Expression condition) throws Exception {
		if (!condition.isCompositeCondition()) {
			return null;
		}

		Operator compositeOperator = condition.getOperator();
		List<PartedVariableHolder> compositeElements = new LinkedList<PartedVariableHolder>();

		for (AbstractOperand operand : condition.getOperands()) {
			/* All operands of a CompositeCondition are Expressions, not OperandImpls */
			if (operand instanceof Expression) {
				Expression operandExpression = (Expression) operand;
				compositeElements.add(convertConditionalStmt(operandExpression, false));
			}
		}

		return new CompositeFunctionVariable(compositeOperator, compositeElements.toArray(new PartedVariableHolder[compositeElements.size()]));
	}

	/**
	 * @param funcOperand  either Expression or inverted Operand to create a Function from
	 * @param isTransition flag marks whether the expression originates from transition
	 *                     (<code>true</code> value) or from condition (<code>false</code> value).
	 *                     Depending on this, <i>inversion</i> in expression is either ignored (in case
	 *                     of conditions), or treated as a Function (in case of transition).
	 * @return FunctionVariable which represents the specified operand
	 * @throws Exception cause {@link #convertOperandToVariable(AbstractOperand, Indices, boolean) cause }
	 */
	private FunctionVariable createFunction(AbstractOperand funcOperand, boolean isTransition) throws Exception {
		FunctionVariable functionVariable;

		if (funcOperand instanceof OperandImpl) {
			/* Check the operand to be inverted */
			if (!funcOperand.isInverted())
				throw new Exception("Function is being created from a non-inverted operand: " + funcOperand);
			/* Here isTransition was previously false. True cannot be, actually. todo: WTF? See CRC.vhd, transition of ips_xfr_wait */
			functionVariable = createInversionFunction(funcOperand, isTransition);

		} else if (funcOperand instanceof Expression) {
			Expression expression = (Expression) funcOperand;
			List<PartedVariableHolder> partiallySetVarList;
			if (expression.isInverted() && isTransition) {
				/* Create NOT function on the base of underlying function */
				/* NOT (REG1 AND REG2) --- in condition NOT should be ignored
														  (it's taken into account when obtaining
														   condition value)
								 * NOT (REG1 AND REG2) --- in transition NOT should be treated as a Function
								 * */
				functionVariable = createInversionFunction(expression, isTransition);

			} else if (!(partiallySetVarList = getPartiallySetVariablesFor(expression)).isEmpty()) {
				functionVariable = doCreateFinalFunction(Operator.CAT,
						partiallySetVarList.toArray(new PartedVariableHolder[partiallySetVarList.size()]));
			} else {
				/* Create and collect operands */
				PartedVariableHolder[] operandsHolders = new PartedVariableHolder[expression.getOperands().size()];
				if (isTransition) {
					/* Transition */
					int i = 0;
					for (AbstractOperand operand : expression.getOperands()) {
						operandsHolders[i++] = new PartedVariableHolder(convertOperandToVariable(operand, null,
								isTransition),
								operand.getPartedIndices());
					}

				} else {
					/* CONDITION */
					int i = 0;
					AbstractVariable operandVariable;
					Indices operandPartedIndices;
					for (AbstractOperand operand : expression.getOperands()) {
						operandPartedIndices = operand.getPartedIndices();
						if (operand instanceof Expression) {
							/* Treat operand as condition and extract dependent variable from it */
							PartedVariableHolder depVariableHolder = convertConditionalStmt((Expression) operand, false);
							operandVariable = getIdenticalVariable(depVariableHolder.getVariable());
							operandPartedIndices = depVariableHolder.getPartedIndices();
							/* If extracted dependent variable is OperandImpl or an inverted Expression,
														 * then take into account its value (e.g. cs_read='0' -> "cs_read" with true_value = 0).
														 * In Functions, if some operand is inversed or with true_value = 0, then all we can do
														 * is to substitute this operand with its INV function. */
							if (depVariableHolder.getTrueValue() != 1) {
								/* Create INV function */
								operandVariable = getIdenticalVariable(
										doCreateFinalInversionFunction(operandVariable, operandPartedIndices));
							}
						} else if (operand instanceof OperandImpl) {
							operandVariable = convertOperandToVariable(operand, null, false);
						} else throw new Exception("Unexpected situation while creating function: " +
								"operand is neither " + Expression.class.getSimpleName() +
								" nor " + OperandImpl.class.getSimpleName());
						operandsHolders[i++] = new PartedVariableHolder(operandVariable, operandPartedIndices);
					}

				}
				functionVariable = doCreateFinalFunction(expression.getOperator(), operandsHolders);

			}


		} else {
			throw new Exception("Unexpected situation while creating function: " +
					"funcOperand is neither " + Expression.class.getSimpleName() +
					" nor " + OperandImpl.class.getSimpleName());
		}


		/* Search for existent identical one */
		return (FunctionVariable) getIdenticalVariable(functionVariable);
	}

	private FunctionVariable doCreateFinalInversionFunction(AbstractVariable operandVariable, Indices operandPartedIndices) {
		/* Create Function */
		FunctionVariable invFunctionVariable = new FunctionVariable(Operator.INV, generateFunctionNameIdx(Operator.INV));
		/* Add a single operand */
		try {
			invFunctionVariable.addOperand(operandVariable, operandPartedIndices);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected exception: " +
					"Cannot add a single operand (" + operandVariable + ") to " + Operator.INV + " Function");
		}
		return invFunctionVariable;
	}

	private FunctionVariable doCreateFinalFunction(Operator operator, PartedVariableHolder... operandVariables) throws Exception {
		/* Create new Function Variable */
		FunctionVariable functionVariable = new FunctionVariable(operator, generateFunctionNameIdx(operator));
		/* Add operand variables one by one to the new Function Variable */
		for (PartedVariableHolder operandVarHolder : operandVariables) {
			try {
				functionVariable.addOperand(operandVarHolder.getVariable(), operandVarHolder.getPartedIndices());
			} catch (Exception e) {
				if (!e.getMessage().startsWith(FunctionVariable.FAILED_ADD_OPERAND_TEXT))
					throw e;
				/* If Function Variable cannot accept operands anymore, add this Function Variable (1) as an
								 * operand to the new Function Variable (2), replace the link of the Function Variable being
								 * created to the new Function Variable (2) and proceed with adding operands, this time - to
								 * the new Function Variable (2). */

				/* Tune so far created functionVariable (1) and add it to collector */
				tuneFunctionVariable(functionVariable);
				functionVariable = (FunctionVariable) getIdenticalVariable(functionVariable);
				/* Create new Function */
				FunctionVariable newFunction = new FunctionVariable(operator, generateFunctionNameIdx(operator));
				/* Make the previously created function an operand of the new one */
				newFunction.addOperand(functionVariable, null);
				/* Make the operand that failed to be added an operand of the new function */
				newFunction.addOperand(operandVarHolder.getVariable(), operandVarHolder.getPartedIndices());
				/* Change the link of functionVariable to the new one */
				functionVariable = newFunction;
			}
		}
		/* Tune functionVariable */
		tuneFunctionVariable(functionVariable);
		return functionVariable;
	}

	/**
	 * Method performs additional manipulations with function parameters.
	 * Manipulations include:
	 * <br>- Replacing DIVISION and MULTIPLICATION by power of 2 with SHIFTS
	 * <br>- Deciding between SIGNED and UNSIGNED modifications of the 4
	 * comparison operators: GT / U_GT, LT / U_LT, GE / U_GE, LE / U_LE.
	 *
	 * @param functionVariable variable to tune
	 * @throws Exception {@link #convertOperandToVariable(AbstractOperand, Indices, boolean)}.
	 */
	private void tuneFunctionVariable(FunctionVariable functionVariable) throws Exception {
		/* Replace DIVISION and MULTIPLICATION by power of 2 with SHIFTS */
		Operator operator = functionVariable.getOperator();
		List<PartedVariableHolder> operandHolders = functionVariable.getOperands();
		ValueAndIndexHolder powerOf2Constant;
		//todo: temporarily comment this for Toha (was if (false && ...))
		if (/*false && */(operator == Operator.MULT || operator == Operator.DIV)
				&& (powerOf2Constant = findPowerOf2Constant(operandHolders)) != null) {
			/* Substitute operator with SHIFT (mult -> SHIFT_LEFT, div -> SHIFT_RIGHT): */
			functionVariable.setOperator(operator == Operator.MULT ? Operator.SHIFT_LEFT : Operator.SHIFT_RIGHT);
			functionVariable.setNameIdx(generateFunctionNameIdx(functionVariable.getOperator()));
			/* Create SHIFT step operand */
			AbstractVariable shiftStepOpeVariable = variableManager.getConstantByValue(powerOf2Constant.value);
			/* Get the operand being shifted. Here assume that MULT and DIV have 2 operands only. */
			PartedVariableHolder shiftedOperandHolder = operandHolders.get(invertBit(powerOf2Constant.index));
			/* Set the shiftedOperand as left operand */
			functionVariable.setOperand(0, shiftedOperandHolder);
			/* Set the shiftStepOpeVariable as right operand */
			functionVariable.setOperand(1, new PartedVariableHolder(shiftStepOpeVariable, null));
		}

		/* Decide between SIGNED and UNSIGNED modifications of comparison operators */
		if (operandHolders.size() == 2 && !operandHolders.get(0).getVariable().isSigned()
				&& !operandHolders.get(1).getVariable().isSigned()) {
			Operator wasOperator = operator;
			if (operator == Operator.GT) {
				operator = Operator.U_GT;
			} else if (operator == Operator.LT) {
				operator = Operator.U_LT;
			} else if (operator == Operator.GE) {
				operator = Operator.U_GE;
			} else if (operator == Operator.LE) {
				operator = Operator.U_LE;
			}
			if (wasOperator != operator) {
				functionVariable.setOperator(operator);
				functionVariable.setNameIdx(generateFunctionNameIdx(operator));
			}
		}
	}

	/**
	 * Searches amongst the specified operands list for the first met
	 * {@link ConstantVariable} that's value is a power of 2.
	 * todo: Note that partedIndices are not taken into account here
	 *
	 * @param operandHolders list of Parted Variable Holders to search in
	 * @return value of the first met {@link ConstantVariable} that's
	 *         value is a power of 2, or <code>null</code> if no such a
	 *         {@link ConstantVariable} is fount in the specified list.
	 */
	private ValueAndIndexHolder findPowerOf2Constant(List<PartedVariableHolder> operandHolders) {
		for (int i = 0; i < operandHolders.size(); i++) {
			AbstractVariable variable = operandHolders.get(i).getVariable();
			if (variable instanceof ConstantVariable) {
				double power = Math.log10(((ConstantVariable) variable).getValue().doubleValue()) / Math.log10(2);
				if (power == ((int) power)) { // if the power is an INTEGER (i.e. the whole number, not a decimal)
					return new ValueAndIndexHolder(BigInteger.valueOf((long) power), i); /* ;*/
				}
			}
		}
		return null;
	}

	public ConstantVariable extractSubConstant(ConstantVariable baseConstant, Indices rangeToExtract) throws HLDDException {

		if (baseConstant == null) {
			return null;
		}
		if (rangeToExtract == null) {
			throw new IllegalArgumentException("null rangeToExtract is not allowed as an input parameter for SubConstant extraction");
		}

		ConstantVariable subConstant = baseConstant.subRange(rangeToExtract);

		return variableManager.getConstantByValue(subConstant.getValue(), subConstant.getLength());
	}

	public void concatenatePartialAssignments(AbstractVariable wholeVariable, List<OperandImpl> partialAssignments, boolean delay) throws Exception {
		/* Create CAT function */
		FunctionVariable catFunction = createCatFunction(partialAssignments);
		/* Make base variable tree contain 1 Terminal Node only: the CAT node */
		createAndReplaceNewGraph(wholeVariable, new Node.Builder(catFunction).build(), delay); // isDelay(varName) was at first "true".
	}

	/**
	 * @param oldGraphVariable old variable on the base of which to build the
	 *                         new one and what to replace with the new variable
	 * @param graphVarRootNode root node of the new variable
	 * @param isDelay		  Delay-flag to set to the new GraphVariable
	 * @return the newly created GraphVariable
	 */
	public GraphVariable createAndReplaceNewGraph(AbstractVariable oldGraphVariable, Node graphVarRootNode, boolean isDelay) {
		GraphVariable newGraphVariable = new GraphVariable(oldGraphVariable, graphVarRootNode);
		newGraphVariable.setDelay(isDelay);
		replaceWithIndices(oldGraphVariable, new PartedVariableHolder(newGraphVariable, null));
		return newGraphVariable;
	}

	private FunctionVariable createCatFunction(List<OperandImpl> partialSetList) throws Exception {
		/* Sort by index */
		Collections.sort(partialSetList, new OperandImplComparator());
		/* Create CAT Expression */
		Expression catExpression = new Expression(Operator.CAT, false);
		/* Add all inputs to the expression */
		for (OperandImpl partialSet : partialSetList) {
			catExpression.addOperand(partialSet);
		}
		/* Add the Expression to ModelCollector */
		return (FunctionVariable) convertOperandToVariable(catExpression, null, true);
	}

	private PartialAssignmentManager getPartialAssignmentManager() {
		if (partialAssignmentManager == null) {
			partialAssignmentManager = new PartialAssignmentManager(this);
		}
		return partialAssignmentManager;
	}

	public void collectPartialSettings(Architecture architecture) throws Exception {
		getPartialAssignmentManager().collectPartialSettings(architecture);
	}

	public boolean hasPartialAssignmentsIn(Process process) {
		return getPartialAssignmentManager().hasPartialAssignmentsIn(process);
	}

	public Map<String, Set<OperandImpl>> getPartialAssignmentsFor(Object astObject) {
		return getPartialAssignmentManager().getPartialAssignmentsFor(astObject);
	}

	public void finalizeAndCheckForCompleteness(Set<OperandImpl> partialSets, Indices wholeLength, String varName) throws Exception {
		PartialAssignmentManager.finalizeAndCheckForCompleteness(partialSets, wholeLength, varName);
	}

	public void splitPartiallyAssignedSignals(Set<ComponentInstantiation> components) throws Exception {
		getPartialAssignmentManager().splitPartiallyAssignedSignals(components);
	}

	private class OperandImplComparator implements Comparator<OperandImpl> {
		public int compare(OperandImpl o1, OperandImpl o2) {
			/* NB! Larger indices should be catted earlier, so swap the compared indices */
			return o2.getPartedIndices().compareTo(o1.getPartedIndices());
		}
	}

	private class ValueAndIndexHolder {
		private final BigInteger value;
		private final int index;

		public ValueAndIndexHolder(BigInteger value, int index) {
			this.value = value;
			this.index = index;
		}
	}

	private FunctionVariable createInversionFunction(AbstractOperand operand, boolean isTransition) throws Exception {
		/* Temporarily make the operand non-inversed to prevent
				 * infinite recursion and create corresponding variable */
		operand.setInverted(false);
		/* Create Function */
		FunctionVariable invFunctionVariable
				= doCreateFinalInversionFunction(convertOperandToVariable(operand, null, isTransition),
				operand.getPartedIndices());
		/* Restore initial inverted state */
		operand.setInverted(true);
		return invFunctionVariable;
	}


	/**
	 * <b>Note, that</b> the returned list (if it is not empty) contains
	 * instances of {@link base.hldd.structure.variables.GraphVariable} for
	 * what it is guaranteed that all the
	 * {@link base.hldd.structure.variables.GraphVariable#baseVariable}-s
	 * are instances of {@link base.hldd.structure.variables.PartedVariable}.
	 *
	 * @param expression where to extract PartedVariables from
	 * @return a <code>non-empty</code> list of
	 *         {@link base.hldd.structure.models.utils.PartedVariableHolder}-s, if the
	 *         specified expression is CAT with all its operands being
	 *         instances of
	 *         {@link base.hldd.structure.variables.PartedVariable} (all in all
	 *         --- a Complete Partially Set Variable). Otherwise an empty list
	 *         is returned.
	 */
	private List<PartedVariableHolder> getPartiallySetVariablesFor(Expression expression) {
		LinkedList<PartedVariableHolder> returnList = new LinkedList<PartedVariableHolder>();
		LinkedList<PartedVariableHolder> emptyList = new LinkedList<PartedVariableHolder>();
		if (expression.getOperator() != Operator.CAT) return emptyList;
		/* Check all operands to be PartedVariables */
		for (AbstractOperand operand : expression.getOperands()) {
			if (!(operand instanceof OperandImpl)) return emptyList;
			OperandImpl operandImpl = (OperandImpl) operand;
			if (!operandImpl.isParted()) return emptyList;
			/* Compose name of PartedVariable */
			Indices partedIndices = operandImpl.getPartedIndices();
			String varName = operandImpl.getName() + partedIndices;
			/* Obtain the PartedVariable (all parted GraphVariables have been set by this point) */
			AbstractVariable operandVariable = getVariable(varName);
			if (operandVariable != null && (operandVariable instanceof PartedVariable ||
					(operandVariable instanceof GraphVariable
							&& ((GraphVariable) operandVariable).getBaseVariable() instanceof PartedVariable))) {
				/* Provide NULL indices here, since they are already included in the base variable as PartedVariable. */
				returnList.add(new PartedVariableHolder(operandVariable, null));
			} else {
				return emptyList;
			}
		}
		return returnList;
	}

	/**
	 * Returns either an identical variable already residing in the collector
	 * or the variableToFind, previously adding it to collector.
	 * todo: internals of this method should be moved to VariableManager
	 *
	 * @param variableToFind variable to search for amongst existent variables
	 * @return an existent identical variable or the desired variable if an existent is not found
	 * @throws Exception cause {@link #addVariable(AbstractVariable) cause}
	 */
	AbstractVariable getIdenticalVariable(AbstractVariable variableToFind) throws Exception {

		if (variableToFind instanceof ConstantVariable) {
			/* Search amongst CONSTANTS */
			for (ConstantVariable constVariable : variableManager.getConstants()) {
				if (constVariable.isIdenticalTo(variableToFind)) {
					return constVariable;
				}
			}
		} else {
			/* Search amongst VARIABLES */
			for (AbstractVariable variable : variableManager.getVariables()) {
				if (variable.isIdenticalTo(variableToFind)) {
					return variable;
				}
			}
		}

		/* Identical variable was not found,
				* so add the desired variable to the variables and return it */
		addVariable(variableToFind);
		return variableToFind;
	}

	/**
	 * @param variable variable for which to calculate the number of conditions
	 * @return number of possible values of the variable
	 * @throws Exception if number of conditions is being calculated for a variable that doesn't belong
	 *                   neither to FunctionVariable nor to Variable class
	 */
	public int getConditionValuesCount(AbstractVariable variable) throws Exception {
		int order;
		if (variable.getClass() == FunctionVariable.class) {
			order = 1;
		} else if (variable.getClass() == Variable.class || variable.getClass() == GraphVariable.class) {
			Type varType = variable.getType();
			if (varType.isEnum()) {
				return varType.getCardinality();
			} else {
				return varType.getLength().deriveValueRange().getHighest() + 1;
//                return varType.getLength().length();
			}
//            order = variable.getLength().length()  /*variable.getHighestSB() + 1*/ ;
			//todo: Indices.deriveValueRange() may be helpfull here
		} else if (variable instanceof PartedVariable) { // todo: comment this part and add "variable instanceof PartedVariable" to previous
			variable.getLength().length();
			PartedVariable partedVariable = (PartedVariable) variable;
			order = partedVariable.getPartedIndices().length();
		} else throw new Exception("Number of conditions can be calculated " +
				"for " + FunctionVariable.class.getSimpleName() + ", " +
				Variable.class.getSimpleName() + " and " +
				GraphVariable.class.getSimpleName() + " only:" +
				"\nRequested variable: \n" + variable.toString());

		return (int) Math.pow(2, order);
	}

	public int getBooleanValueFromOperand(AbstractOperand abstractOperand) {
		return adjustBooleanCondition(1, abstractOperand.isInverted());
	}

	public static int adjustBooleanCondition(int booleanCondition, boolean inverted) {
		return inverted ? invertBit(booleanCondition) : booleanCondition;
	}

	public static int invertBit(int bit) {
		return bit == 0 ? 1 : 0;
	}

	/**
	 * Extracts a variable out of an <b>operand</b>.
	 * If it's a function, then creates it and adds to variables.
	 * Otherwise searches for it amongst internal variables.
	 *
	 * @param operand	  base operand to extract variable from
	 * @param targetLength desired length of the variable or <code>null<code> if doesn't matter.
	 *                     <b>NB!</b> This currently matters for {@link ConstantVariable}-s only.
	 * @param isTransition {@link #createFunction(base.vhdl.structure.AbstractOperand, boolean)}
	 * @return a ConstantVariable, FunctionVariable or Variable based on the <code>operand</code>
	 * @throws Exception if the <code>operand</code> contains an undeclared variable
	 */
	public AbstractVariable convertOperandToVariable(AbstractOperand operand, Indices targetLength, boolean isTransition) throws Exception {

		if (operand instanceof Expression || operand instanceof OperandImpl && operand.isInverted()) {

			return createFunction(operand, isTransition);

		} else if (operand instanceof OperandImpl) {
			String variableName = ((OperandImpl) operand).getName();
			BigInteger constantValue = PackageParser.parseConstantValue(variableName);
			if (constantValue != null) {
				/* Get CONSTANT by VALUE*/
				//todo: Jaan: different constants for different contexts
				return variableManager.getConstantByValue(constantValue,
						operand.getLength() != null ? operand.getLength() : targetLength);

			} else {
				/* Get VARIABLE by NAME */
				AbstractVariable variable = variableManager.getVariableByName(variableName);
				if (variable != null) return variable;
				/* Get CONSTANT by NAME */
				ConstantVariable constant = variableManager.getConstantByName(variableName);
				if (constant != null) return constant;
			}
			throw new Exception("Undeclared VARIABLE (" + variableName + ") is used in the following operand: " + operand);

		} else if (operand instanceof UserDefinedFunction) {
			UserDefinedFunction udFunction = (UserDefinedFunction) operand;
			String udFunctionName = udFunction.getUserDefinedFunction();
			List<AbstractOperand> udFunctionOperands = udFunction.getOperands();
			// Create User Defined Function Variable
			UserDefinedFunctionVariable udFunctionVariable =
					new UserDefinedFunctionVariable(udFunctionName,
							generateUDFunctionNameIdx(udFunctionName),
							udFunctionOperands.size(), operand.getLength());
			// Create and add operands to UD Function Variable
			for (AbstractOperand udFunctionOperand : udFunctionOperands) {
				udFunctionVariable.addOperand(convertOperandToVariable(udFunctionOperand, null, true), udFunctionOperand.getPartedIndices());
			}
			// Search for identical and add variable to collector if needed
			return getIdenticalVariable(udFunctionVariable);

		} else if (operand == null) {
			return null;
		}
		throw new Exception("Obtaining VHDL structure variables from " + operand.getClass().getSimpleName() +
				"instances is not supported");
	}

	public Condition convertOperandsToCondition(OperandImpl[] conditionOperands) {
		int[] values = new int[conditionOperands.length];
		for (int i = 0; i < conditionOperands.length; i++) {
			String operandName = conditionOperands[i].getName();

			ConstantVariable constant = variableManager.getConstantByName(operandName);
			values[i] = constant != null ? constant.getValue().intValue() : PackageParser.parseConstantValue(operandName).intValue();
		}
		return Condition.createCondition(values);
	}

	/**
	 * Generates a name for function variables ADDER, MULT, DIV etc
	 * and user defined functions like f_ComputeCrc16() in crc.vhd.
	 *
	 * @param operator type of function (ADDER, MULT, DIV etc)
	 * @return the order number of the function
	 */
	private int generateFunctionNameIdx(Operator operator) {
		return deriveNextIdx(variableManager.getFunctions(operator));
	}

	private int generateUDFunctionNameIdx(String userDefinedOperator) {
		return deriveNextIdx(variableManager.getUDFunctions(userDefinedOperator));
	}

	private int deriveNextIdx(Collection<FunctionVariable> functions) {
		int largestIndexUsed = -1;
		for (FunctionVariable functionVariable : functions) {
			int nameIndex = functionVariable.getNameIdx();
			if (nameIndex > largestIndexUsed) {
				largestIndexUsed = nameIndex;
			}
		}
		return largestIndexUsed == -1 ? 1 : largestIndexUsed + 1;
	}

	public Collection<ConstantVariable> getConstants() {
		return variableManager.getConstants();
	}

	public Collection<AbstractVariable> getVariables() {
		return variableManager.getVariables();
	}

	public AbstractVariable getVariable(String variableName) {
		return variableManager.getVariableByName(variableName);
	}

	public ConstantVariable getConstant(String constantName) {
		return variableManager.getConstantByName(constantName);
	}

	/* AUXILIARY classes */

	public class CompositeFunctionVariable extends FunctionVariable {
		private Operator compositeOperator;
		private PartedVariableHolder[] functionVariables;

		public CompositeFunctionVariable(Operator compositeOperator, PartedVariableHolder... functionVariables) {
			this.compositeOperator = compositeOperator;
			this.functionVariables = functionVariables;
		}

		public Operator getCompositeOperator() {
			return compositeOperator;
		}

		public PartedVariableHolder[] getFunctionVariables() {
			return functionVariables;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < functionVariables.length; i++) {
				sb.append(functionVariables[i]).append("\n");
				if (i < functionVariables.length - 1) {
					sb.append(compositeOperator).append("\n");
				}
			}
			return sb.toString();
		}
	}

}
