package base;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
 */
public class TypeTest {

	@Test
	public void testEquals() {
		Type secondType;
		Type firstType;
		assertEquals("Following types are not equal: " + Type.BIT_TYPE + " and " + Type.BIT_TYPE, Type.BIT_TYPE, Type.BIT_TYPE);
		firstType = new Type(new Range(0, 0));
		assertEquals("Following types are not equal: " + firstType + " and " + Type.BIT_TYPE, firstType, Type.BIT_TYPE);
		firstType = new Type(new Range(8, 0));
		secondType = new Type(new Range(8, 0));
		assertEquals("Following types are not equal: " + firstType + " and " + secondType, firstType, secondType);
		firstType = Type.createFromValues(8, 0);
		secondType = new Type(new Range(3, 0));
		assertFalse("Following types are equal: " + firstType + " and " + secondType, firstType.equals(secondType));
		firstType = Type.createFromValues(8, 0);
		secondType = new Type(new Range(8, 0), new Range(3, 0));
		assertTrue("Following types are equal: " + firstType + " and " + secondType, firstType.equals(secondType));
		firstType = Type.createFromValues(8, 0);
		secondType = Type.createFromValues(8, 1);
		assertFalse("Following types are equal: " + firstType + " and " + secondType, firstType.equals(secondType));

	}

	@Test
	public void testToString() {
		Type type1 = new Type(new Range(0, 0));
		assertEquals("Incorrect type printing", "TYPE <0:0>", type1.toString());
		type1 = new Type(new Range(2, 0));
		assertEquals("Incorrect type printing", "TYPE <2:0>", type1.toString());
		type1 = new Type(new Range(10, 4));
		assertEquals("Incorrect type printing", "TYPE <10:4>", type1.toString());
		type1 = Type.createFromValues(1, 0);
		assertEquals("Incorrect type printing", "TYPE <0:0> (ENUM=(1 DOWNTO 0));", type1.toString());
		type1 = Type.createFromValues(8, 0);
		assertEquals("Incorrect type printing", "TYPE <3:0> (ENUM=(8 DOWNTO 0));", type1.toString());
		type1 = Type.createFromValues(7, 5);
		assertEquals("Incorrect type printing", "TYPE <2:0> (ENUM=(7 DOWNTO 5));", type1.toString());
		type1 = Type.createFromValues(7, 7);
		assertEquals("Incorrect type printing", "TYPE <2:0> (ENUM=(7));", type1.toString());
	}

	@Test
	public void deriveRangeType() {
		Type type = new Type(new Range(7, 0));
		assertEquals(new Type(new Range(2, 0)), type.deriveRangeType(new Range(7, 5)));
		assertEquals(new Type(new Range(0, 0)), type.deriveRangeType(new Range(7, 7)));
		assertEquals(new Type(new Range(0, 0)), type.deriveRangeType(new Range(0, 0)));
		assertEquals(new Type(new Range(2, 0)), type.deriveRangeType(new Range(2, 0)));
		assertEquals(new Type(new Range(1, 0)), type.deriveRangeType(new Range(4, 3)));
		assertEquals(type, type.deriveRangeType(null));
		assertEquals(new Type(new Range(7, 0)), type.deriveRangeType(null));
		type = Type.createFromValues(255, 0);
		assertEquals(new Type(new Range(7, 0), new Range(2, 0)), type.deriveRangeType(new Range(7, 5)));
		assertEquals(new Type(new Range(1, 0), new Range(0, 0)), type.deriveRangeType(new Range(7, 7)));
		assertEquals(new Type(new Range(1, 0), new Range(0, 0)), type.deriveRangeType(new Range(0, 0)));
		assertEquals(new Type(new Range(7, 0), new Range(2, 0)), type.deriveRangeType(new Range(2, 0)));
		assertEquals(new Type(new Range(3, 0), new Range(1, 0)), type.deriveRangeType(new Range(4, 3)));
		assertEquals(type, type.deriveRangeType(null));
		assertEquals(Type.createFromValues(255, 0), type.deriveRangeType(null));
	}
}
