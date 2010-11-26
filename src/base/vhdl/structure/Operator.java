package base.vhdl.structure;

import base.Indices;

import java.util.regex.Pattern;

/**
 * When adding a new operator, change getHighestPrecedenceOperator(),
 * FunctionVariable.isIdenticalTo(), FunctionVariable.computeHsb();
 * computeHSB() also.
 *
 * @author Anton Chepurov
 */
@SuppressWarnings({"EnumeratedConstantNamingConvention"})
public enum Operator {
	/* CONDITIONS */
	EQ("=", true, false, 2),
	NEQ("/=", true, false, 2),
	LE("<=", true, false, 2),
	U_LE(true, 2),
	GE(">=", true, false, 2),
	U_GE(true, 2),
	LT("<", true, false, 2),
	U_LT(true, 2),
	GT(">", true, false, 2),
	U_GT(true, 2),
	/* ALGEBRAIC OPERATIONS */
	ADDER("+", false, false, 2),
	MULT("*", false, false, 2),
	DIV("/", false, false, 2),
	CAT("&", false, false, 2),
	XOR(" XOR ", false, true, 2),
	SUBTR("-", false, false, 2),
	AND(" AND ", false, true, 2), // todo: consider using 2 AND-S, OR-s (INV-s?): logical and algebraical. Benefit 1) remove 3rd parameter from constructor and method isLogical(). Benefit 2) simplify code that uses objects of Operator class. Or just remove 3rd parameter and check in ConditionGraphManager operator to be AND, OR or XOR in order for them to be logical.
	OR(" OR ", false, true, 2),
	MOD(" MOD ", false, false, 2),
	EXP(" * * ", false, false, 2),
	SHIFT_RIGHT(false, 2),
	SHIFT_LEFT(false, 2),
	INV(false, true, 1);

	private final String delim;
	private final boolean isCondition;
	private final boolean isLogical;
	private final int numberOfOperands;
	private static final Pattern MINUS_NUMBER_PATTERN = Pattern.compile("\\-[0-9A-F]+");

	/**
	 * Constructor for functions with delim (all but SHR and SHL)
	 *
	 * @param delim			operation delimiter
	 * @param isCondition	  whether operator is condition
	 * @param isLogical		whether operator is logical
	 * @param numberOfOperands number of supported operands
	 */
	Operator(String delim, boolean isCondition, boolean isLogical, int numberOfOperands) {
		this.delim = delim;
		this.isCondition = isCondition;
		this.isLogical = isLogical;
		this.numberOfOperands = numberOfOperands;
	}

	/**
	 * Constructor for functions without delim (SHR and SHL)
	 *
	 * @param isCondition	  whether operator is logical
	 * @param numberOfOperands number of supported operands
	 */
	Operator(boolean isCondition, int numberOfOperands) {
		this(isCondition, false, numberOfOperands);
	}

	/**
	 * Constructor for logical functions without delim (INV)
	 *
	 * @param isCondition	  whether operator is conditional
	 * @param isLogical		whether operator is logical
	 * @param numberOfOperands number of supported operands
	 */
	Operator(boolean isCondition, boolean isLogical, int numberOfOperands) {
		delim = null;
		this.isCondition = isCondition;
		this.isLogical = isLogical;
		this.numberOfOperands = numberOfOperands;
	}

	public boolean isCondition() {
		return isCondition;
	}

	/**
	 * Currently logical are: XOR, AND, OR, INV
	 *
	 * @param length length of the operand
	 * @return whether operator is logical
	 */
	public boolean isLogical(int length) {
		//todo: when AND (OR) operators get split into logical and arithmetical, remove length == 1 condition from here.
		return isLogical && length == 1;
	}

	public String getDelim() {
		return delim;
	}

	public int getNumberOfOperands() {
		return numberOfOperands;
	}

	/**
	 * Checks whether the specified {@code lines} contain
	 * the specified {@code operatorToContain}
	 *
	 * @param operatorToContain function to check for
	 * @param lines			 array of Strings where to check for the {@code operatorToContain}
	 * @return <code>true</code> if the lines contain the specified function.
	 *         <code>false</code> otherwise.
	 */
	public static boolean containsOperator(Operator operatorToContain, String... lines) {
		for (String line : lines) {
			if (line.contains(operatorToContain.delim)) return true;
		}
		return false;
	}

	public static Operator getHighestPrecedenceOperator(String... validRegions) {

		/* Here operator precedence is hard-coded */
		if (containsOperator(OR, validRegions)) {
			return OR;
		} else if (containsOperator(AND, validRegions)) {
			return AND;
		} else if (containsOperator(NEQ, validRegions)) {
			return NEQ;
		} else if (containsOperator(LE, validRegions)) {
			return LE;
		} else if (containsOperator(GE, validRegions)) {
			return GE;
		} else if (containsOperator(EQ, validRegions)) {
			return EQ;
		} else if (containsOperator(LT, validRegions)) {
			return LT;
		} else if (containsOperator(GT, validRegions)) {
			return GT;
		} else if (containsOperator(XOR, validRegions)) {
			return XOR;
		} else if (containsOperator(ADDER, validRegions)) {
			return ADDER;
		} else if (containsOperator(SUBTR, validRegions) && !isMinusNumber(validRegions)) {
			return SUBTR;
		} else if (containsOperator(EXP, validRegions)) {
			return EXP;
		} else if (containsOperator(MULT, validRegions)) {
			return MULT;
		} else if (containsOperator(DIV, validRegions)) {
			return DIV;
		} else if (containsOperator(MOD, validRegions)) {
			return MOD;
		} else if (containsOperator(CAT, validRegions)) {
			return CAT;
		}

		//todo...

		return null;
	}

	/**
	 * Method prevents lines like "-32767" from being parsed as SUBTR expression
	 *
	 * @param validRegions regions to check
	 * @return <code>true</code> if the specified validRegions declare a number with minus sign.
	 *         <code>false</code> otherwise.
	 */
	static boolean isMinusNumber(String... validRegions) {
		return validRegions.length == 1 && MINUS_NUMBER_PATTERN.matcher(validRegions[0]).matches();
	}


	public Indices adjustLength(Indices currentLength, Indices addedLength, Indices addedRange) {
		/* Length is derived for the following case:
		* Some_operand<2:2> ==> the length is being adjusted (0:0), not the real indices (2:2).*/
		Indices newOperandLength = addedRange != null ? addedRange.deriveLength() : addedLength;
		/* Adjust range */
		return adjustLength(currentLength, newOperandLength);
	}

	public Indices adjustLength(Indices currentIndices, Indices newOperandIndices) {

		if (isCondition) {
			return Indices.BIT_INDICES;
		}
		switch (this) {
			case EQ:
			case NEQ:
			case LE:
			case U_LE:
			case GE:
			case U_GE:
			case LT:
			case U_LT:
			case GT:
			case U_GT:
				throw new RuntimeException("Adjusting length for condition operator; should've returned BIT_INDICES by this place");
			case CAT:
				/* Accumulative HSB of all the operands */
				int currentLength = currentIndices == null ? 0 : currentIndices.length();
				int newOperandLength = newOperandIndices.length();

				return new Indices(currentLength + newOperandLength - 1, 0);
			case ADDER:
			case MULT:
			case DIV:
			case XOR:
			case SUBTR:
			case AND:
			case OR:
			case MOD:
				/* HSB of the longest operand */
				if (currentIndices == null) {
					return newOperandIndices;
				} else {
					int currentHsb = currentIndices.highestSB();
					int newHsb = newOperandIndices.highestSB();
					return currentHsb > newHsb ? currentIndices : newOperandIndices;
				}
			case EXP:
				break;
			case SHIFT_RIGHT:
			case SHIFT_LEFT:
				/* HSB of the operand being shifted */
				return currentIndices == null ? newOperandIndices : currentIndices;
			case INV:
				return newOperandIndices;
		}
		throw new RuntimeException("Don't know how to adjust length for " + this + " operator");
	}


}
