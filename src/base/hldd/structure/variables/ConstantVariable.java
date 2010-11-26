package base.hldd.structure.variables;

import base.HLDDException;
import base.Range;
import base.Type;
import base.hldd.structure.nodes.utils.Condition;

import java.math.BigInteger;
import java.util.Map;

/**
 * Class represents a CONSTANT VARIABLE as it is defined in AGM.<br>
 * This extends Variable class by introducing parameter <i>value</i>.
 *
 * @author Anton Chepurov
 */
public class ConstantVariable extends Variable {

	private BigInteger value;

	private final Map<Condition, ConstantVariable> arrayValues;

	public ConstantVariable(String constantName, BigInteger value) {
		this(constantName, value, deriveType(value));
	}

	public ConstantVariable(String constantName, BigInteger value, Type type) {
		super(constantName, type == null ? deriveType(value) : type);
		this.value = value;
		setConstant(true);
		arrayValues = null;
	}

	public ConstantVariable(Map<Condition, ConstantVariable> arrayValues) {
		this.arrayValues = arrayValues;
	}

	private static Type deriveType(BigInteger value) {
		return new Type(Range.deriveLengthForValues(value.intValue(), 0));
	}

	public boolean isArray() {
		return arrayValues != null;
	}

	public String toString() {
		if (isArray()) {
			return "### ARRAY CONSTANT (" + arrayValues.size() + ") ###";
		}
		return super.toString() + "\tVAL = " + value;
	}

	public static ConstantVariable createNamedConstant(BigInteger value, String name, Range forcedLength) {

		int length = forcedLength != null ? forcedLength.length() : Range.deriveLengthForValues(value.intValue(), 0).length();

		name = (name == null) ? "CONST_" + value + "_BW" + length : name;

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

	public ConstantVariable subRange(Range rangeToExtract) throws HLDDException {

		if (isArray()) {
			if (rangeToExtract.length() != 1) {
				throw new RuntimeException("Only single bit range can be extracted from ARRAY ConstantVariable. Actual range: " + rangeToExtract);
			}
			return arrayValues.get(Condition.createCondition(rangeToExtract.getLowest()));
		}
		Range length = getLength();

		if (!length.contains(rangeToExtract)) {
			throw new HLDDException("ConstantVariable: subRange(): rangeToExtract is out of bounds: " + rangeToExtract + " out of " + length);
		}

		if (rangeToExtract.equals(length)) {
			return this;
		}
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
