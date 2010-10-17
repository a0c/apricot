package base.hldd.structure.variables;

import base.vhdl.structure.Operator;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
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
		assertEquals("Error when comparing " + fun1.getName() + " and " + fun2.getName(), before, cast(comparator.compare(fun1, fun2)));
		assertEquals("Error when comparing " + fun2.getName() + " and " + fun1.getName(), after, cast(comparator.compare(fun2, fun1)));
		assertEquals("Error when comparing " + fun1.getName() + " and " + fun1.getName(), equal, cast(comparator.compare(fun1, fun1)));
		assertEquals("Error when comparing " + fun2.getName() + " and " + fun2.getName(), equal, cast(comparator.compare(fun2, fun2)));

		FunctionVariable fun3 = new FunctionVariable(Operator.XOR, 3);
		assertEquals("Error when comparing " + fun1.getName() + " and " + fun3.getName(), before, cast(comparator.compare(fun1, fun3)));
		assertEquals("Error when comparing " + fun3.getName() + " and " + fun1.getName(), after, cast(comparator.compare(fun3, fun1)));
		assertEquals("Error when comparing " + fun3.getName() + " and " + fun3.getName(), equal, cast(comparator.compare(fun3, fun3)));

		FunctionVariable fun4 = new FunctionVariable(Operator.XOR, 20);
		assertEquals("Error when comparing " + fun3.getName() + " and " + fun4.getName(), before, cast(comparator.compare(fun3, fun4)));

	}

	private int cast(int compareResult) {
		if (compareResult == 0) {
			return 0;
		} else if (compareResult > 0) {
			return 1;
		} else {
			return -1;
		}
	}
}
