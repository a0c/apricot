package base.hldd.structure.variables;

import base.vhdl.structure.Operator;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 27.05.2010
 * <br>Time: 14:37:08
 */
public class FunctionVariableTest {
	@Test
	public void testGetComparator() throws Exception {
		final int before = -1;
		final int equal = 0;
		final int after = 1;
		
		Comparator<FunctionVariable> comparator = FunctionVariable.getComparator();
		assertNotNull("FunctionVariable.getComparator() returned null", comparator);

		FunctionVariable fun1 = new FunctionVariable(Operator.AND, 1);
		FunctionVariable fun2 = new FunctionVariable(Operator.AND, 3);
		assertEquals("Error when comparing " + fun1.getName() + " and " + fun2.getName(), before, comparator.compare(fun1, fun2));
		assertEquals("Error when comparing " + fun2.getName() + " and " + fun1.getName(), after, comparator.compare(fun2, fun1));
		assertEquals("Error when comparing " + fun1.getName() + " and " + fun1.getName(), equal, comparator.compare(fun1, fun1));
		assertEquals("Error when comparing " + fun2.getName() + " and " + fun2.getName(), equal, comparator.compare(fun2, fun2));

		FunctionVariable fun3 = new FunctionVariable(Operator.XOR, 3);
		assertEquals("Error when comparing " + fun1.getName() + " and " + fun3.getName(), before, comparator.compare(fun1, fun3));
		assertEquals("Error when comparing " + fun3.getName() + " and " + fun1.getName(), after, comparator.compare(fun3, fun1));
		assertEquals("Error when comparing " + fun3.getName() + " and " + fun3.getName(), equal, comparator.compare(fun3, fun3));

	}
}
