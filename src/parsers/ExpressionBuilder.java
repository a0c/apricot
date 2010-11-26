package parsers;

import base.Indices;
import base.helpers.RegexpFactory;
import base.hldd.structure.nodes.utils.Condition;
import base.vhdl.structure.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Static class for building Expressions.
 *
 * @author Anton Chepurov
 */
public class ExpressionBuilder {
	private static final Pattern STD_LOGIC_VECTOR_CONV = Pattern.compile(
			"^" + RegexpFactory.createStringRegexp("STD_LOGIC_VECTOR") + " \\( .+ \\)$"
	);
	private static final Pattern UNSIGNED_CONV = Pattern.compile(
			"^" + RegexpFactory.createStringRegexp("UNSIGNED") + " \\( .+ \\)$"
	);
	private static final Pattern CONV_INTEGER_CONV = Pattern.compile(
			"^" + RegexpFactory.createStringRegexp("CONV_INTEGER") + " \\( .+ \\)$"
	);
	private static final Pattern TO_INTEGER_CONV = Pattern.compile(
			"^" + RegexpFactory.createStringRegexp("TO_INTEGER") + " \\( .+ \\)$"
	);
	private static final Pattern USER_DEFINED_CONV = Pattern.compile(
			"^[a-zA-Z][\\w]* \\( .+ \\)$"
	);

	public static final Pattern BIT_RANGE_PATTERN = Pattern.compile("^[^ ]+? \\( .+? (DOWNTO|TO) .+? \\)$");
	private static final Pattern SINGLE_BIT_PATTERN = Pattern.compile("^[^ ]+? \\( .+? \\)$"); // .+? \( .+? \)$
	private static final Pattern PURE_BIT_RANGE_PATTERN = Pattern.compile("^.+? (DOWNTO|TO) .+$");


	private final OperandValueCalculator valueCalculator;
	private final Collection<String> variableNames;
	private final HashMap<String, Alias> aliasByName = new HashMap<String, Alias>();

	public ExpressionBuilder() {
		valueCalculator = new OperandValueCalculator();
		variableNames = java.util.Collections.emptySet();
	}

	public ExpressionBuilder(OperandValueCalculator valueCalculator, Collection<String> variableNames) {
		this.valueCalculator = valueCalculator;
		this.variableNames = variableNames;
	}

	public AbstractOperand buildArrayExpression(Map<Condition, String> lines) throws Exception {

		if (lines.isEmpty()) {
			return null;
		}

		if (lines.size() == 1) {
			return buildExpression(lines.get(Condition.FALSE));
		}

		Map<Condition, OperandImpl> arrayOperands = new TreeMap<Condition, OperandImpl>();
		for (Map.Entry<Condition, String> entry : lines.entrySet()) {
			String line = entry.getValue();
			if (line != null) {
				AbstractOperand operand = buildExpression(line);
				arrayOperands.put(entry.getKey(), (OperandImpl) operand);
			}
		}
		return new OperandImpl(arrayOperands);
	}

	public AbstractOperand buildExpression(String line) throws Exception {
		/* A dedicated ExpressionContext is allocated for creation of every expression */
		ExpressionContext expressionContext = new ExpressionContext();

		/* Trim enclosing brackets and whitespace-s.
		*  Track and trim inverses */
		line = trim(line, expressionContext);

		/* Check for EXPLICIT TYPE CONVERSION, like "STD_LOGIC_VECTOR(...)",
		* "UNSIGNED(...)" and "CONV_INTEGER(...).
		* Cut off the ETC. "*/
		while (isExplicitTypeConversion(line)) {
			line = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")).trim();
		}

		/* Check for User Defined Function and create it if found */
		if (isUserDefinedFunction(line)) {
			// Parse operator
			String userDefinedFunction = extractPureOperand(line, true);
			// Parse operands and create UserDefinedFunction
			String[] operandStrings = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")).trim().split(",");
			UserDefinedFunction userDefFunction =
					new UserDefinedFunction(userDefinedFunction, expressionContext.isInverted, operandStrings.length);
			for (String operandString : operandStrings) {
				userDefFunction.addOperand(buildExpression(operandString.trim()));
			}
			return userDefFunction;
		}

		/* Determine Operator and determine where it appears in the string */
		// Define highestPrecedenceOperator for the current level(string)
		Region[] validRegions = getValidRegions(line);
		Operator operator = Operator.getHighestPrecedenceOperator(new Region().toStringArray(validRegions));

		if (operator == null) {

			/* Parse RANGE */
			Indices range = null;
			AbstractOperand dynamicRange = null;
			try {
				range = buildRange(line);
			} catch (Exception e) {
				dynamicRange = extractDynamicRange(line);
				if (dynamicRange == null || !(dynamicRange instanceof OperandImpl) ||
						!variableNames.contains(((OperandImpl) dynamicRange).getName())) {
					throw e;
				}
			}
			String pureOperand = extractPureOperand(line, range != null || dynamicRange != null);

			return replaceAliases(dynamicRange == null ?
					new OperandImpl(pureOperand, range, expressionContext.isInverted) :
					new OperandImpl(pureOperand, (OperandImpl) dynamicRange, expressionContext.isInverted)
			);
		} else {
			Expression expression = new Expression(operator, expressionContext.isInverted);
			// Get subString-s of operands
			String[] operandStrings = splitToOperands(line, operator, validRegions);
			for (String operandString : operandStrings) {
				expression.addOperand(buildExpression(operandString));
			}
			return expression;
		}
	}

	private AbstractOperand extractDynamicRange(String line) throws Exception {
		if (line.contains("(") && line.contains(")")) {
			line = line.substring(line.indexOf("(") + 1, line.lastIndexOf(")")).trim();
			return buildExpression(line);
		}
		return null;
	}

	private AbstractOperand replaceAliases(OperandImpl operand) {

		if (!aliasByName.containsKey(operand.getName())) {
			return operand;
		}

		if (operand.isRange()) {
			//todo: range ALIAS, see VHDL2008_comments.pdf => p.105
			throw new RuntimeException("Implement me: RANGE ALIAS. todo: merge indices");
		}

		OperandImpl actualOperand = aliasByName.get(operand.getName()).getActual();

		return new OperandImpl(actualOperand.getName(), actualOperand.getRange(), operand.isInverted());
	}

	private boolean isUserDefinedFunction(String line) throws Exception {
		return USER_DEFINED_CONV.matcher(line).matches()
				&& !variableNames.contains(extractPureOperand(line, true))
				&& getClosingIndex(line, line.indexOf("(")) == line.length() - 1;
	}

	static boolean isExplicitTypeConversion(String line) throws Exception {
		/* Check to match the conversion regexp-s */
		return (STD_LOGIC_VECTOR_CONV.matcher(line).matches()
				|| UNSIGNED_CONV.matcher(line).matches()
				|| CONV_INTEGER_CONV.matcher(line).matches()
				|| TO_INTEGER_CONV.matcher(line).matches())
				&& getClosingIndex(line, line.indexOf("(")) == line.length() - 1;
	}

	private static String extractPureOperand(String line, boolean hasRange) {
		return hasRange ? line.substring(0, line.indexOf("(")).trim() : line;
	}

	/**
	 * @param line source line of the following forms:
	 *             <br> d_in ( 8 downto 1 )
	 *             <br> d_in ( 1 to 8 )
	 *             <br>
	 *             <br> d_in ( 0 )
	 *             <br>
	 *             <br> 32767 DOWNTO -32768 *
	 *             <br> 0 TO 3  *
	 *             <br> (Processor_width -1) downto -1 *
	 * @return range if present or  <code>null</code> if range is not present
	 * @throws Exception if {@link #evaluateNumerically(String, String)}
	 */
	public Indices buildRange(String line) throws Exception {
		if (BIT_RANGE_PATTERN.matcher(line).matches()) {
			/* d_in ( 8 downto 1 ) */
			/* d_in ( 1 to 8 ) */
			RangeDeclaration rangeDeclaration =
					parseIndicesDeclaration(line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim());
			int lowestIndex = evaluateNumerically(rangeDeclaration.lowestIndex, line);
			int highestIndex = evaluateNumerically(rangeDeclaration.highestIndex, line);
			return new Indices(highestIndex, lowestIndex, rangeDeclaration.isDescending);

		} else if (SINGLE_BIT_PATTERN.matcher(line).matches()) {
			/* d_in ( 0 ) */
			int index = evaluateNumerically(line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim(), line);
			return new Indices(index, index);

		} else if (PURE_BIT_RANGE_PATTERN.matcher(line).matches()) {
			/* 32767 DOWNTO -32768 */
			/* 0 TO 3  */
			/* (Processor_width -1) downto -1 */
			RangeDeclaration rangeDeclaration = parseIndicesDeclaration(line);
			int lowestIndex = evaluateNumerically(rangeDeclaration.lowestIndex, line);
			int highestIndex = evaluateNumerically(rangeDeclaration.highestIndex, line);
			return new Indices(highestIndex, lowestIndex, rangeDeclaration.isDescending);

		} else return null;
	}

	private static RangeDeclaration parseIndicesDeclaration(String rangeAsString) {
		/* 32767 DOWNTO -32768 */
		/* 0 TO 3 */
		String[] indicesAsString;
		if (rangeAsString.contains(" DOWNTO ")) {
			// DESCENDING order
			indicesAsString = rangeAsString.split(" DOWNTO ");
			return new RangeDeclaration(indicesAsString[1], indicesAsString[0], true);
		} else {
			//ASCENDING order
			indicesAsString = rangeAsString.split(" TO ");
			return new RangeDeclaration(indicesAsString[0], indicesAsString[1], false);
		}
	}


	private String[] splitToOperands(String line, Operator operator, Region... validRegions) {
		List<String> operandsList = new LinkedList<String>();

		/* Get Valid Operator Regions */
		Region[] operatorValidRegions = getValidOperatorRegions(operator, validRegions);

		short startIndex = 0;
		short endIndex;
		for (Region operatorValidRegion : operatorValidRegions) {
			/* END of operandString is the start of the next operatorRegion */
			endIndex = operatorValidRegion.start;
			/* Add new Operand String */
			String operandString = line.substring(startIndex, endIndex).trim();
			if (operandString.length() > 0) operandsList.add(operandString);
			/* START of operandString is the end of the previous operatorRegion */
			startIndex = operatorValidRegion.end;
		}

		/* Add operand String that may come after the last operatorRegion */
		if (startIndex < line.length()) {
			String operandString = line.substring(startIndex);
			if (operandString.length() > 0) operandsList.add(operandString);
		}

		return operandsList.toArray(new String[operandsList.size()]);
	}

	/**
	 * Extracts regions of valid operator.
	 * Regions of valid operator are those subString-s that both represent the operator
	 * delimiter and reside in {@code validRegions}.
	 * <p/>
	 * <<< VOTO0 ='1' AND VOTO1 ='1' AND VOTO2 ='1' AND VOTO3 ='1' >>>
	 *
	 * @param operator	 to search for
	 * @param validRegions where to look for operator
	 * @return regions of valid operator (subString-s from validRegions that contain specified operator)
	 */
	private Region[] getValidOperatorRegions(Operator operator, Region... validRegions) {
		List<Region> regionsList = new LinkedList<Region>();
		String operatorDelim = operator.getDelim();

		/* Check every validRegion to contain operator */
		for (Region region : validRegions) {
			short startIndex;
			short endIndex;
			int lineStartIndex = 0;
			String regionString = region.toString();
			/* If the region contains the operator, then add new ValidOperatorRegion
			* to the list and check the rest of the region to contain the operator */
			while (Operator.containsOperator(operator, regionString.substring(lineStartIndex))) {
				short operatorIndex = (short) regionString.indexOf(operatorDelim, lineStartIndex);
				startIndex = (short) (region.start + operatorIndex);
				endIndex = (short) (startIndex + operatorDelim.length());
				regionsList.add(new Region(startIndex, endIndex, region.baseLine));
				lineStartIndex = operatorIndex + operatorDelim.length();
			}
		}

		return regionsList.toArray(new Region[regionsList.size()]);
	}

	/**
	 * Extracts valid regions from {@code line} where to search
	 * for the operator ( with the highest precedence).
	 * Invalid regions are the subString-s enclosed in brackets.
	 *
	 * @param line where to extract the regions from
	 * @return array of regions
	 * @throws Exception if specified line is a malformed expression
	 *                   (closing bracket is missing)
	 */
	Region[] getValidRegions(String line) throws Exception {
		List<Region> validRegions = new LinkedList<Region>();
		short startIndex = 0;

		while (true) {
			if (startIndex > line.length() - 1) break;

			/*
			* SubLine contains bracket. Add new ValidRegion and
			* adjust the startIndex for the next SubLine to the
			* index of the closing bracket.
			* */
			if (line.substring(startIndex).contains("(")) {
				/* Get OPEN bracket index */
				int openBracketIndex = line.indexOf("(", startIndex);
				/* Add new valid region */
				if (openBracketIndex - startIndex > 0) {
					validRegions.add(new Region(startIndex, (short) openBracketIndex, line));
				}
				/* Get CLOSE bracket index */
				int closeBracketIndex = getClosingIndex(line, openBracketIndex);
				if (closeBracketIndex == -1) {
					throw new Exception("The following expression is malformed (closing bracket is missing): " + line);
				}
				/* Set new StartIndex for subLine (right after the closing index) */
				startIndex = (short) (closeBracketIndex + 1);
				/* Process next SubLine */
				continue;
			}

			/* SubLine doesn't contain brackets.*/
			/* Add the rest of the line, if any*/
			if (line.length() - startIndex > 0) {
				validRegions.add(new Region(startIndex, (short) line.length(), line));
			}
			/* Terminate the loop */
			break;
		}

		return validRegions.toArray(new Region[validRegions.size()]);
	}

	private String trim(String line, ExpressionContext expressionContext) {
		boolean trimNeeded = true;
		boolean isInverted = false;
		while (trimNeeded) {
			/* Trim BRACKETS */
			line = trimEnclosingBrackets(line);
			/* Trim INVERSE */
			if (line.toUpperCase().startsWith("NOT ")) {
				line = line.substring(4).trim();
				isInverted = !isInverted;
				continue;
			}
			trimNeeded = false;
		}
		expressionContext.setInverted(isInverted);
		return line;
	}


	public static String trimEnclosingBrackets(String line) {
		boolean trimNeeded = true;

		while (trimNeeded) {
			/* Trim line */
			line = line.trim();
			/* If line doesn't start or end with a bracket, then do nothing */
			if (!line.startsWith("(") || !line.endsWith(")")) break;
			/* Find closing index */
			int closeIndex = 0;
			try {
				closeIndex = getClosingIndex(line, 0);
			} catch (Exception e) {/* Do nothing. Exception is guaranteed not to be thrown here. */}
			/* Trim brackets, if the closing bracket is the last char in the string */
			if (closeIndex == line.length() - 1) {
				line = line.substring(1, closeIndex);
				continue;
			}

			/* Trim is not needed anymore */
			trimNeeded = false;
		}

		return line;
	}

//	// todo: Below is another version of trimEnclosingBrackets(). It must be considered which is faster.
//	private static String trimEnclosingBrackets(String propertyBody) {
//		propertyBody = propertyBody.trim();
//
//		while (propertyBody.startsWith("(")) {
//
//			/* If the body IS NOT enclosed with brackets at all, then return it (don't process) */
//			if (!propertyBody.endsWith(")")) return propertyBody;
//
//			/* If the body IS enclosed with brackets, then check for the following cases:
//						 * 1) (      () ) ------  brackets must be trimmed
//						 * 2) (  ()   ()  )-----  brackets must be trimmed
//						 * 3) ()    ()   -------  brackets must NOT be trimmed*/
//			byte bracketCount = 0;
//			String trimmedPropertyBody = propertyBody.substring(1, propertyBody.length() - 1).trim();
//			for (char aChar : trimmedPropertyBody.toCharArray()) {
//				if (aChar == '(') bracketCount++;
//				else if (aChar == ')') {
//					if (bracketCount == 0)
//						return propertyBody; /* If this ')' closes previously trimmed '(', then the trim is not accepted! Undo trim.*/
//					else bracketCount--;
//				}
//			}
//
//			propertyBody = trimmedPropertyBody;
//		}
//		return propertyBody;
//	}

	/**
	 * For the specified <code>line</code>, returns the <i>index</i>
	 * of the closing character for the starting character defined with
	 * the <code>startingCharIndex</code>.
	 * <p/>
	 * Currently supported pairs of <b>{ starting : ending }</b> characters:<br>
	 * 1) <b>'('</b> : <b>')'</b><br>
	 * 2) <b>'{'</b> : <b>'}'</b><br>
	 *
	 * @param line			  where to search the starting/closing characters
	 * @param startingCharIndex index of the starting character
	 *                          the closing character has to be found for
	 * @return index of the closing character, if it is found,
	 *         or <code>-1</code> if the closing character has
	 *         not been found (either is missing at all or the
	 *         starting character is the last character in the
	 *         line).
	 * @throws Exception if the specified <code>startingCharIndex</code> is
	 *                   beyond the length of the string, or the starting character
	 *                   is not supported.
	 */
	private static int getClosingIndex(String line, int startingCharIndex) throws Exception {
		/* Check for the startingCharIndex to conform with the length of the line */
		if (startingCharIndex > line.length() - 1) {
			throw new IndexOutOfBoundsException("Index of the starting char is beyond the length of the string:" +
					"\nStarting char index: " + startingCharIndex +
					"\nString length: " + line.length() +
					"\nString: " + line);
		} else if (startingCharIndex == line.length() - 1) return -1;
		/* Define START_CHAR and END_CHAR */
		char startChar = line.charAt(startingCharIndex);
		char endChar;
		switch (startChar) {
			case '(':
				endChar = ')';
				break;
			case '{':
				endChar = '}';
				break;
			default:
				throw new Exception("Cannot determine ending character for the following UNKNOWN starting character: \'" + startChar + "\'");
		}
		Stack<Boolean> openStack = new Stack<Boolean>();
		openStack.push(true);

		for (int index = startingCharIndex + 1; index < line.length(); index++) {
			/* Check for STARTING and ENDING characters */
			if (line.charAt(index) == startChar) openStack.push(true);
			else if (line.charAt(index) == endChar) openStack.pop();
			/* Check for CLOSING character */
			if (openStack.isEmpty()) return index;
		}
		return -1;
	}

	/**
	 * @param expressionLine	   expression line to calculate value for
	 * @param expressionLineSource source line of the expression, used when
	 *                             throwing Exception
	 * @return numerical value of the specified expression if it can be
	 *         calculated
	 * @throws Exception if the value of expression could not be calculated
	 */
	public int evaluateNumerically(String expressionLine, String expressionLineSource) throws Exception {
		OperandValueCalculator.ValueHolder valueHolder = valueCalculator.calculateValueFor(buildExpression(expressionLine));
		/* If value could not be calculated, throw an error */
		if (!valueHolder.isValueCalculated())
			throw new Exception("Could not calculate value for operand \"" + valueHolder.getUnknownOperand() +
					"\" in expression \"" + expressionLineSource + "\"");
		return valueHolder.getValue().intValue();
	}

	public Expression unfoldBoolean(OperandImpl operand) throws Exception {
		Expression expression = new Expression(Operator.EQ, false);

		expression.addOperand(new OperandImpl(operand.getName(), operand.getRange(), false));
		expression.addOperand(operand.isInverted() ? buildExpression("\'0\'") : buildExpression("\'1\'"));

		return expression;
	}

	public void addAlias(Alias alias) {
		aliasByName.put(alias.getName(), alias);
	}

	/* AUXILIARY classes */

	@SuppressWarnings({"InstanceVariableNamingConvention"})
	class Region {
		short start;
		short end;
		String baseLine;

		Region(short start, short end, String baseLine) {
			this.start = start;
			this.end = end;
			this.baseLine = baseLine;
		}

		/**
		 * Empty constructor. Used only for calling toStringArray() method.
		 */
		Region() {
		}

		public String toString() {
			return baseLine.substring(start, end);
		}

		private String[] toStringArray(Region... regions) {
			String[] array = new String[regions.length];
			for (int i = 0; i < regions.length; i++) {
				array[i] = regions[i].toString();
			}
			return array;
		}

	}

	/**
	 * Keeps track of inversions
	 */
	private class ExpressionContext {
		private boolean isInverted;

		public void setInverted(boolean isInverted) {
			this.isInverted = isInverted;
		}
	}

}
