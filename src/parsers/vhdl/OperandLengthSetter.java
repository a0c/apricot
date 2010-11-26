package parsers.vhdl;

import base.Range;
import base.vhdl.structure.*;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.ConstantVariable;
import base.hldd.structure.models.utils.ModelManager;
import base.helpers.ExceptionSolver;

import java.util.List;

/**
 * Calculates and sets lengths for all operands within the one specified as constructor parameter.
 * <p/>
 * Algorithm:<br>
 * <b>1)</b> If the operand is a <b>leaf operand</b> ({@link OperandImpl} or {@link UserDefinedFunction}), then its
 * <i>VHDL typing</i> is analyzed:<br>
 * *  - <u>range</u> are taken into account;<br>
 * *  - <u>VHDL constants</u> are parsed depending on <u>radix</u> (HEX, BOOLEAN and DECIMAL). If VHDL
 * constant typing defines a fixed length, then it is used.<br>
 * *  - length for {@link UserDefinedFunction} is extracted from the <u>package file</u> where the function
 * is defined.<br><br>
 * <b>2)</b> If the operand is an <b>{@link Expression}</b>, then:<br>
 * *  - its length is calculated using {@link Operator#adjustLength(Range , Range , Range)};<br>
 * *  - its <u>operator</u> is analyzed for imposing constraints to the lengths of Expression operands. This
 * is used for those constants, that don't have their length fixed in VHDL typing.
 * <p/>
 * The set length is used mainly when creating {@link base.hldd.structure.variables.ConstantVariable}-s
 * and {@link base.hldd.structure.variables.UserDefinedFunctionVariable UserDefinedFunctionVariable}-s.
 *
 * @author Anton Chepurov
 */
public class OperandLengthSetter {
	public static final String UNDECLARED_VARIABLE_TEXT = "Cannot calculate length for the following operand: ";

	private final ModelManager modelCollector;

	/**
	 * @param modelCollector to use for obtaining lengths of variables and named constants
	 * @param operand		the root operand to calculate (set) length for
	 * @throws Exception {@link #calculate(AbstractOperand, AbstractOperand)}.
	 */
	public OperandLengthSetter(ModelManager modelCollector, AbstractOperand operand) throws Exception {
		this.modelCollector = modelCollector;
		calculate(operand, null);
	}

	/**
	 * @param operand	   to calculate length for
	 * @param parentOperand of the operand
	 * @throws Exception if undeclared Variable is met during calculation
	 */
	private void calculate(AbstractOperand operand, AbstractOperand parentOperand) throws Exception {
		if (operand instanceof Expression) {
			/* Calling recursion:
			* Dealing with EXPRESSION operands. */
			/* Here both
			* 1) calculate and store fixed length for the Expression and
			* 2) calculate and store operator imposed length, when it gets available. */
			while (!isLengthFixedFor(operand)) {
				Expression expression = (Expression) operand;
				Operator operator = expression.getOperator();
				List<AbstractOperand> operands = expression.getOperands();
				for (AbstractOperand childOperand : operands) {
					/* Evaluate childOperand */
					calculate(childOperand, operand);
					/* Try to obtain Operator imposed length */
					storeImposedLength(expression);
				}
				/* If all children have fixed lengths, then
				* calculate and store fixed length for the Expression itself. */
				if (areAllChildrenFixed(operands)) {
					/* Calculate */
					Range length = null;
					for (AbstractOperand childOperand : operands) {
						length = operator.adjustLength(length,
								childOperand.getLength(), childOperand.getRange());
					}
					/* Store */
					operand.setLength(length);
				}
			}
		} else {

			/* Terminating recursion:
			* Dealing with LEAF operands.
			* Here store fixed length for the LEAF operand,
			* either directly obtained length or obeying the operator imposed length. */
			if (operand instanceof OperandImpl) {
				/* Try to obtain constant value: */
				String variableName = ((OperandImpl) operand).getName();
				ConstantValueAndLengthHolder valAndLenHolder
						= PackageParser.parseConstantValueWithLength(variableName);
				if (valAndLenHolder == null) {
					/* Operand doesn't declare a constant. Search for it amongst vars. */
					AbstractVariable variable = modelCollector.getVariable(variableName);
					if (variable == null) variable = modelCollector.getConstant(variableName);
					if (variable == null) {
						if (variableName.equalsIgnoreCase("C_ADDR_OFFS_CRC_CFG")) {
							ConstantVariable constantVariable = new ConstantVariable(variableName, new java.math.BigInteger("0"));
							constantVariable.setLength(new Range(1, 0));
							modelCollector.addVariable(constantVariable);
							variable = constantVariable;
						} else if (variableName.equalsIgnoreCase("C_ADDR_OFFS_CRC_INP")) {
							ConstantVariable constantVariable = new ConstantVariable(variableName, new java.math.BigInteger("1"));
							constantVariable.setLength(new Range(1, 0));
							modelCollector.addVariable(constantVariable);
							variable = constantVariable;
						} else if (variableName.equalsIgnoreCase("C_ADDR_OFFS_CRC_CSTAT")) {
							ConstantVariable constantVariable = new ConstantVariable(variableName, new java.math.BigInteger("2"));
							constantVariable.setLength(new Range(1, 0));
							modelCollector.addVariable(constantVariable);
							variable = constantVariable;
						} else if (variableName.equalsIgnoreCase("C_ADDR_OFFS_CRC_OUTP")) {
							ConstantVariable constantVariable = new ConstantVariable(variableName, new java.math.BigInteger("3"));
							constantVariable.setLength(new Range(1, 0));
							modelCollector.addVariable(constantVariable);
							variable = constantVariable;
						} else {
							// todo: commented for DEMO
							Exception exception = new Exception(UNDECLARED_VARIABLE_TEXT + operand
									+ "\nUndeclared VARIABLE (" + variableName + ") is met.");
							Object solution = ExceptionSolver.getInstance().findSolution(exception.getMessage(), ExceptionSolver.SolutionOptions.VALUE);
							if (solution.getClass() == ConstantValueAndLengthHolder.class) {
								ConstantValueAndLengthHolder constValHolder = (ConstantValueAndLengthHolder) solution;
								ConstantVariable constantVariable = new ConstantVariable(variableName,
										constValHolder.getValue());
								constantVariable.setLength(constValHolder.getDesiredLength());
								modelCollector.addVariable(constantVariable);
								variable = constantVariable;
							} else throw exception;

						}
					}
					/* ### DIRECT length ###:
					* Map operand, if its length is set (either by range or by own length) */
					Range length = null;
					if (operand.isRange())
						length = operand.getRange().deriveLength(); // length is derived: Some_operand<2:2> ==> the length is being set (0:0), not the real range (2:2).
					if (length == null) length = variable.getLength();
					/* Map */
					if (length != null) {
						operand.setLength(length);
					} else if (isLengthImposedBy(parentOperand)) {
						operand.setLength(getLengthImposedBy(parentOperand));
						/* For Constants that were added using ExceptionSolver, set the length, if it is imposed */
						if (variable instanceof ConstantVariable) {
							((ConstantVariable) variable).setLength(getFixedLengthFor(operand));
						}
					}
				} else {
					/* Operand declares constant. */
					/* ### DIRECT length ###:
					* Map operand, if its desired length is set (either by range or by own length, i.e. by VHDL typing) */
					Range length = null;
					if (operand.isRange())
						length = operand.getRange().deriveLength(); // length is derived: Some_operand<2:2> ==> the length is being set (0:0), not the real range (2:2).
					if (length == null) length = valAndLenHolder.getDesiredLength();
					if (length != null) {
						operand.setLength(length);
					} else {
						/* ### OBEYING OPERATOR IMPOSED LENGTH ###:
						* If its desired length is not set, look for operand imposed desired length. */
						/* If it is available, then map operand.
						* If it isn't available, then:
						* 1) in case of constraint imposing operators, skip this operand this time and wait until
						*    operator sets its constraint;
						* 2) in case of non-imposing operators, set the length to the minimum length required
						*    to store the constant value. */
						if (isLengthImposedBy(parentOperand)) {
							operand.setLength(getLengthImposedBy(parentOperand));
						} else if (parentOperand != null && parentOperand instanceof Expression
								&& isNonImposingOperator(((Expression) parentOperand).getOperator())) {
							length = Range.deriveLengthForValues(valAndLenHolder.getValue().intValue(), 0);
							operand.setLength(length);
						}
					}
				}
			} else if (operand instanceof UserDefinedFunction) {
				//todo: take from package file
				if (((UserDefinedFunction) operand).getUserDefinedFunction().startsWith("F_")) {
					operand.setLength(new Range(31, 0));
				} else {
					//todo: commented for DEMO
					Exception exception = new Exception("Could not calculate length for User Defined Function: " +
							((UserDefinedFunction) operand).getUserDefinedFunction() +
							"\nDo implement extracting length from package file!!!");
					Object solution = ExceptionSolver.getInstance().findSolution(exception.getMessage(), ExceptionSolver.SolutionOptions.VALUE);
					if (solution.getClass() == ConstantValueAndLengthHolder.class) {
						Range length = new Range(((ConstantValueAndLengthHolder) solution).getValue().intValue(), 0);
						operand.setLength(length);
					} else throw exception;
				}


			}
		}
	}

	private boolean areAllChildrenFixed(List<AbstractOperand> operands) {
		for (AbstractOperand operand : operands) {
			if (operand.getLength() == null) {
				return false;
			}
		}
		return true;
	}

	private boolean isNonImposingOperator(Operator operator) {
		return operator == Operator.CAT || operator == Operator.SHIFT_LEFT || operator == Operator.SHIFT_RIGHT;
	}

	/**
	 * Tries to set Operator imposed length, if both it is not set yet and it is possible (deducible).
	 *
	 * @param expression to check for length constraints on its operands
	 */
	private void storeImposedLength(Expression expression) {
		/* If imposed length has already been set for this operand, skip setting. */
		if (isLengthImposedBy(expression)) return;

		Operator operator = expression.getOperator();
		if (isNonImposingOperator(operator)) {
			/* Do nothing, since CAT and SHIFTs don't impose any constrained length. */
		} else {
			/* Scan operands in search for the first met one with fixed length.
			* If such one is found, then store the Operator imposed length (map it by the Expression) */
			for (AbstractOperand operand : expression.getOperands()) {
				if (isLengthFixedFor(operand)) {
					expression.setLength(getFixedLengthFor(operand));
					break;
				}
			}
		}
	}

	/**
	 * @param operand to get the length for
	 * @return fixed length for the operand or <code>null</code> if length is not set
	 */
	public Range getFixedLengthFor(AbstractOperand operand) {
		return operand.getLength();
	}

	private boolean isLengthFixedFor(AbstractOperand operand) {
		return operand.getLength() != null;
	}

	private Range getLengthImposedBy(AbstractOperand parentOperand) {
		return isLengthImposedBy(parentOperand) ? parentOperand.getLength() : null;
	}

	private boolean isLengthImposedBy(AbstractOperand parentOperand) {
		return parentOperand != null && isLengthFixedFor(parentOperand);
	}
}
