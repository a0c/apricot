package base.vhdl.structure;

import base.Range;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
 */
public class OperandImplTest {

	@Test
	public void correctHashCode() {
		OperandImpl operand1 = new OperandImpl("firstOperand", new Range(5, 0), false);
		OperandImpl operand11 = new OperandImpl("firstOperand", new Range(5, 0), false);
		OperandImpl operand2 = new OperandImpl("firstOperand", new Range(8, 0), false);
		assertFalse(operand1.hashCode() == operand2.hashCode());
		assertTrue(operand1.hashCode() == operand11.hashCode());
	}

	@Test
	public void correctEquals() {
		OperandImpl operand1 = new OperandImpl("firstOperand", new Range(5, 0), false);
		OperandImpl operand11 = new OperandImpl("firstOperand", new Range(5, 0), false);
		OperandImpl operand2 = new OperandImpl("firstOperand", new Range(8, 0), false);
		assertFalse(operand1.equals(operand2));
		assertTrue(operand1.equals(operand11));
	}

	@Test
	public void correctToString() {
		Range nullRange = null;
		OperandImpl operand1 = new OperandImpl("firstOperand", new Range(5, 0), false);
		OperandImpl operand11 = new OperandImpl("firstOperand", new Range(5, 5), false);
		OperandImpl operand2 = new OperandImpl("secondOperand", nullRange, false);
		OperandImpl operand3 = new OperandImpl("thirdOperand", new Range(10, 1), true);
		assertEquals("firstOperand(5 DOWNTO 0)", operand1.toString());
		assertEquals("firstOperand(5)", operand11.toString());
		assertEquals("secondOperand", operand2.toString());
		assertEquals("NOT thirdOperand(10 DOWNTO 1)", operand3.toString());
	}
}
