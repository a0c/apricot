package parsers.psl;

import base.psl.structure.*;

import java.util.List;
import java.util.LinkedList;

import parsers.ExpressionBuilder;

/**
 * @author Anton Chepurov
 */
public class StructureBuilder {

	private PPGLibrary library;
	private List<Property> propertyList;

	private ExpressionBuilder booleanExpressionBuilder;

	/**
	 * @param library PPG Library that can extract operator with highest precedence from a specified line expression
	 */
	public StructureBuilder(PPGLibrary library) {
		this.library = library;
		propertyList = new LinkedList<Property>();
		booleanExpressionBuilder = new ExpressionBuilder();
	}

	public void buildProperty(String propertyName, VerificationDirective directive, String propertyBody, String propertySourceLine) throws Exception {

		propertyList.add(new Property(propertySourceLine, propertyName, directive, buildExpression(propertyBody)));
	}

	private AbstractExpression buildExpression(String expressionLine) throws Exception {

		expressionLine = ExpressionBuilder.trimEnclosingBrackets(expressionLine);

		PSLOperator pslOperator = library.extractOperator(expressionLine);

		if (pslOperator == null) {
			/* HDL Boolean ==> OperandImpl */
			return new OperandImpl(booleanExpressionBuilder.buildExpression(expressionLine));
		} else {
			/* Temporal Operand ==> ExpressionImpl */
			ExpressionImpl expression = new ExpressionImpl(pslOperator, Range.parseRangeNEXT(expressionLine));//todo... before was "if (pslOperator.isWithWindow())"

			String[] operandLines = pslOperator.extractOperandLinesFrom(expressionLine);
			for (String operandLine : operandLines) {
				expression.addOperand(buildExpression(operandLine));
			}

			return expression;
		}

	}

	public Property[] getProperties() {
		return propertyList.toArray(new Property[propertyList.size()]);
	}
}
