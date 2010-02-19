package base;

/**
 * Immutable class representing indices (i.e. ranges).
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 13.10.2008
 * <br>Time: 0:26:13
 */
public final class Indices implements Comparable<Indices> {
    static final String INTERSECTION_TEXT = "Cannot compare intersecting indices: ";
    public static final Indices BIT_INDICES = new Indices(0, 0);

    private final int highest;
    private final int lowest;
    private final boolean isDescending; //todo: take this field into account in all the methods!

    public Indices(int highest, int lowest) {
        this (highest, lowest, true);
    }

    public Indices(int highest, int lowest, boolean isDescending) {
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

    public int highestSB(){
        return length() - 1;
    }

    /**
     * When applied to {@link base.hldd.structure.variables.PartedVariable}-s (see method
     * {@link base.vhdl.visitors.GraphGenerator#visitTransitionNode(base.vhdl.structure.nodes.TransitionNode)}),
     * the following situation holds:<br>
     * <i>This object</i> is the Indices of {@link base.hldd.structure.variables.PartedVariable} and so it's never null.
     * <br>
     * Different situations:<br>
     * 1) <code>valueIndices</code> may be missing or not.<br>
     * 2) <code>variableIndices</code> may be missing or not.
     * <p>
     * See the different situations in corresponding JUnit test.
     *
     * @param valueIndices where to extract absolute indices from (indices of
     *        <b>valueOperand</b> of {@link base.vhdl.structure.nodes.TransitionNode})
     * @param variableIndices indices of <b>variableOperand</b> of {@link base.vhdl.structure.nodes.TransitionNode}.
     *        <b>NB!</b> Note that method checks curTransitionIndices to contain the Indices this method is called on
     *        and throws an Exception if they don't.
     * @return corresponding absolute indices of these Indices ( the ones method is invoked on ),
     *         extracted from targetIndices.
     */
    public Indices absoluteFor(Indices variableIndices, Indices valueIndices) {
        /* Calculations require both variableIndices and valueIndices,
         * so the missing indices must be derived. */

        if (variableIndices == null && valueIndices == null) {
            /* If both indices are missing, it means that variableOperand and valueOperand
            * have the same length, and all derivations will be truncated at the end anyway.
            * So simply return the parted indices themselves. */
            return this;
        } else {
            /* Here at least one of the indices is not null. */
            /* Derive the missing indices from the present one: */
            variableIndices = variableIndices == null ? deriveLength(valueIndices) : variableIndices;
            valueIndices = valueIndices == null ? deriveLength(variableIndices) : valueIndices;

            /* Here both variableIndices and valueIndices are available. */
            /* Check variableIndices to contain these indices: */
            if (!variableIndices.contain(this)) throw new RuntimeException("Unexpected bug while obtaining absolute " +
                    "indices:\nvariableIndices don't contain indices of PartedVariable." +
                    "\nProbable source of error: incorrect splitting of PartedVariales into non-interlapping regions");
            /* Calculate difference: */
            Indices difference = new Indices(
                    variableIndices.highest - this.highest,
                    this.lowest - variableIndices.lowest);
            return new Indices(
                    valueIndices.highest - difference.highest,
                    valueIndices.lowest + difference.lowest);
        }
    }

    private Indices deriveLength(Indices availableIndices) {
        return new Indices(availableIndices.highestSB(), 0);
    }

    public Indices deriveLength() {
        return deriveLength(this);
    }

    public Indices deriveValueRange() {
        /* Compute maximum value these indices can store (alternatively, maximum index this indices can address) */
        int maxValue = (int) Math.pow(2, length());
        return new Indices(maxValue - 1, 0);
    }

    /**
     * Calculate the LENGTH of the register to store the max. possible value of the variable
     *
     * @param largestValue highest value to be stored in the register
     * @param smallestValue lowest value to be stored in the register
     * @return length of the register required to store whichever of the possible values of the variable
     */
    public static Indices deriveLengthForValues(int largestValue, int smallestValue) {
        /* By default use the largest value.
        * Extend the largest value with negative range part if the range has one. */
        int extension = smallestValue < 0 ? smallestValue : 0;
        return new Indices(Integer.toBinaryString(largestValue - extension).length() - 1, 0);
    }

    public boolean contain(Indices indices) {
        return indices != null && lowest <= indices.lowest && highest >= indices.highest;
    }

    public int hashCode() {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash(result, highest);
        result = HashCodeUtil.hash(result, lowest);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ((obj == null) || (obj.getClass() != this.getClass())) return false;
        Indices that = (Indices) obj;
        return highest == that.highest && lowest == that.lowest;
    }

    public static boolean equals(Indices indices1, Indices indices2) {
        /* Check NULLs */
        if ((indices1 == null) ^ (indices2 == null)) return false;
        /* Here both either are nulls or non-nulls */
        if (indices1 != null) {
            if (!indices1.equals(indices2)) return false;
        }
        /* All checks passed */
        return true;
    }

    public static String toString(Indices indices) {
        return indices == null ? "" : indices.toString();
    }    

    /**
     * @param mergeIndices whether to merge single bit indices (<5:5>) into one bit (<5>).
     *        <br><code>true</code> is currently used only in variable names of Nodes
     *        (V = 133	"CRC_STAT_WEN_1<< 1 >"	<< 1:1>).
     * @return indices in angular brackets, e.g. <br><code><<8:0><br>< 2 ></code>
     */
    public String toStringAngular(boolean mergeIndices) {
        return mergeIndices && highest == lowest ?
                "<" + highest + ">" :
                "<" + highest + ":" + lowest + ">";
    }

    /**
     * @return indices in round brackets, e.g. <br><code>(8 DOWNTO 0)<br>( 2 )</code>
     */
    public String toString() {
        return highest == lowest ?
                "(" + highest + ")" :
                "(" + highest + " DOWNTO " + lowest + ")";
    }

    public int compareTo(Indices o) {
        /* Check to be equal, before intersection is detected */
        if (equals(o)) return 0;
        /* Check for intersections: they cannot be compared */
        if (intersectsWith(o)) throw new RuntimeException(INTERSECTION_TEXT + this + " and " + o);
        /* Check arbitrary index: now both are either larger or smaller than any index of compared object */
        return Integer.valueOf(highest).compareTo(o.highest);
    }

    private boolean intersectsWith(Indices o) {
        /* Check the o to be inside this Indices. */
        return contains(o.highest) || contains(o.lowest);
    }

    private boolean contains(int index) {
        return index >= lowest && index <= highest;
    }

}
