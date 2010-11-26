package base.hldd.structure.variables;

import base.HLDDException;
import base.Range;
import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.Assert.*;

/**
 * @author Anton Chepurov
 */
public class ConstantVariableTest {
	@Test(expected = HLDDException.class)
	public void testSubRange() throws HLDDException {

		ConstantVariable const0100 = ConstantVariable.createNamedConstant(new BigInteger("4"), null, new Range(3, 0));
		ConstantVariable const0 = ConstantVariable.createNamedConstant(new BigInteger("0"), null, new Range(0, 0));
		ConstantVariable const1 = ConstantVariable.createNamedConstant(new BigInteger("1"), null, new Range(0, 0));
		ConstantVariable const10 = ConstantVariable.createNamedConstant(new BigInteger("2"), null, new Range(1, 0));
		ConstantVariable const100 = ConstantVariable.createNamedConstant(new BigInteger("4"), null, new Range(2, 0));
		ConstantVariable const01 = ConstantVariable.createNamedConstant(new BigInteger("1"), null, new Range(1, 0));
		ConstantVariable const010 = ConstantVariable.createNamedConstant(new BigInteger("2"), null, new Range(2, 0));

		assertEquals(const0.toString(), const0100.subRange(new Range(0, 0)).toString());
		assertEquals(const0.toString(), const0100.subRange(new Range(1, 1)).toString());
		assertEquals(const0.toString(), const0100.subRange(new Range(3, 3)).toString());
		assertEquals(const1.toString(), const0100.subRange(new Range(2, 2)).toString());
		assertEquals(const10.toString(), const0100.subRange(new Range(2, 1)).toString());
		assertEquals(const100.toString(), const0100.subRange(new Range(2, 0)).toString());
		assertEquals(const01.toString(), const0100.subRange(new Range(3, 2)).toString());
		assertEquals(const010.toString(), const0100.subRange(new Range(3, 1)).toString());
		assertEquals(const0100.toString(), const0100.subRange(new Range(3, 0)).toString());
		System.out.println(const0.toString());
		System.out.println(const1.toString());
		System.out.println(const10.toString());
		System.out.println(const100.toString());
		System.out.println(const01.toString());
		System.out.println(const010.toString());
		System.out.println(const0100.toString());

		const0100.subRange(new Range(4, 3));
	}
}
