package base;

/**
 * @author Anton Chepurov
 */
public final class Range implements Comparable<Range> {

	static final String INTERSECTION_TEXT = "Cannot compare intersecting ranges: ";
	
	public static final Range BIT_RANGE = new Range(0, 0);

	private final int highest;
	private final int lowest;
	private final boolean isDescending; //todo: take this field into account in all the methods!

	public Range(int highest, int lowest) {
		this(highest, lowest, true);
	}

	public Range(int highest, int lowest, boolean isDescending) {
		this.highest = highest;
		this.lowest = lowest;
		this.isDescending = isDescending;
	}

	public int getHighest() {
		return highest;
	}

	public int getLowest() {
		return lowest;
	}

	public boolean isDescending() {
		return isDescending;
	}

	// todo: maybe should be renamed to getCardinality();

	public int length() {
		return highest - lowest + 1;
	}

	public int highestSB() {
		return length() - 1;
	}

	/**
	 * When applied to {@link base.hldd.structure.variables.RangeVariable}-s (see method
	 * {@link base.vhdl.visitors.GraphGenerator#visitTransitionNode(base.vhdl.structure.nodes.TransitionNode)}),
	 * the following situation holds:<br>
	 * <i>This object</i> is the range of {@link base.hldd.structure.variables.RangeVariable} and so it's never null.
	 * <br>
	 * Different situations:<br>
	 * 1) <code>valueRange</code> may be missing or not.<br>
	 * 2) <code>targetRange</code> may be missing or not.
	 * <p/>
	 * See the different situations in corresponding JUnit test.
	 *
	 * @param valueRange  where to extract absolute range from (range of
	 *                    <b>valueOperand</b> of {@link base.vhdl.structure.nodes.TransitionNode})
	 * @param targetRange range of <b>targetOperand</b> of {@link base.vhdl.structure.nodes.TransitionNode}.
	 *                    <b>NB!</b> Note that method checks targetRange to contain the range this
	 *                    method is called on and throws an Exception if it doesn't.
	 * @return corresponding absolute range of this range (the one method is invoked on),
	 *         extracted from targetRange.
	 */
	public Range absoluteFor(Range targetRange, Range valueRange) {
		/* Calculations require both targetRange and valueRange,
		* so the missing range must be derived. */

		if (targetRange == null && valueRange == null) {
			/* If both ranges are missing, it means that targetOperand and valueOperand
			* have the same length, and all derivations will be truncated at the end anyway.
			* So simply return the range itself. */
			return this;
		} else {
			/* If range is the same as target range,
			* it means that this is a direct assignment, where valueRange should be preserved (incl. null). */
			if (this.equals(targetRange)) {
				return valueRange;
			}
			/* Here at least one of the ranges is not null. */
			/* Derive the missing range from the present one: */
			targetRange = targetRange == null ? deriveLength(valueRange) : targetRange;
			valueRange = valueRange == null ? deriveLength(targetRange) : valueRange;

			/* Here both targetRange and valueRange are available. */
			/* Check targetRange to contain this range: */
			if (!targetRange.contains(this)) throw new RuntimeException("Unexpected bug while obtaining absolute " +
					"range:\ntargetRange doesn't contain range of RangeVariable." +
					"\nProbable source of error: incorrect splitting of RangeVariables into non-overlapping regions");
			/* Calculate difference: */
			Range difference = new Range(
					targetRange.highest - this.highest,
					this.lowest - targetRange.lowest);
			return new Range(
					valueRange.highest - difference.highest,
					valueRange.lowest + difference.lowest);
		}
	}

	private Range deriveLength(Range availableRange) {
		return new Range(availableRange.highestSB(), 0);
	}

	public Range deriveLength() {
		return deriveLength(this);
	}

	public Range deriveValueRange() {
		/* Compute maximum value this range can store (alternatively, maximum index this range can address) */
		int maxValue = (int) Math.pow(2, length());
		return new Range(maxValue - 1, 0);
	}

	/**
	 * Calculate the LENGTH of the register to store the max. possible value of the variable
	 *
	 * @param largestValue  highest value to be stored in the register
	 * @param smallestValue lowest value to be stored in the register
	 * @return length of the register required to store whichever of the possible values of the variable
	 */
	public static Range deriveLengthForValues(int largestValue, int smallestValue) {
		/* By default use the largest value.
		* Extend the largest value with negative range part if the range has one. */
		int extension = smallestValue < 0 ? smallestValue : 0;
		return new Range(Integer.toBinaryString(largestValue - extension).length() - 1, 0);
	}

	@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
	public boolean contains(Range range) {
		return range != null && lowest <= range.lowest && highest >= range.highest;
	}

	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, highest);
		result = HashCodeUtil.hash(result, lowest);
		result = HashCodeUtil.hash(result, isDescending);
		return result;
	}

	@SuppressWarnings({"QuestionableName"})
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		Range that = (Range) obj;
		return highest == that.highest && lowest == that.lowest;
	}

	public static boolean equals(Range firstRange, Range secondRange) {
		/* Check NULLs */
		if ((firstRange == null) ^ (secondRange == null)) return false;
		/* Here both either are nulls or non-nulls */
		if (firstRange != null) {
			if (!firstRange.equals(secondRange)) return false;
		}
		/* All checks passed */
		return true;
	}

	public static String toString(Range range) {
		return range == null ? "" : range.toString();
	}

	/**
	 * @param merge whether to merge single bit range (<5:5>) into one bit (<5>).
	 *                     <br><code>true</code> is currently used only in variable names of Nodes
	 *                     (V = 133	"CRC_STAT_WEN_1<< 1 >"	<< 1:1>).
	 * @return range in angular brackets, e.g. <br><code><<8:0><br>< 2 ></code>
	 */
	public String toStringAngular(boolean merge) {
		return merge && highest == lowest ?
				"<" + highest + ">" :
				"<" + highest + ":" + lowest + ">";
	}

	/**
	 * @return range in round brackets, e.g. <br><code>(8 DOWNTO 0)<br>( 2 )</code>
	 */
	public String toString() {
		return highest == lowest ?
				"(" + highest + ")" :
				"(" + highest + " DOWNTO " + lowest + ")";
	}

	public int compareTo(Range o) {
		/* Check to be equal, before intersection is detected */
		if (equals(o)) return 0;
		/* Check for intersections: they cannot be compared */
		if (intersectsWith(o)) throw new RuntimeException(INTERSECTION_TEXT + this + " and " + o);
		/* Check arbitrary index: now both are either larger or smaller than any index of compared object */
		return Integer.valueOf(highest).compareTo(o.highest);
	}

	@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
	private boolean intersectsWith(Range o) {
		/* Check the o to be inside this Range. */
		return intersects(o.highest) || intersects(o.lowest);
	}

	@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
	private boolean intersects(int index) {
		return index >= lowest && index <= highest;
	}

}
