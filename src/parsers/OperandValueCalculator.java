package parsers;

import base.vhdl.structure.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.HashSet;
import java.math.BigInteger;

import parsers.vhdl.PackageParser;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 28.10.2008
 * <br>Time: 22:43:50
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

    private BigInteger calculate(BigInteger leftValue, BigInteger righValue, Operator operator) throws Exception {
        BigInteger result = leftValue;
        switch (operator) {
            case ADDER:
                result = result.add(righValue);
                break;
            case DIV:
                result = result.divide(righValue); // leftValue / righValue;
                break;
            case MOD: //todo
                result = result.mod(righValue); // leftValue % righValue;
                break;
            case MULT:
                result = result.multiply(righValue); // leftValue * righValue;
                break;
            case SUBTR:
                result = result.subtract(righValue); // leftValue - righValue;
                break;
            case XOR: //todo
                result = result.xor(righValue); // leftValue ^ righValue;
                break;
            case SHIFT_LEFT:
                result = result.shiftLeft(righValue.intValue()); // leftValue * ( 10 * righValue );
                break;
            case SHIFT_RIGHT:
                result = result.shiftRight(righValue.intValue()); // leftValue / (10 * righValue);
                break;
            default:
                throw new Exception("Operation " + operator + " is not supported for evaluation" );
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

    ValueHolder createValueHolder(BigInteger value, AbstractOperand unknownOperand) {
        return new ValueHolder(value, unknownOperand);
    }


    public class ValueHolder {
        private BigInteger value;
        private AbstractOperand unknownOperand;

        public ValueHolder(BigInteger value, AbstractOperand unknownOperand) {
            this.value = value;
            this.unknownOperand = unknownOperand;
        }

        /**
         * @return whether calculator was able to calculate the value of the
         *         specified operand
         */
        public boolean isValueCalculated(){
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
