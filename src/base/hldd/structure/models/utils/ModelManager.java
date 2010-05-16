package base.hldd.structure.models.utils;

import base.HLDDException;
import base.hldd.structure.variables.*;
import base.hldd.structure.variables.Variable;
import base.hldd.visitors.DependentVariableReplacer;
import base.vhdl.structure.*;
import base.Indices;
import base.Type;

import java.util.*;
import java.math.BigInteger;

import parsers.vhdl.OperandLengthSetter;
import parsers.vhdl.PackageParser;

/**
 * <br><br>User: Anton Chepurov
* <br>Date: 12.02.2008
* <br>Time: 8:20:59
*/
public class ModelManager {
    private VariableManager variableManager;
    private final boolean useSameConstants;

    public ModelManager(boolean useSameConstants) {
        this.useSameConstants = useSameConstants;
        variableManager = new VariableManager();
    }

    public void addVariable(AbstractVariable newVariable) throws Exception {
        variableManager.addVariable(newVariable);
    }

    public void addVariable(String varName, AbstractVariable newVariable) {
        variableManager.addVariable(varName, newVariable);
    }

    public void removeVariable(AbstractVariable variableToRemove) {
        variableManager.removeVariable(variableToRemove);
    }

    public void replace(AbstractVariable variableToReplace, AbstractVariable replacingVariable) throws Exception {
        if (variableToReplace instanceof ConstantVariable) {
            throw new Exception("ConstantVariable cannot be replaced currently. Implementation is simply missing.");
        }
        if (variableToReplace instanceof GraphVariable && replacingVariable instanceof Variable) {
			/* todo: this situation never occurs, because replace() is only called with replacingVariable being instanceof GraphVariable */
            /* Remove old variable from hash */
            removeVariable(variableToReplace);
            /* Remove replacing variable from hash */
            removeVariable(replacingVariable);

            /* Replace base variable */
            ((GraphVariable) variableToReplace).replaceBaseVariable(replacingVariable);
            /* ReAdd updated old variable to hash */
            addVariable(variableToReplace);
        } else {
            /* Remove old variable from hash */
            removeVariable(variableToReplace);
            /* Add new variable to hash */
            addVariable(replacingVariable);

            /* Replace old variable with new one in Functions and GraphVariables */
            for (AbstractVariable absVariable : variableManager.getVariables()) {
                if (absVariable instanceof FunctionVariable) {
                    FunctionVariable functionVariable = (FunctionVariable) absVariable;
                    for (PartedVariableHolder operand : functionVariable.getOperands()) {
                        if (operand.getVariable() == variableToReplace) {
                            operand.setVariable(replacingVariable);
                        }
                    }
                } else if (absVariable instanceof GraphVariable) {
                    GraphVariable graphVariable = (GraphVariable) absVariable;
                    graphVariable.traverse(new DependentVariableReplacer(variableToReplace, replacingVariable));
                }
            }
        }

    }

    public AbstractVariable[] getFutureGraphVars() {
        ArrayList<AbstractVariable> varList = new ArrayList<AbstractVariable>();

        for (AbstractVariable variable : variableManager.getVariables()) {
            if (!variable.isInput()) {
                varList.add(variable);
            }
        }

        return varList.toArray(new AbstractVariable[varList.size()]);
    }

    /**
     * Extracts a boolean dependent variable out of an <b>expression</b>.
     * If it's a function, then creates it and adds to variables.
     * Otherwise searches for it amongst internal variables.
     *
     * @param expression
     * @param expandCondition
     * @return an instance of
     *         {@link base.hldd.structure.models.utils.PartedVariableHolder}
     * @throws Exception If an undeclared variable is used within the expression
     *          <br>{@link #createConditionalFunction(base.vhdl.structure.Expression, boolean) cause2}
     */
    public PartedVariableHolder getBooleanDependentVariable(Expression expression, boolean expandCondition) throws Exception {

        FunctionVariable additionalFunction = createConditionalFunction(expression, expandCondition);
        if (additionalFunction != null) return new PartedVariableHolder(
                additionalFunction, null, getConditionValue(additionalFunction, expression));
        else {
            //todo: replace Boolean expression (SOME_BOOLEAN) with artificial condition, in VHDLStructureBuilder. 
            List<AbstractOperand> operands = expression.getOperands();
            if (operands.size() != 2) throw new Exception("Error during extraction of DEPENDENT VARIABLE:" +
                    "\nSupported number of operands is 2. Received number of operands: " + operands.size() +
                    "\nExpression: " + expression);
            AbstractVariable dependentVariable = convertOperandToVariable(operands.get(0), null, false);
            Indices partedIndices = operands.get(0).getPartedIndices();
            return new PartedVariableHolder(dependentVariable, partedIndices, getConditionValue(dependentVariable, expression));
        }

    }

    public PartedVariableHolder extractBooleanDependentVariable(AbstractOperand abstractOperand, boolean useComposites) throws Exception {
        if (abstractOperand instanceof Expression) {
            return getBooleanDependentVariable((Expression) abstractOperand, useComposites);
        } else if (abstractOperand instanceof OperandImpl) {
            OperandImpl operand = (OperandImpl) abstractOperand;
            return new PartedVariableHolder(getVariable(operand.getName()), operand.getPartedIndices(), getBooleanValueFromOperand(operand));
        }
        throw new Exception("Dependent variable is being extracted from : " + abstractOperand +
                "\nCurrently extraction of Dependent Variables is only supported for " +
                Expression.class.getSimpleName() + " and " + OperandImpl.class.getSimpleName());
    }

    /**
     *
     * @param condition
     * @param expandCondition
     * @return  a FunctionVariable representing the given <code>condition</code>,
     *          or <code>null</code> if FunctionVariable is not needed to represent it
     * @throws Exception  Causes: {@link #convertOperandToVariable(AbstractOperand, Indices, boolean) cause1 }
     *                      {@link #getIdenticalVariable(AbstractVariable) cause2 }
     */
    private FunctionVariable createConditionalFunction(Expression condition, boolean expandCondition) throws Exception {

        /* Check the expression to be a CONDITION */
        Operator operator = condition.getOperator();
        if (operator.isCondition()) {
            if (condition.isReducedCompositeCondition()) {
                /* Imitate a CompositeCondition and pass it to create a CompositeFunction */
                return createCompositeFunction(recreateCompositeCondition(condition));
            }
            /* Check if a new FUNCTION must be created */
            else if (isFunctionRequired(condition/*operator, leftVariable, rightVariable, leftVarIndices*/)) {

                return createFunction(condition, false);

            } else return null;

        } else {

            /* Check for AND/OR composite conditions, if allowed */
            if (expandCondition) {
                if (condition.isCompositeCondition()) {
                    /* Create CompositeFunctionVariable */
                    return createCompositeFunction(condition);

                }
            }
            if (condition.getOperator().isLogical() /*isFunctionRequired(condition)*/) {
                /* todo: START experimental 16.10.2008 :  CompositeConditions are checked before checking to create Function from unConditional expression */
                return createFunction(condition, false);
                /* todo: END   experimental 16.10.2008 :  CompositeConditions are checked before checking to create Function from unConditional expression */

            } else return createFunction(condition, false);
            /*throw new Exception("Cannot create Conditional function from a non-conditional operator: " + operator +
                        "\nCondition: " + condition)*/
        }
    }

    /**
     * <b>NB! </b> Method expects a reducedCompositeCondition ({@link Expression#isReducedCompositeCondition()}). Otherwise the
     * method may fail.
     *
     * @param reducedCompCondition an Expression that is {@link Expression#isReducedCompositeCondition()}
     * @return an extendended CompositeCondition
     */
    private Expression recreateCompositeCondition(Expression reducedCompCondition) {
        Operator reducedOperator = reducedCompCondition.getOperator();

        List<AbstractOperand> operands = reducedCompCondition.getOperands();
        AbstractOperand reducedRightOperand = operands.get(1);
        Expression leftOperand = (Expression) operands.get(0);
        Operator compositeOperator = leftOperand.getOperator();
        Expression compConditionExpr = new Expression(compositeOperator, reducedCompCondition.isInverted());
        for (AbstractOperand leftExprOperand : leftOperand.getOperands()) {
            /* Create an expanded expression out of a reduced one */
            Expression expandedExpression = new Expression(reducedOperator, leftExprOperand.isInverted());
            expandedExpression.addOperand(leftExprOperand);
            expandedExpression.addOperand(reducedRightOperand); //todo: clone() may be needed

            /* Add Expanded Expression to the Composite Expression */
            compConditionExpr.addOperand(expandedExpression);
        }

        return compConditionExpr;
    }

    private CompositeFunctionVariable createCompositeFunction(Expression condition) throws Exception {
        Operator compositeOperator = condition.getOperator();
        List<PartedVariableHolder> compositeElements = new LinkedList<PartedVariableHolder>();

        for (AbstractOperand operand : condition.getOperands()) {
            /* All operands of a CompositeCondition are Expressions, not OperandImpls */
            if (operand instanceof Expression) {
                Expression operandExpression = (Expression) operand;
                compositeElements.add(getBooleanDependentVariable(operandExpression, false));
            }
        }

        return new CompositeFunctionVariable(compositeOperator, compositeElements.toArray(new PartedVariableHolder[compositeElements.size()]));
    }

    private boolean isFunctionRequired(Expression expression/*Operator operator, AbstractVariable leftVariable, AbstractVariable rightVariable, int[] leftVarIndices*/) throws Exception {
        doSetLengthFor(expression);

        /* If expression is CONDITIONAL, apart from EQ, then an additional FUNCTION must be created. */
        Operator operator = expression.getOperator();
        if (operator.isCondition() && operator != Operator.EQ)
            return true;

        /*  If EQ CONDITION VARIABLE is longer than 1 bit, then it is a FUNCTION (not a NODE), since otherwise CASE condition would be used.
            So, if condition variable is very large (long register with multiple possible values),
            an additional FUNCTION must be created (previously, check for existent ones)
            (Simple CONTROL node cannot be created easily, if a register is long and thus has many values... ) */
        else if (operator == Operator.EQ) {
            /* Get expression children. Process only 2 of them */
            List<AbstractOperand> operands = expression.getOperands();
            if (operands.size() != 2) throw new Exception("Error during creation of CONDITIONAL FUNCTION:" +
                    "\nSupported number of operands is 2. Received number of operands: " + operands.size() +
                    "\nExpression: " + expression);
            //todo: What if operands are in the opposite order?...
            /* Get 1st Variable and its parted indices */
            AbstractVariable leftVariable = convertOperandToVariable(operands.get(0), null, false);
            AbstractOperand leftOperand = operands.get(0);
            /* Get 2nd Variable */
            AbstractVariable rightVariable = convertOperandToVariable(operands.get(1), leftVariable.getLength(), false);

            if (leftOperand.isParted()) {
                Indices partedIndices = leftOperand.getPartedIndices();
                // If a PART of variable is used
                if (partedIndices.length() > 1 || !(rightVariable instanceof ConstantVariable)) {
                    // If more than 1 bit of a variable is used, or a comparison with a variable
                    return true;
                }
            } else if (leftVariable.getLength().getHighest() > 0/*vars.get(condition.getLeftOperand().getVariable()).getHighestIndex() > 0*/)
                return true;
            else if (!(rightVariable instanceof ConstantVariable)) /* If right operand is also a variable */ //todo: remove left-right separation.
                return true;
        }
        // todo: Note! Block below ("if (!operator.isCondition())") is substituted with Operator.isLogical()
//        /* todo: START experimental 16.10.2008 :  CompositeConditions are checked before checking to create Function from unCondition expression */
//        /* Possible CompositeConditions have been created to this moment. */
//        /* todo: Here Logical operators count: XOR, OR, AND. May be differentiate them  */
//        else if (!operator.isCondition()) {
//            return true;
//        }
//        /* todo: END   experimental 16.10.2008 :  CompositeConditions are checked before checking to create Function from unCondition expression */

        return false;
    }

    /**
     *
     * todo: Add desiredHighestSB parameter to createFunction(). And to createInversionFunction().
     * todo: This is both to solve problem of ConstantVariable-s (search below for "Problem: if one")
     * todo: and UserDefinedFunctionVariable, which possibly may be obtained (returned) from convertOperandToVariable()
     * todo: wherever this method is called.
     * todo: NB! before using desiredHighestSB, call extractOperatorImposedHighestSB(operator, operands) to find
     * todo: whether operator imposes a constraint on the highestSB.
     * todo: extractOperatorImposedHighestSB scans operands in search for the one with fixed(!) highestSB(Variable
     * todo: or GraphVariable(! consider what are fixed)) and depending on the operator return the fixed highestSB
     * todo: if the operator imposes a constraint (AND/OR/etc)or return null, if operator hasn't got any constraints(
     * todo: CAT). 
     *
     * todo: May be change isFunctionRequired() method to accept all AbstractOperands, not only Expressions,
     * todo: and use isFunctionRequired() in convertOperandToVariable() at the very beginning.
     *
     * @param funcOperand either Expression or inverted Operand to create a Function from
     * @param isTransition flag marks whether the expression originates from transition
     *        (<code>true</code> value) or from condition (<code>false</code> value).
     *        Depending on this, <i>inversion</i> in expression is either ignored (in case
     *        of conditions), or treated as a Function (in case of transition).
     * @return
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
                /* Store lengths for CONSTANTS */
                doSetLengthFor(expression);
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
                            PartedVariableHolder depVariableHolder = getBooleanDependentVariable((Expression) operand, false);
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
        FunctionVariable invFunctionVariable =
                new FunctionVariable(Operator.INV, generateFunctionName(Operator.INV.name()));
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
        FunctionVariable functionVariable = new FunctionVariable(operator, generateFunctionName(operator.name()));
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
                FunctionVariable newFunction = new FunctionVariable(operator, generateFunctionName(operator.name()));
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
     *  comparison operators: GT / U_GT, LT / U_LT, GE / U_GE, LE / U_LE.
     *
     * @param functionVariable variable to tune
     * @throws Exception {@link #convertOperandToVariable(AbstractOperand, Indices, boolean)}.
     */
    private void tuneFunctionVariable(FunctionVariable functionVariable) throws Exception {
        /* Replace DIVISION and MULTIPLICATION by power of 2 with SHIFTS */
        Operator operator = functionVariable.getOperator();
        List<PartedVariableHolder> operandHolders = functionVariable.getOperands();
        ValueAndIndexHolder powerOf2Constant = null;
        //todo: temporarily comment this for Toha (was if (false && ...))
        if (/*false && */(operator == Operator.MULT || operator == Operator.DIV)
                && (powerOf2Constant = findPowerOf2Constant(operandHolders)) != null) {
            /* Substitute operator with SHIFT (mult -> SHIFT_LEFT, div -> SHIFT_RIGHT): */
            functionVariable.setOperator(operator == Operator.MULT ? Operator.SHIFT_LEFT : Operator.SHIFT_RIGHT);
            functionVariable.setName(generateFunctionName(functionVariable.getOperator().name()));
            /* Create SHIFT step operand */
            AbstractVariable shiftStepOpeVariable =
                    ConstantVariable.getConstByValue(powerOf2Constant.value, null, variableManager.getConsts(), useSameConstants);
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
                functionVariable.setName(generateFunctionName(operator.name()));
            }
        }
    }

    /**
     * Searches amongst the specified operands list for the first met
     * {@link ConstantVariable} that's value is a power of 2.
     * todo: Note that partedIndices are not taken into account here
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
		
		ConstantVariable subConstant = baseConstant.subRange(rangeToExtract);
		
		return ConstantVariable.getConstByValue(subConstant.getValue(), subConstant.getLength(), variableManager.getConsts(), useSameConstants);
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
            if (operandVariable == null || !(operandVariable instanceof GraphVariable)
                    || !(((GraphVariable) operandVariable).getBaseVariable() instanceof PartedVariable)) return emptyList;
            /* Provide NULL indices here, since they are already included in the base variable as PartedVariable. */
            returnList.add(new PartedVariableHolder(operandVariable, null/*operand.getPartedIndices()*/));
        }
        return returnList;
    }

    /**
     * Returns either an identical variable already residing in the collector
     * or the variableToFind, previously adding it to collector. 
     *
     * @param variableToFind variable to search for amongst existent variables
     * @return an existent identical variable or the desired variable if an existent is not found
     * @throws Exception cause {@link #addVariable(AbstractVariable) cause}
     */
    private AbstractVariable getIdenticalVariable(AbstractVariable variableToFind) throws Exception {

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
     *
     * @param variable variable for which to calculate the number of conditions
     * @return number of possible values of the variable
     * @throws  Exception if number of conditions is being calculated for a variable that doesn't belong
     *          neither to FunctionVariable nor to Variable class
     */
    public int getConditionValuesCount(AbstractVariable  variable) throws Exception {
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

    /**
     *
     * @param dependentVariable variable that is generated from the <code>expression</code>
     * @param expression base expression where the <code>dependentVariable</code> has been generated from
     * @return value of the <code>dependentVariable</code> in the given <code>expression</code>
     * @throws  Exception if expression value is being obtained from a variable
                that is not an instance of neither FunctionVariable nor Variable class
     */
    public int getConditionValue(AbstractVariable dependentVariable, Expression expression) throws Exception {

        if (dependentVariable.getClass() == FunctionVariable.class) {
            return getBooleanValueFromOperand(expression);
        }
        if (dependentVariable.getClass() == Variable.class || dependentVariable.getClass() == GraphVariable.class) {
            List<AbstractOperand> operands = expression.getOperands();
            if (operands.size() != 2) throw new Exception("Error during extraction of CONDITION VALUE:" +
                    "\nSupported number of operands is 2. Received number of operands: " + operands.size() +
                    "\nExpression: " + expression);
            /* Analyze 2nd operand */ //todo: if the order of operands is changed into an opposite one?... 
            AbstractVariable valueVariable = convertOperandToVariable(operands.get(1), dependentVariable.getLength(), false);
            if (!(valueVariable instanceof ConstantVariable)) {
                throw new Exception("Error during extraction of CONDITION VALUE:" +
                        "\nUndeclared variable (" + valueVariable + ") used in the following expression: " + expression.toString());
            }
            int initialBit = ((ConstantVariable) valueVariable).getValue().intValue();
            return getBooleanValueFromOperand(expression, initialBit);
        } else if (dependentVariable.getClass() == CompositeFunctionVariable.class) {
            /* For both AND and OR return 1 */
            return 1;
        } else throw new Exception("Condition value can be obtained for " +
                FunctionVariable.class.getSimpleName() + ", " +
                Variable.class.getSimpleName() + " and " +
                GraphVariable.class.getSimpleName() + " only:" +
                "\nRequested dependent variable: \n" + dependentVariable);

    }

    public int getBooleanValueFromOperand(AbstractOperand abstractOperand) {
        return getBooleanValueFromOperand(abstractOperand, 1);
    }


    public static int getBooleanValueFromOperand(AbstractOperand abstractOperand, int trueBit) {
        return abstractOperand.isInverted() ? invertBit(trueBit) : trueBit;
    }

    public static int adjustBooleanCondition(int booleanConditon, boolean isInversed) {
        return isInversed ? invertBit(booleanConditon) : booleanConditon;
    }

    public static int invertBit(int bit) {
        return bit == 0 ? 1 : 0;
    }

    /**
     * Extracts a variable out of an <b>operand</b>.
     * If it's a function, then creates it and adds to variables.
     * Otherwise searches for it amongst internal variables.
     *
     * @param operand base operand to extract variable from
     * @param targetLength desired length of the variable or <code>null<code> if doesn't matter.
     *                          <b>NB!</b> This currently matters for {@link ConstantVariable}-s only.
     * @param isTransition {@link #createFunction(base.vhdl.structure.AbstractOperand, boolean)}
     * @return a ConstantVariable, FunctionVariable or Variable based on the <code>operand</code>
     * @throws Exception if the <code>operand</code> contains an undeclared variable
     */
    public AbstractVariable convertOperandToVariable(AbstractOperand operand, Indices targetLength, boolean isTransition) throws Exception {
        doSetLengthFor(operand);

        if (operand instanceof Expression || operand instanceof OperandImpl && operand.isInverted()) {

            return createFunction(operand, isTransition);

        } else if (operand instanceof OperandImpl) {
            String variableName = ((OperandImpl) operand).getName();
            BigInteger constantValue = PackageParser.parseConstantValue(variableName);
            if (constantValue != null) {
                /* Get CONSTANT by VALUE*/
                //todo: Jaan: different constants for different contexts
                return ConstantVariable.getConstByValue(constantValue,
                        operand.getLength() != null ? operand.getLength() : targetLength, variableManager.getConsts(), useSameConstants);

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
                            generateFunctionName(udFunctionName),
                            udFunctionOperands.size(), operand.getLength());
            // Create and add operands to UD Function Variable
            for (AbstractOperand udFunctionOperand : udFunctionOperands) {
                udFunctionVariable.addOperand(convertOperandToVariable(udFunctionOperand, null, true), udFunctionOperand.getPartedIndices());
            }
            // Search for identical and add variable to collector if needed
            return getIdenticalVariable(udFunctionVariable);
        }
        throw new Exception("Obtaining VHDL structure variables from " + operand.getClass().getSimpleName() +
                "instances is not supported");
    }

    /**
	 * todo: remove this method. use a separate visitor in VHDL preprocessing. consider: v_out <= "0000"; // v_out may be 3:0 and may be 0:3
     * Sets all the lengths for the specified operand and its sub-operands.
     * @param operand to set length for
     * @throws Exception {@link parsers.vhdl.OperandLengthSetter#OperandLengthSetter(ModelManager , AbstractOperand)}.
     */
    private void doSetLengthFor(AbstractOperand operand) throws Exception {
        if (operand.getLength() == null) {
            new OperandLengthSetter(this, operand);
        }
    }

    public int[] getConditionValues(OperandImpl[] conditionOperands) {
        int[] values = new int[conditionOperands.length];
        for (int i = 0; i < conditionOperands.length; i++) {
            String operandName = conditionOperands[i].getName();

            ConstantVariable constant = variableManager.getConstantByName(operandName);
            values[i] = constant != null ? constant.getValue().intValue() : PackageParser.parseConstantValue(operandName).intValue();
        }
        return values;
    }

    /**
     * Generates a name for function variables ADDER, MULT, DIV etc
     * and user defined functions like f_ComputeCrc16() in crc.vhd.
     *
     * @param operatorName type of function (ADDER, MULT, DIV etc)
     * @return the name of the function of type "XXXX_Y", where XXXX
     *         is ADDER/MULT/DIV and Y is an order number
     */
    private String generateFunctionName(String operatorName) {

        // Go through all variables and look for the largest NAME INDEX used amongst FunctionVariables of the required type
        int largestIndexUsed = -1;
        for (AbstractVariable variable : variableManager.getVariables()) {
            if (variable instanceof FunctionVariable) {
                FunctionVariable functionVariable = (FunctionVariable) variable;
                if (isRequiredTypeFunction(functionVariable, operatorName) ) {
                    // if the INDEX of the function is larger, than previously read - save it to the largestFunctionIndex
                    if (functionVariable.getNameIndex() > largestIndexUsed)
                        largestIndexUsed = functionVariable.getNameIndex();
                }
            }
        }

        return largestIndexUsed == -1 ? operatorName + "____1" : operatorName + "____" + (largestIndexUsed + 1);
    }

    private boolean isRequiredTypeFunction(FunctionVariable functionVariable, String operatorName) {
        if (functionVariable instanceof UserDefinedFunctionVariable) {
            return ((UserDefinedFunctionVariable) functionVariable).getUserDefinedOperator().equals(operatorName);
        } else {
            Operator opNameOperator;
            try {
                opNameOperator = Operator.valueOf(operatorName);
            } catch (IllegalArgumentException e) {
                return false;
            }
            return functionVariable.getOperator() == opNameOperator;
        }
    }

    public ConstantVariable[] getConstants() {
        return variableManager.getConstantsAsArray();
    }

    public AbstractVariable[] getVariables() {
        return variableManager.getVariablesAsArray();
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
