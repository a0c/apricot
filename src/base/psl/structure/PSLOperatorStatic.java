package base.psl.structure;

/**
 * Class statically defines the set of supported PSL operators.
 *
 * @author Anton Chepurov
 * @deprecated {@link base.psl.structure.PSLOperator} must be used instead
 */
@Deprecated
public enum PSLOperatorStatic {

	/* INVARIANCE operators */
	ALWAYS("ALWAYS"), NEVER("NEVER"), EVENTUALLY_STRONG("EVENTUALLY!"), NEXT("[nN][eE][xX][tT]"),

	/* SERE operators */
	SERE_IMPLICATION_OVERLAPPING("|->"),
	SERE_IMPLICATION_NON_OVERLAPPING("|=>"),
	SERE_FUSION(":"),
	SERE_CONCATENATION(";"),
//    SERE_REPETITION_, // [*], [+], [=], [->]

	/* LOGICAL operators */
	LOGICAL_IMPLICATION("->"),
	LOGICAL_IMPLICATION_IFF("<->"),
	LOGICAL_OR("[oO][rR]"),
	LOGICAL_AND("[aA][nN][dD]"),
	LOGICAL_NOT("!");


	/* Operator precedence table, starting from the lowest precision (which is parsed first, actually) */
	private static final PSLOperatorStatic[][] operatorPrecedenceMatrix = new PSLOperatorStatic[][]
			{
					{LOGICAL_IMPLICATION, LOGICAL_IMPLICATION_IFF},												 // ->   <->
					{SERE_IMPLICATION_OVERLAPPING, SERE_IMPLICATION_NON_OVERLAPPING},								// |->  |=>
					{NEXT},
					{SERE_CONCATENATION},
					{SERE_FUSION},
					{LOGICAL_OR},
					{LOGICAL_AND},
					{LOGICAL_NOT}

			};

	public final String delim;

	PSLOperatorStatic(String delim) {
		this.delim = delim;
	}

	public static PSLOperatorStatic parseInvarianceOperator(String invariantLine) throws Exception {
		if (invariantLine.equalsIgnoreCase(ALWAYS.delim)) {
			return ALWAYS;
		} else if (invariantLine.equalsIgnoreCase(NEVER.delim)) {
			return NEVER;
		} else if (invariantLine.equalsIgnoreCase(EVENTUALLY_STRONG.delim)) {
			return EVENTUALLY_STRONG;
		} else if (invariantLine.matches(NEXT.delim + "[\\s\\[_].*")) {
			return NEXT;
		} else throw new Exception("Invalid word is used as an invariant operator: " + invariantLine);
	}

	/**
	 * Iterates all supported operators in <u>ascending</u> order of their precedence<br>
	 * and returns the operator if it is found in the string.
	 *
	 * @param word the string to search in
	 * @return <code>PSLOperator</code> with the highest precedence found in the string,
	 *         or <code>null</code> if no PSLOperator is found
	 *         (i.e. if HDL operators only are left in the string)
	 * @throws Exception if unknown String operator is used to determine the PSLOperator
	 */
	public static PSLOperatorStatic getOperatorWithLowestPrecedence(String word) throws Exception {

		for (PSLOperatorStatic[] levelPSLOperators : operatorPrecedenceMatrix) {
			for (PSLOperatorStatic levelPSLOperator : levelPSLOperators) {
				if (levelPSLOperator == LOGICAL_IMPLICATION) {
					/*      ->      */
					if (word.matches(".*[^\\|]+\\" + levelPSLOperator.delim + ".*"))
						return levelPSLOperator; //todo: when multiple operators of the same precedence are allowed - use split[regex]

				} else if (levelPSLOperator == LOGICAL_OR || levelPSLOperator == LOGICAL_AND) {
					/*      OR      AND     */
					if (word.matches(".*[\\s\\)]" + levelPSLOperator.delim + "[\\s\\(].*"))
						return levelPSLOperator; //todo: when multiple operators of the same precedence are allowed - use split[regex]

				} else if (levelPSLOperator == NEXT) {
					/*      NEXT        NEXT_       */
					if (word.matches(levelPSLOperator.delim + "[\\s\\[_].*"))
						return levelPSLOperator;

				} else {
					if (word.contains(levelPSLOperator.delim))
						return levelPSLOperator; //todo: when multiple operators of the same precedence are allowed - use split[regex]

				}
			}
		}

		return null;
	}

	public boolean isSERE() {
		return this == SERE_CONCATENATION || this == SERE_FUSION;
	}

}
