package base.vhdl.visitors;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import parsers.ExpressionBuilder;
import base.vhdl.structure.Expression;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 08.10.2008
 * <br>Time: 20:55:44
 */
public class ClockEventRemoverTest {
    private static final String[] correctClockExpressions = {
            "Clk='0'",
            "Clk='1'",
            "CLOCK='1'",
            "CLOCK='0'",
            "Clock = someValue" // NB!
    };
    private static final String[] incorrectClockExpressions = {
            "E='0'",
            "smth < 1"
    };

    /*
     * 1)   IF clock'event and clock='1' THEN
     * 2)   elsif CLOCK'event and CLOCK='1' then
     * */

    @Test
    public void correctCompareClockExpressionAccepted() throws Exception {
        Expression[] correctExprs = buildExpressions(correctClockExpressions);
        for (Expression correctExpr : correctExprs) {
            assertTrue("Expression " + correctExpr + " is regarded to be incorrect compareClockExpression",
                    ClockEventRemover.isComparingClockForEquality(correctExpr));
        }
    }

    @Test
    public void incorrectCompareClockExpressionRejected() throws Exception {
        Expression[] incorrectExprs = buildExpressions(incorrectClockExpressions);
        for (Expression incorrectExpr : incorrectExprs) {
            assertTrue("Expression " + incorrectExpr + " is regarded to be correct compareClockExpression", 
                    !ClockEventRemover.isComparingClockForEquality(incorrectExpr));
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
