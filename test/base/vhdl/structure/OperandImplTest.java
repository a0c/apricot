package base.vhdl.structure;

import org.junit.Test;
import static org.junit.Assert.*;
import base.Indices;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 13.10.2008
 * <br>Time: 15:15:07
 */
public class OperandImplTest {

    @Test
    public void correctHashCode() {
        OperandImpl operand1 = new OperandImpl("firstOperand", new Indices(5, 0), false);
        OperandImpl operand11 = new OperandImpl("firstOperand", new Indices(5, 0), false);
        OperandImpl operand2 = new OperandImpl("firstOperand", new Indices(8, 0), false);
        assertFalse(operand1.hashCode() == operand2.hashCode());
        assertTrue(operand1.hashCode() == operand11.hashCode());
    }

    @Test
    public void correctEquals() {
        OperandImpl operand1 = new OperandImpl("firstOperand", new Indices(5, 0), false);
        OperandImpl operand11 = new OperandImpl("firstOperand", new Indices(5, 0), false);
        OperandImpl operand2 = new OperandImpl("firstOperand", new Indices(8, 0), false);
        assertFalse(operand1.equals(operand2));
        assertTrue(operand1.equals(operand11));
    }

    @Test
    public void correctToString() {
        Indices nullIndices = null;
        OperandImpl operand1 = new OperandImpl("firstOperand", new Indices(5, 0), false);
        OperandImpl operand11 = new OperandImpl("firstOperand", new Indices(5, 5), false);
        OperandImpl operand2 = new OperandImpl("secondOperand", nullIndices, false);
        OperandImpl operand3 = new OperandImpl("thirdOperand", new Indices(10, 1), true);
        assertEquals("firstOperand(5 DOWNTO 0)", operand1.toString());
        assertEquals("firstOperand(5)", operand11.toString());
        assertEquals("secondOperand", operand2.toString());
        assertEquals("NOT thirdOperand(10 DOWNTO 1)", operand3.toString());
    }
}
