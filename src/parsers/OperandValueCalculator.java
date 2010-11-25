package parsers;

import base.vhdl.structure.*;
import parsers.vhdl.PackageParser;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class OperandValueCalculator {

	private Collection<Constant> constants = new HashSet<Constant>();

	public ValueHolder calculateValueFor(AbstractOperand operand) throws Exception {

		/* Discard PARTED operands */
		if (operand.isParted())
			throw new Exception(OperandValueCalculator.class.getSimpleName() +
					" is calculating value for Operand with parted indices. " +
					"This functionality is not currently supported.");

		if (operand instanceof OperandImpl) {
			String operandAsString = ((OperandImpl) operand).getName();
			/* Try to obtain direct integer value */
			BigInteger operandAsInteger = PackageParser.parseConstantValue(operandAsString);
			/* If direct integer value is not obtained, search for named constants */
			if (operandAsInteger == null) {
				operandAsInteger = getIntegerFromConstants(operandAsString);
			}
			return new ValueHolder(operandAsInteger, operand);
		} else if (operand instanceof Expression) {
			OperandValueCalculator.ValueHolder valueHolder;
			Collection<BigInteger> operandValues = new LinkedList<BigInteger>();
			for (AbstractOperand childOperand : ((Expression) operand).getOperands()) {
				valueHolder = calculateValueFor(childOperand);
				/* If operand could not be evaluated, throw it up and exit */
				if (!valueHolder.isValueCalculated()) return valueHolder;
				/* If operand was evaluated successfully, collect its value */
				operandValues.add(valueHolder.getValue());
			}
			/* Calculate value of the Expression using operands and operator */
			return calculateValueUsing(((Expression) operand).getOperator(), operandValues, operand);
		} else return new ValueHolder(null, operand);

	}

	private ValueHolder calculateValueUsing(Operator operator, Collection<BigInteger> operandValues, AbstractOperand unknownOperand) throws Exception {
		BigInteger currentValue = null;
		for (BigInteger operandValue : operandValues) {
			if (currentValue == null) {
				currentValue = operandValue;
			} else {
				currentValue = calculate(currentValue, operandValue, operator);
			}
		}

		return new ValueHolder(currentValue, unknownOperand);
	}

	private BigInteger calculate(BigInteger leftValue, BigInteger rightValue, Operator operator) throws Exception {
		BigInteger result = leftValue;
		switch (operator) {
			case ADDER:
				result = result.add(rightValue);
				break;
			case DIV:
				result = result.divide(rightValue); // leftValue / rightValue;
				break;
			case MOD: //todo
				result = result.mod(rightValue); // leftValue % rightValue;
				break;
			case MULT:
				result = result.multiply(rightValue); // leftValue * rightValue;
				break;
			case SUBTR:
				result = result.subtract(rightValue); // leftValue - rightValue;
				break;
			case XOR: //todo
				result = result.xor(rightValue); // leftValue ^ rightValue;
				break;
			case EXP:
				result = result.pow(rightValue.intValue()); // leftValue ** rightValue;
				break;
			case SHIFT_LEFT:
				result = result.shiftLeft(rightValue.intValue()); // leftValue * ( 10 * rightValue );
				break;
			case SHIFT_RIGHT:
				result = result.shiftRight(rightValue.intValue()); // leftValue / (10 * rightValue);
				break;
			default:
				throw new Exception("Operation " + operator + " is not supported for evaluation");
		}
		return result;
	}

	/**
	 * @param namedConstantName name of the named constant to search value of
	 * @return value of the specified named constant or <code>null</code> if
	 *         the set of constants doesn't contain the specified named constant.
	 */
	private BigInteger getIntegerFromConstants(String namedConstantName) {
		for (Constant constant : constants) {
			if (constant.getName().equals(namedConstantName)) {
				return constant.getValue();
			}
		}
		return null;
	}

	public void addConstant(Constant constant) {
		constants.add(constant);
	}

	public class ValueHolder {
		private final BigInteger value;
		private final AbstractOperand unknownOperand;

		public ValueHolder(BigInteger value, AbstractOperand unknownOperand) {
			this.value = value;
			this.unknownOperand = unknownOperand;
		}

		/**
		 * @return whether calculator was able to calculate the value of the
		 *         specified operand
		 */
		public boolean isValueCalculated() {
			return value != null;
		}

		/**
		 * @return calculated value of the specified expression or
		 *         <code>null</code> if the value could not be calculated
		 */
		public BigInteger getValue() {
			return value;
		}

		public AbstractOperand getUnknownOperand() {
			return unknownOperand;
		}
	}
}
