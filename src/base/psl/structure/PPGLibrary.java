package base.psl.structure;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.nodes.Node;
import base.hldd.structure.nodes.TemporalNode;
import parsers.ExpressionBuilder;

/**
 * @author Anton Chepurov
 */
public class PPGLibrary {

	PSLOperator[] pslOperators;

	public PPGLibrary(PSLOperator[] pslOperators) {
		this.pslOperators = pslOperators;
	}

	/**
	 * Extracts operator with lowest precedence.
	 * All operators in the library are applied one by one for matching
	 * with the specified <code>propertyBody</code>.
	 *
	 * @param propertyBody property body line to extract operator from
	 * @return pslOperator with the lowest precedence, if found. If
	 *         none of the operators in the library match, <code>null</code>
	 *         is returned.
	 */
	public PSLOperator extractOperator(String propertyBody) {
		propertyBody = ExpressionBuilder.trimEnclosingBrackets(propertyBody);

		for (PSLOperator pslOperator : pslOperators) {

			if (propertyBody.matches(pslOperator.getMatchingRegexp())) {
				/* If body matches operator's regexp, then it's a potential matching.
				*
				* Check if the operator is enclosed with brackets :
				* Note, that enclosing brackets have been previously trimmed (method "trimEnclosingBrackets"),
				* so if the operator is still enclosed, then these are some internal brackets
				* with the highest possible precedence, which makes the currently analyzed operator's precedence invalid.
				* Thus, a designer can use brackets to break the precedence of operators in order to make his or her
				* property more flexible and to exactly achieve the desired behaviour of the property.
				* */

				//todo: what if operator is at the beginning of the body and is parsed first (in such a way that the whole ending substring is treated as an operand)??
				/* Operator is somewhere in the middle of the body:*/
				/* If operator is enclosed with brackets, then splitting the body into operands will produce
				* operands with pairless brackets. */
				boolean validOperator = true;
				String[] operands = pslOperator.extractOperandLinesFrom(propertyBody);
				for (String operand : operands) {
					if (containsPairlessBracket(operand)) {
						validOperator = false;
						break;
					}
				}

				if (validOperator) return pslOperator;

			}

		}

		return null;
	}

	private boolean containsPairlessBracket(String operandBody) {
		operandBody = operandBody.trim();

		byte bracketCount = 0;
		for (char aChar : operandBody.toCharArray()) {
			if (aChar == '(') bracketCount++;
			else if (aChar == ')') bracketCount--;
		}

		return bracketCount != 0;
	}

	public void setModelToPPG(String ppgName, BehModel model) {
		for (PSLOperator pslOperator : pslOperators) {
			if (pslOperator.getName().equalsIgnoreCase(ppgName)) {
				pslOperator.setModel(model);
				/* Search for window */
				Node rootNode = pslOperator.getPropertyGraph().getGraph().getRootNode();
				if (rootNode instanceof TemporalNode) {
					String[] windowPlaceholders = ((TemporalNode) rootNode).getWindowPlaceholders();
					pslOperator.setWindowPlaceholders(windowPlaceholders);
//					if (areFixed(windowPlaceholders)) {
//						pslOperator.setWithWindow(true);
//					}
				}
				break;
			}
		}
	}

	/**
	 * windowPlaceholders are fixed, if both placeholders are numbers.
	 * If at least one placeholder is a String (not a number),
	 * then windowPlaceholders are not fixed.
	 * todo: Create class "WindowPlaceholders"
	 *
	 * @param windowPlaceholders
	 * @return
	 */
	@Deprecated
	private static boolean areFixed(String[] windowPlaceholders) {
		for (String windowPlaceholder : windowPlaceholders) {
			try {
				Integer.parseInt(windowPlaceholder);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
}
