package base;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
 */
public class HierarchyLocationTest {

	@Test
	public void testToString() {

		HierarchyLocation hierarchyLocation = new HierarchyLocation();
		assertEquals("", hierarchyLocation.toString());

		hierarchyLocation.addLocation("LOC1");
		assertEquals("#LOC1#", hierarchyLocation.toString());

		hierarchyLocation.addLocation("LOC2");
		assertEquals("#LOC2#LOC1#", hierarchyLocation.toString());
	}
	@Test
	public void testIsTopLevel() {

		HierarchyLocation hierarchyLocation = new HierarchyLocation();
		assertTrue(hierarchyLocation.isTopLevel());

		hierarchyLocation.addLocation("LOC1");
		assertFalse(hierarchyLocation.isTopLevel());

		hierarchyLocation.addLocation("LOC2");
		assertFalse(hierarchyLocation.isTopLevel());
	}

}
