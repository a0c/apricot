package base.hldd.structure.nodes.utils;

import base.hldd.structure.nodes.Node;
import org.junit.Test;

import static junit.framework.Assert.*;

import java.util.TreeMap;

/**
 * @author Anton Chepurov
 */
public class ConditionTest {
	@Test
	public void testParse() throws Exception {

		assertEquals(Condition.createCondition(), Condition.parse(""));

		assertEquals(Condition.createCondition(1), Condition.parse("1"));
		assertEquals(Condition.createCondition(0), Condition.parse("0"));
		assertEquals(Condition.createCondition(2010), Condition.parse("2010"));

		assertEquals(Condition.createCondition(1, 2, 8, 1991), Condition.parse("1,2,8,1991"));

		assertEquals(Condition.createCondition(1, 2), Condition.parse("1-2"));
		assertEquals(Condition.createCondition(1, 2, 3), Condition.parse("1-3"));
		assertEquals(Condition.createCondition(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), Condition.parse("0-10"));

		assertEquals(Condition.createCondition(0, 1, 2, 3, 4, 7, 8, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100), Condition.parse("0-4, 7-8, 90-100"));

		assertEquals(Condition.createCondition(0, 1, 3, 4, 5, 6), Condition.parse("0-1,3-6"));
		assertEquals(Condition.createCondition(1, 2, 5), Condition.parse("1-2,5"));
		assertEquals(Condition.createCondition(0, 3), Condition.parse("0 , 3"));

	}

	@Test
	public void testEquals() throws Exception {
		Condition condition0and1 = Condition.createCondition(0, 1);
		Condition condition0and2 = Condition.createCondition(0, 2);
		assertTrue(!condition0and1.equals(condition0and2));
		assertTrue(condition0and1.equals(condition0and1));
		assertTrue(condition0and2.equals(condition0and2));

		Condition condition0 = Condition.createCondition(0);
		assertTrue(!condition0.equals(condition0and1));
	}

	@Test
	public void testHashCode() throws Exception {
		Condition condition0 = Condition.createCondition(0);
		Condition condition8 = Condition.createCondition(8);
		Condition condition0and8 = Condition.createCondition(0, 8);
		assertTrue(condition0.hashCode() != condition0and8.hashCode());
		assertTrue(condition0.hashCode() != condition8.hashCode());
	}

	@Test
	public void testCompareTo() throws Exception {
		Condition condition0 = Condition.createCondition(0);
		Condition condition8 = Condition.createCondition(8);
		Condition condition0and8 = Condition.createCondition(0, 8);

		TreeMap<Condition, Node> map = new TreeMap<Condition, Node>();
		map.put(condition0and8, null);
		assertTrue(map.containsKey(condition0and8));
		assertTrue(!map.containsKey(condition8));
		assertTrue(!map.containsKey(condition0));

		assertTrue(condition0.compareTo(condition0) == 0);
		assertTrue(condition0.compareTo(condition8) == -1);
		assertTrue(condition8.compareTo(condition0) == 1);

		assertTrue(condition0.compareTo(condition0and8) == -1);
		assertTrue(condition0and8.compareTo(condition0) == 1);
		assertTrue(condition8.compareTo(condition0and8) == 1);
		assertTrue(condition0and8.compareTo(condition8) == -1);

		Condition condition0and8and199 = Condition.createCondition(0, 8, 199);
		Condition condition0and8and199and2010 = Condition.createCondition(0, 8, 199, 2010);
		assertTrue(condition0and8.compareTo(condition0and8and199) == -1);
		assertTrue(condition0and8and199.compareTo(condition0and8) == 1);
		assertTrue(condition0and8.compareTo(condition0and8and199and2010) == -1);
		assertTrue(condition0and8and199and2010.compareTo(condition0and8) == 1);
	}
}
