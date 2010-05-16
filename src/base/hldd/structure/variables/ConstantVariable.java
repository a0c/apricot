package base.hldd.structure.variables;

import base.HLDDException;
import base.Type;
import base.Indices;

import java.util.TreeMap;
import java.math.BigInteger;

/**
 * Class represents a CONSTANT VARIABLE as it is defined in AGM.<br>
 * This extends Variable class by introducing parameter <i>value</i>.
 *
 * <p>User: Anton Chepurov
 * <br>Date: 25.02.2007
 * <br>Time: 21:14:16
 */
public class ConstantVariable extends Variable {

    // ConstantVariable VALUE
    private BigInteger value;

    public ConstantVariable(String constantName, BigInteger value){
        this.value = value;
        this.name = constantName;
        type = new Type(Indices.deriveLengthForValues(value.intValue(), 0)); //todo: remove this thing!!!
        setConstant(true);
    }

    public String toString() {
        return super.toString() + "\tVAL = " + value;
    }

    /**
     * todo: only leave usages of this in ModelCollectors (VHDL and PSL). And then try to move this method to the collector
     * Looks for a constant with the desired value in the CONSTS hashMap.
     * If no such constant is found, a new one is created and added to CONSTS hashMap.
     * The new constant is also returned.
     * @param constValueToFind desired value of the constant
     * @param targetLength requested length or <code>null</code> if doesn't matter
     * @param consts map of constants to search through
     * @param useSameConstants if existent constants with the same value can be used. todo: The length is thus adjusted to the greatest one! (is it?)
     * @return an instance of ConstantVariable with the value of <code>constValueToFind</code>
     */
    public static ConstantVariable getConstByValue(BigInteger constValueToFind, Indices targetLength,
                                                   TreeMap<String, ConstantVariable> consts, boolean useSameConstants) {

        // Search for EXISTENT constants
        for (String constName : consts.keySet()) {
            ConstantVariable constantVariable = consts.get(constName);
            if (constantVariable.getValue().equals(constValueToFind)) {
                /* Constan with the SAME VALUE found. */
                if (useSameConstants) {
                    //todo:
                } else {
                    /* For EVERY variable LENGTH there must be a SEPARATE constant */
                    if (targetLength == null) {
                        /* If length doesn't matter, return the first met constant with the required value */
                        return constantVariable;
                    } else {
                        if (targetLength.equals(constantVariable.getLength())) {
                            return constantVariable;
                        }
                    }
                }
                /* Check LENGTH. */
//                if (targetLength != -1 && constantVariable.getHighestIndex() != targetLength) {
//                    /* DIFFERENT lengths */
//                    if (useSameConstants) {
//                        /* Change the LENGTH of existent constant */
//                        constantVariable.setHighestIndex(targetLength);
//                    } else {
//                        constantVariable = new ConstantVariable("CONST_" + constValueToFind + "_BW" + (targetLength + 1),
//                                constValueToFind);
//                        constantVariable.setHighestIndex(targetLength);
//                        consts.put(constantVariable.getName(), constantVariable);
//                    }
//                }
//                return constantVariable;
            }
        }
        // EXISTENT constant is not found, so create a new one
        ConstantVariable newConstantVariable = createNamedConstant(constValueToFind, targetLength); /*targetLength +1*/
        consts.put(newConstantVariable.getName(), newConstantVariable);
        return newConstantVariable;

    }

	public static ConstantVariable createNamedConstant(BigInteger value, Indices forcedLength) {

		int length = forcedLength != null ? forcedLength.length() : Indices.deriveLengthForValues(value.intValue(), 0).length();

		ConstantVariable newConstant = new ConstantVariable("CONST_" + value + "_BW" + length, value);

		if (forcedLength != null && !newConstant.getLength().equals(forcedLength))
			newConstant.setLength(forcedLength);

		return newConstant;
	}

    public boolean isIdenticalTo(AbstractVariable comparedAbsVariable) {
        return comparedAbsVariable instanceof ConstantVariable && value.equals(((ConstantVariable) comparedAbsVariable).value);
    }

    /* Setters and Getters START */

    public BigInteger getValue() {
        return value;
    }

    /* Setters and Getters END */

	public ConstantVariable subRange(Indices rangeToExtract) throws HLDDException {
		Indices length = getLength();
		if (!length.contain(rangeToExtract)) {
			throw new HLDDException("ConstantVariable: subRange(): rangeToExtract is out of bounds: " + rangeToExtract + " out of " + length);
		}
		//todo: if rangeToExtract.equals(length)...
		/* As string, ... */
		StringBuilder builder = new StringBuilder(value.toString(2)); // 4 -> '100'
		/* ... , fill with '0'-s up to the desired length, ...*/
		StringBuilder newBuilder = new StringBuilder();
		int offset = builder.length() - 1;
		for (int idx = length.getLowest(); idx <= length.getHighest(); idx++) { // '100' -> '0010'   (reverse + fill with '0'-s)
			newBuilder.append(offset - idx >= 0 ? builder.charAt(offset - idx) : '0');
		}
		/* ..., extract,... */
		BigInteger subRangeValue = new BigInteger(
				new StringBuilder(newBuilder.subSequence(rangeToExtract.getLowest(), rangeToExtract.getHighest() + 1))
						.reverse().toString(), 2);

		return createNamedConstant(subRangeValue, rangeToExtract.deriveLength());
	}
}
