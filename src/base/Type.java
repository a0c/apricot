package base;

/**
 * @author Anton Chepurov
 */
public final class Type {

	public static final Type BIT_TYPE = new Type(Range.BIT_RANGE);
	public static final Type BOOLEAN_TYPE = createFromValues(1, 0);

	/**
	 * Range of possible values in case of enum-s; <code>null</code> if not enum.
	 */
	private final Range valueRange;
	/**
	 * Physical length of variable.
	 * Used in different length-calculation methods and for final AGM printing to file.
	 */
	private Range length;
	/**
	 * Type of array other than std_logic (composite type)
	 */
	private final Type arrayElementType;
//    private final boolean isUpDirection;
//    private final int offset;


	/**
	 * Constructor which sets valueRange to <code>null</code>.
	 *
	 * @param length physical length of variable/register. This defines the highestSB.
	 */
	public Type(Range length) {
		this(null, length, null);
	}

	/**
	 * @param valueRange range of possible values in case of enum-s; <code>null</code> if not enum.
	 * @param length	 physical length of variable/register. This defines the highestSB.
	 */
	public Type(Range valueRange, Range length) {
		this(valueRange, length, null);
	}

	public Type(Range length, Type arrayElementType) {
		this(null, length, arrayElementType);
	}

	private Type(Range valueRange, Range length, Type arrayElementType) {
		this.valueRange = valueRange;
		this.length = length;
		this.arrayElementType = arrayElementType;
	}

	public Range getValueRange() {
		return valueRange;
	}

	public Range getLength() {
		return length;
	}

	public void setLength(Range length) {
		this.length = length;
	}

	public boolean isSigned() {
		return isEnum() && valueRange.getLowest() < 0;
	}

	public boolean isEnum() {
		return valueRange != null;
	}

	public boolean isArray() {
		return arrayElementType != null;
	}

	public Range resolveValueRange() {
		if (isEnum()) {
			return valueRange;
		} else {
			return length.deriveValueRange();
		}
	}

	public int countPossibleValues(Range range) {
		return countPossibleValues(range, -1);
	}

	public int countPossibleValues(Range range, int upperBound) {
		int valuesCount;
		if (range != null) {
			valuesCount = range.deriveValueRange().length();
		} else {
			valuesCount = resolveValueRange().length();
		}

		if (upperBound == -1) {
			return valuesCount;
		}
		return Math.min(valuesCount, upperBound);
	}

	public int getCardinality() {
		if (!isEnum()) {
			throw new RuntimeException("Cardinality is requested from a non-enum Type: " + this);
		}
		return valueRange.length();
	}

	public int getHighestSB() {
		return length.highestSB();
	}

	public int getOffset() {
		return length.getLowest();
	}

	public String lengthToString() {
		return length.deriveLength().toStringAngular(false);
	}

	@SuppressWarnings({"QuestionableName"})
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		Type that = (Type) obj;
		if (!length.equals(that.length)) return false;
		/* If one is enum and another is not enum */
		if (isEnum() ^ that.isEnum()) return false;
		/* Here both are either enum-s or not enum */
		if (isEnum()) {
			if (!valueRange.equals(that.valueRange)) return false;
		}
		if (isArray() ^ that.isArray()) return false;
		if (isArray()) {
			if (!arrayElementType.equals(that.arrayElementType)) return false;
		}
		/* All fields identical */
		return true;
	}

	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, length.hashCode());
		if (isEnum()) {
			result = HashCodeUtil.hash(result, valueRange.hashCode());
		}
		if (isArray()) {
			result = HashCodeUtil.hash(result, arrayElementType.hashCode());
		}
		return result;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("TYPE ");
		sb.append(length.toStringAngular(false));
		if (isEnum()) {
			sb.append(" (ENUM=");
			sb.append(valueRange.toString());
			sb.append(");");
		}
		if (isArray()) {
			sb.append(" (SUBTYPE=").append(arrayElementType).append(");");
		}
		return sb.toString();
	}

	public Type getArrayElementType() {
		if (!isArray()) {
			throw new RuntimeException("Obtaining element type from non-array type: " + this);
		}
		return arrayElementType;
	}

	public Type deriveRangeType(Range range) {
		if (range == null) {
			return this;
		}
		/* Derive the length of the range */
		Range length = range.deriveLength();
		if (isArray()) {
			return length.length() == 1 ? arrayElementType : new Type(length, arrayElementType);
		}
		/* Generate valueRange if needed */
		Range valueRange = null;
		if (isEnum()) {
			valueRange = length.deriveValueRange();
		}

		return new Type(valueRange, length);

	}

	public static Type createFromValues(Range valueRange) {
		/* Calculate the LENGTH of the register to store the max. possible value of the variable */
		Range length = Range.deriveLengthForValues(valueRange.getHighest(), valueRange.getLowest());

		return new Type(valueRange, length); /*todo: , valueRange.isDescending() ? */   // todo: <== isDescending() for #length#     <=== see in VHDLStructureParser.parseType()
	}

	public static Type createFromValues(int largestValue, int smallestValue) {
		return createFromValues(new Range(largestValue, smallestValue));
	}

	public Type getTargetElementType() {
		Type elType = this;
		while (elType.isArray()) {
			elType = elType.getArrayElementType();
		}
		return elType;
	}
}
