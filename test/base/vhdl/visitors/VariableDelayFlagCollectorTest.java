package base.vhdl.visitors;

import base.vhdl.structure.AbstractOperand;
import base.vhdl.structure.OperandImpl;
import org.junit.Test;
import parsers.ExpressionBuilder;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Anton Chepurov
 */
public class VariableDelayFlagCollectorTest {

	@Test
	public void correctOperandNamesExtracted() throws Exception {
		AbstractOperand resetExpr = buildExpressionsFrom("RESET = '1'")[0];
		OperandImpl[] resetOperands = createFrom("'1'", "RESET");
		Set<OperandImpl> resOperandCollection = VariableDelayFlagCollector.extractOperandsFrom(resetExpr);
		OperandImpl[] extractedRstOperands = resOperandCollection.toArray(new OperandImpl[resOperandCollection.size()]);

		AbstractOperand clkExpr = buildExpressionsFrom("CLOCK'EVENT AND CLOCK = '1'")[0];
		OperandImpl[] clkOperands = createFrom("'1'", "CLOCK", "CLOCK'EVENT");
		Set<OperandImpl> clkOperandCollection = VariableDelayFlagCollector.extractOperandsFrom(clkExpr);
		OperandImpl[] extractedClkOperands = clkOperandCollection.toArray(new OperandImpl[clkOperandCollection.size()]);

		AbstractOperand stExpr = buildExpressionsFrom("STATO")[0];
		OperandImpl[] stOperands = createFrom("STATO");
		Set<OperandImpl> stOperandCollection = VariableDelayFlagCollector.extractOperandsFrom(stExpr);
		OperandImpl[] extractedStOperands = stOperandCollection.toArray(new OperandImpl[stOperandCollection.size()]);

		AbstractOperand cmplExpr = buildExpressionsFrom("RES AND ( ENA = '1' OR DATA_OUT < '100011')")[0];
		OperandImpl[] cmplOperands = createFrom("'1'", "'100011'", "DATA_OUT", "ENA", "RES");
		Collection<OperandImpl> cmplOperandCollection = VariableDelayFlagCollector.extractOperandsFrom(cmplExpr);
		OperandImpl[] extractedCmplOperands = cmplOperandCollection.toArray(new OperandImpl[cmplOperandCollection.size()]);

		assertArrayEquals(resetOperands, extractedRstOperands);
		assertArrayEquals(clkOperands, extractedClkOperands);
		assertArrayEquals(stOperands, extractedStOperands);
		assertArrayEquals(cmplOperands, extractedCmplOperands);
	}

	private OperandImpl[] createFrom(String... names) {
		OperandImpl[] operands = new OperandImpl[names.length];
		for (int i = 0, namesLength = names.length; i < namesLength; i++) {
			operands[i] = new OperandImpl(names[i]);
		}
		return operands;
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
