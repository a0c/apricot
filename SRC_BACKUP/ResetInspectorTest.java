package base.vhdl.visitors;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static junit.framework.Assert.assertEquals;
import base.vhdl.structure.OperandImpl;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 02.10.2008
 * <br>Time: 12:39:05
 */
public class ResetInspectorTest {

    @Test
    public void correctCalcReferenceOperandIndex() {
        assertEquals(1, BehDDResetInspector.calcReferenceOperandIndex(0));
        assertEquals(0, BehDDResetInspector.calcReferenceOperandIndex(1));
    }

    @Test
    public void correctConvertOperandToString() {
        BehDDResetInspector ri = new BehDDResetInspector();
        assertEquals(new Integer(1), ri.parseConstantValue(new OperandImpl("1")));
        assertEquals(new Integer(1), ri.parseConstantValue(new OperandImpl("'1'")));
        assertEquals(new Integer(0), ri.parseConstantValue(new OperandImpl("0")));
        assertEquals(new Integer(0), ri.parseConstantValue(new OperandImpl("'0'")));
        assertEquals(new Integer(10), ri.parseConstantValue(new OperandImpl("10")));
        assertEquals(new Integer(2), ri.parseConstantValue(new OperandImpl("'10'")));
        assertEquals(new Integer(1000), ri.parseConstantValue(new OperandImpl("1000")));
        assertEquals(new Integer(8), ri.parseConstantValue(new OperandImpl("'1000'")));
        assertNull(ri.parseConstantValue(new OperandImpl("operandName")));
        assertNull(ri.parseConstantValue(new OperandImpl("CONSTANT_")));

    }

    @Test
    public void correctIntegerComparison() {
        assertTrue(new Integer(1) == 1);
        assertTrue(new Integer(0) == 0);
    }
}
