package base.vhdl.visitors;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import parsers.ExpressionBuilder;
import base.vhdl.structure.Expression;
import ui.ConfigurationHandler;

/**
 * @author Anton Chepurov
 */
public class ClockEventRemoverTest {
	private static final String[] CORRECT_CLOCK_EXPRESSIONS = {
			"Clk='0'",
			"Clk='1'",
			"CLOCK='1'",
			"CLOCK='0'",
			"Clock = someValue" // NB!
	};
	private static final String[] INCORRECT_CLOCK_EXPRESSIONS = {
			"E='0'",
			"smth < 1"
	};

	/*
		 * 1)   IF clock' event and clock='1' THEN
		 * 2)   elsif CLOCK' event and CLOCK='1' then
		 * */

	@Test
	public void correctClockExpressionAccepted() throws Exception {
		Expression[] correctExpressions = buildExpressions(CORRECT_CLOCK_EXPRESSIONS);
		ConfigurationHandler config = mock(ConfigurationHandler.class);
		when(config.isClockName(anyString())).thenReturn(true);
		ClockEventRemover clockEventRemover = new ClockEventRemover(config);
		for (Expression correctExpression : correctExpressions) {
			assertTrue("Expression " + correctExpression + " is regarded to be incorrect compareClockExpression",
					clockEventRemover.isComparingClockForEquality(correctExpression));
		}
	}

	@Test
	public void incorrectClockExpressionRejected() throws Exception {
		Expression[] incorrectExpressions = buildExpressions(INCORRECT_CLOCK_EXPRESSIONS);
		ConfigurationHandler config = mock(ConfigurationHandler.class);
		when(config.isClockName(anyString())).thenReturn(false);
		ClockEventRemover clockEventRemover = new ClockEventRemover(config);
		for (Expression incorrectExpression : incorrectExpressions) {
			assertTrue("Expression " + incorrectExpression + " is regarded to be correct compareClockExpression",
					!clockEventRemover.isComparingClockForEquality(incorrectExpression));
		}
	}

	private static Expression[] buildExpressions(String[] expressionsAsString) throws Exception {
		Expression[] expressions = new Expression[expressionsAsString.length];
		ExpressionBuilder builder = new ExpressionBuilder();
		for (int i = 0; i < expressionsAsString.length; i++) {
			expressions[i] = (Expression) builder.buildExpression(expressionsAsString[i]);
		}
		return expressions;
	}
}
