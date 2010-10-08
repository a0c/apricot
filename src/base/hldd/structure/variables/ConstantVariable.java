package base.hldd.structure.variables;

import base.HLDDException;
import base.Type;
import base.Indices;

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
		super(constantName, new Type(Indices.deriveLengthForValues(value.intValue(), 0))/*todo: remove this thing!!!*/);
        this.value = value;
        setConstant(true);
    }

    public String toString() {
        return super.toString() + "\tVAL = " + value;
    }

	public static ConstantVariable createNamedConstant(BigInteger value, String name, Indices forcedLength) {

		int length = forcedLength != null ? forcedLength.length() : Indices.deriveLengthForValues(value.intValue(), 0).length();

		name = (name == null) ?  "CONST_" + value + "_BW" + length : name;

		ConstantVariable newConstant = new ConstantVariable(name, value);

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

		return createNamedConstant(subRangeValue, null, rangeToExtract.deriveLength());
	}
}
