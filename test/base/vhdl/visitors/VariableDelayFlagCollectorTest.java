package base.vhdl.visitors;

import org.junit.Test;
import static org.junit.Assert.*;
import parsers.ExpressionBuilder;
import base.vhdl.structure.Expression;
import base.vhdl.structure.AbstractOperand;

import java.util.Collection;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 21.10.2008
 * <br>Time: 15:00:19
 */
public class VariableDelayFlagCollectorTest {

    @Test
    public void correctSetOfOperandNamesExtracted() throws Exception {
        AbstractOperand resetExpr = buildExpressionsFrom("RESET = '1'")[0];
        String[] resetOperands = {"'1'", "RESET"};
        Collection<String> resOperandCollection = VariableDelayFlagCollector.extractSetOfOperandNamesFrom(resetExpr);
        String[] extractedRstOperands = resOperandCollection.toArray(new String[resOperandCollection.size()]);

        AbstractOperand clkExpr = buildExpressionsFrom("CLOCK'EVENT AND CLOCK = '1'")[0];
        String[] clkOperands = {"'1'", "CLOCK", "CLOCK'EVENT"};
        Collection<String> clkOperandCollection = VariableDelayFlagCollector.extractSetOfOperandNamesFrom(clkExpr);
        String[] extractedClkOperands = clkOperandCollection.toArray(new String[clkOperandCollection.size()]);

        AbstractOperand stExpr = buildExpressionsFrom("STATO")[0];
        String[] stOperands = {"STATO"};
        Collection<String> stOperandCollection = VariableDelayFlagCollector.extractSetOfOperandNamesFrom(stExpr);
        String[] extractedStOperands = stOperandCollection.toArray(new String[stOperandCollection.size()]);

        AbstractOperand cmplExpr = buildExpressionsFrom("RES AND ( ENA = '1' OR DATA_OUT < '100011')")[0];
        String[] cmplOperands = {"'1'", "'100011'", "DATA_OUT", "ENA", "RES"};
        Collection<String> cmplOperandCollection = VariableDelayFlagCollector.extractSetOfOperandNamesFrom(cmplExpr);
        String[] extractedCmplOperands = cmplOperandCollection.toArray(new String[cmplOperandCollection.size()]);

        assertArrayEquals(resetOperands, extractedRstOperands);
        assertArrayEquals(clkOperands, extractedClkOperands);
        assertArrayEquals(stOperands, extractedStOperands);
        assertArrayEquals(cmplOperands, extractedCmplOperands);
    }

    private AbstractOperand[] buildExpressionsFrom(String... expressionLines) throws Exception {
        AbstractOperand[] abstractOperands = new AbstractOperand[expressionLines.length];
        ExpressionBuilder builder = new ExpressionBuilder();
        for (int i = 0; i < expressionLines.length; i++) {
            abstractOperands[i] = builder.buildExpression(expressionLines[i]);
        }
        return abstractOperands;
    }
}
