package base;

import ee.ttu.pld.apricot.DetectionException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
 */
public class IndicesTest {

	@Test
	public void fields() {
		Indices indices = new Indices(10, 0);
		assertEquals(10, indices.getHighest());
		assertEquals(0, indices.getLowest());
		assertTrue(indices.isDescending());
		indices = new Indices(7, 4);
		assertEquals(7, indices.getHighest());
		assertEquals(4, indices.getLowest());
		assertTrue(indices.isDescending());
		indices = new Indices(9, 9);
		assertEquals(9, indices.getHighest());
		assertEquals(9, indices.getLowest());
		assertTrue(indices.isDescending());

		indices = new Indices(9, 9);
		assertEquals(9, indices.getHighest());
		assertEquals(9, indices.getLowest());
		assertTrue(indices.isDescending());
	}

	// todo: validate indices (error for new Indices(0, 10) )


	@Test
	public void deriveLength() {
		/* used with Parted Indices */
		assertEquals(new Indices(0, 0), Indices.BIT_INDICES.deriveLength());
		assertEquals(new Indices(1, 0), new Indices(1, 0).deriveLength());
		assertEquals(new Indices(7, 0), new Indices(7, 0).deriveLength());
		assertEquals(new Indices(0, 0), new Indices(7, 7).deriveLength());
		assertEquals(new Indices(2, 0), new Indices(7, 5).deriveLength());
	}

	@Test
	public void deriveValueRange() {
		assertEquals(new Indices(1, 0), Indices.BIT_INDICES.deriveValueRange());
		assertEquals(new Indices(3, 0), new Indices(1, 0).deriveValueRange());
		assertEquals(new Indices(255, 0), new Indices(7, 0).deriveValueRange());
		assertEquals(new Indices(1, 0), new Indices(7, 7).deriveValueRange());
		assertEquals(new Indices(7, 0), new Indices(7, 5).deriveValueRange());
	}

	@Test
	public void deriveLengthForValues() {
		Indices indices = Indices.deriveLengthForValues(127, -129);
		assertEquals(new Indices(8, 0), indices);
		indices = Indices.deriveLengthForValues(128, -128);
		assertEquals(new Indices(8, 0), indices);
		indices = Indices.deriveLengthForValues(127, -128);
		assertEquals(new Indices(7, 0), indices);
		indices = Indices.deriveLengthForValues(127, -1);
		assertEquals(new Indices(7, 0), indices);
		indices = Indices.deriveLengthForValues(127, 0);
		assertEquals(new Indices(6, 0), indices);
		indices = Indices.deriveLengthForValues(127, 126);
		assertEquals(new Indices(6, 0), indices);
		indices = Indices.deriveLengthForValues(7, 5);
		assertEquals(new Indices(2, 0), indices);
	}


	@Test
	public void length() {
		Indices indices = new Indices(10, 0);
		assertEquals(11, indices.length());
		indices = new Indices(7, 4);
		assertEquals(4, indices.length());
		indices = new Indices(12, 11);
		assertEquals(2, indices.length());
		indices = new Indices(19191911, 19191911);
		assertEquals(1, indices.length());
		indices = new Indices(9, 9);
		assertEquals(1, indices.length());
	}

	@Test
	public void highestSB() {
		Indices indices = new Indices(10, 0);
		assertEquals(10, indices.highestSB());
		indices = new Indices(7, 4);
		assertEquals(3, indices.highestSB());
		indices = new Indices(9, 4);
		assertEquals(5, indices.highestSB());
		indices = new Indices(9, 9);
		assertEquals(0, indices.highestSB());
	}

	@Test
	public void testToString() {
		Indices indices = new Indices(10, 0);
		assertEquals("(10 DOWNTO 0)", indices.toString());
		indices = new Indices(7, 4);
		assertEquals("(7 DOWNTO 4)", indices.toString());
		indices = new Indices(9, 9);
		assertEquals("(9)", indices.toString());
	}

	@Test
	public void testToStringAngular() {
		Indices indices = new Indices(10, 0);
		assertEquals("<10:0>", indices.toStringAngular(false));
		assertEquals("<10:0>", indices.toStringAngular(true));
		indices = new Indices(7, 4);
		assertEquals("<7:4>", indices.toStringAngular(false));
		assertEquals("<7:4>", indices.toStringAngular(true));
		indices = new Indices(9, 9);
		assertEquals("<9>", indices.toStringAngular(true));
		assertEquals("<9:9>", indices.toStringAngular(false));
	}

	@Test
	public void absoluteFor() {
		/* ############## 1 ##############
		* Both variableIndices and valueIndices are missing */
		// "DATA_HELD <= DATAIN;" DATA_HELD(24 DOWNTO 10) ===> DATAIN(24 DOWNTO 10) ----------  COPY "this" indices
		assertEquals(new Indices(24, 10), new Indices(24, 10).absoluteFor(null, null));

		/* ############## 2 ##############
		* targetIndices are missing */
		// "DATA_HELD <= DATAIN(18 DOWNTO 3);" DATA_HELD(7 DOWNTO 0) ===> DATAIN(10 DOWNTO 3) ----- DATA_HELD is actually (15 DOWNTO 0) -- DERIVE IT
		assertEquals(new Indices(10, 3), new Indices(7, 0).absoluteFor(null, new Indices(18, 3)));
		// "DATA_HELD <= DATAIN(18 DOWNTO 3);" DATA_HELD(7 DOWNTO 5) ===> DATAIN(10 DOWNTO 8) ----- DATA_HELD is actually (15 DOWNTO 0) -- DERIVE IT
		assertEquals(new Indices(10, 8), new Indices(7, 5).absoluteFor(null, new Indices(18, 3)));
		// "DATA_HELD <= DATAIN(18 DOWNTO 3);" DATA_HELD(15 DOWNTO 8) ===> DATAIN(18 DOWNTO 11) ----- DATA_HELD is actually (15 DOWNTO 0) -- DERIVE IT
		assertEquals(new Indices(18, 11), new Indices(15, 8).absoluteFor(null, new Indices(18, 3)));
		assertEquals(new Indices(17, 17), new Indices(0, 0).absoluteFor(null, new Indices(18, 17)));
		assertEquals(new Indices(18, 17), new Indices(1, 0).absoluteFor(null, new Indices(18, 17)));
		assertEquals(new Indices(18, 18), new Indices(1, 1).absoluteFor(null, new Indices(18, 17)));

		/* ############## 3 ##############
		* valueIndices are missing */
		// "DATA_HELD(8 DOWNTO 0) <= DATAIN;" DATA_HELD(6 DOWNTO 4) ===> DATAIN(6 DOWNTO 4) ----- DATAIN is actually (8 DOWNTO 0) -- DERIVE IT
		assertEquals(new Indices(6, 4), new Indices(6, 4).absoluteFor(new Indices(8, 0), null));
		// "DATA_HELD(8 DOWNTO 3) <= DATAIN;" DATA_HELD(6 DOWNTO 4) ===> DATAIN(3 DOWNTO 1) ----- DATAIN is actually (5 DOWNTO 0) -- DERIVE IT
		assertEquals(new Indices(3, 1), new Indices(6, 4).absoluteFor(new Indices(8, 3), null));
		/* ############## 3 (*) ##############
        * valueIndices are missing, but targetIndices are equal to the (base) object */
		// "V_OUT(1) <= VOTO1;" V_OUT(1) ===> VOTO1 ----- VOTO1 is actually (0 DOWNTO 0) -- DERIVE IT
		assertNull(new Indices(1, 1).absoluteFor(new Indices(1, 1), null)); // no indices!!!
		/* valueIndices are present, but targetIndices are equal to the (base) object */
		// "V_OUT(1) <= VOTO1(3);" V_OUT(1) ===> VOTO1(3) ----- VOTO1 is actually (3 DOWNTO 0) -- DERIVE IT
		assertEquals(new Indices(3, 3), new Indices(1, 1).absoluteFor(new Indices(1, 1), new Indices(3, 3)));

		/* ############## 4 ##############
		* Both variableIndices and valueIndices are present */
		// "DATA_HELD(8 DOWNTO 0) <= DATAIN(11 DOWNTO 3);" DATA_HELD(6 DOWNTO 4) ===> DATAIN(9 DOWNTO 7) ----- NOTHING TO DERIVE
		assertEquals(new Indices(9, 7), new Indices(6, 4).absoluteFor(new Indices(8, 0), new Indices(11, 3)));
		// "DATA_HELD(8 DOWNTO 3) <= DATAIN(11 DOWNTO 6);" DATA_HELD(6 DOWNTO 4) ===> DATAIN(9 DOWNTO 7) ----- NOTHING TO DERIVE
		assertEquals(new Indices(9, 7), new Indices(6, 4).absoluteFor(new Indices(8, 3), new Indices(11, 6)));
		// "DATA_HELD(33 DOWNTO 0) <= DATAIN(48 DOWNTO 15);" DATA_HELD(24 DOWNTO 10) ===> DATAIN(39 DOWNTO 25)
		assertEquals(new Indices(39, 25), new Indices(24, 10).absoluteFor(new Indices(33, 0), new Indices(48, 15)));
//        assertEquals(new Indices(10, 3), new Indices(7, 0).absoluteFor(new Indices(18, 3), null));
//        assertEquals(new Indices(10, 8), new Indices(7, 5).absoluteFor(new Indices(18, 3), null));
//        assertEquals(new Indices(18, 11), new Indices(15, 8).absoluteFor(new Indices(18, 3), null));
//        assertEquals(new Indices(17, 17), new Indices(0, 0).absoluteFor(new Indices(18, 17), null));
//        assertEquals(new Indices(18, 17), new Indices(1, 0).absoluteFor(new Indices(18, 17), null));
//        assertEquals(new Indices(18, 18), new Indices(1, 1).absoluteFor(new Indices(18, 17), null));

		/* The following situation:
		* "DATA_HELD(33 DOWNTO 5) <= DATAIN;" DATA_HELD(24 DOWNTO 0) ===> ...
		* is not possible due to doDisintersectPartialSettings() method of PartialSetVariableCollector.
		* This method would split the above mentioned operand into 2:
		* DATA_HELD(24 DOWNTO 5) and DATA_HELD(4 DOWNTO 0),
		* and the latter wouldn't match the check isGraphVariableSetIn() in GraphGenerator. */

		/* Examples created from ECC1_mod_hldd.vhd:
		* 1) DATA_HELD has length: Y to X
		* DATA_HELD(8 DOWNTO 6) from:
		* DATA_HELD(10 DOWNTO 3) <= DATAIN(8 DOWNTO 1);
		* produces:
		* DATAIN( 6 DOWNTO 4 )
		*
		* */
		assertEquals(new Indices(6, 4), new Indices(8, 6).absoluteFor(new Indices(10, 3), new Indices(8, 1)));


		/* Examples from CRC.vhd */
		// crc_inp_1 <= ips_wdata;
		assertEquals(new Indices(7, 0), new Indices(7, 0).absoluteFor(null, null));
		// crc_inp_1(7 downto 0) <= ips_wdata(7 downto 0);
		assertEquals(new Indices(7, 0), new Indices(7, 0).absoluteFor(new Indices(7, 0), new Indices(7, 0)));
		// crc_inp_1(15 downto 8) <= ips_wdata(15 downto 8);
		assertEquals(new Indices(15, 8), new Indices(15, 8).absoluteFor(new Indices(15, 8), new Indices(15, 8)));
		// crc_inp_1(23 downto 16) <= ips_wdata(23 downto 16);
		assertEquals(new Indices(23, 16), new Indices(23, 16).absoluteFor(new Indices(23, 16), new Indices(23, 16)));
		// crc_inp_1(31 downto 24) <= ips_wdata(31 downto 24);
		assertEquals(new Indices(31, 24), new Indices(31, 24).absoluteFor(new Indices(31, 24), new Indices(31, 24)));

		// crc_inp_1(15 downto 0) <= ips_wdata(15 downto 0); crc_inp_1(15 downto 8) ===> ips_wdata(15 downto 8)
		// crc_inp_1(15 downto 0) <= ips_wdata(15 downto 0); crc_inp_1(7 downto 0) ===> ips_wdata(7 downto 0)
		assertEquals(new Indices(15, 8), new Indices(15, 8).absoluteFor(new Indices(15, 0), new Indices(15, 0)));
		assertEquals(new Indices(7, 0), new Indices(7, 0).absoluteFor(new Indices(15, 0), new Indices(15, 0)));

		// crc_inp_1(31 downto 16) <= ips_wdata(31 downto 16); crc_inp_1(31 downto 24) ===> ips_wdata(31 downto 24)
		// crc_inp_1(31 downto 16) <= ips_wdata(31 downto 16); crc_inp_1(23 downto 16) ===> ips_wdata(23 downto 16)
		assertEquals(new Indices(31, 24), new Indices(31, 24).absoluteFor(new Indices(31, 16), new Indices(31, 16)));
		assertEquals(new Indices(23, 16), new Indices(23, 16).absoluteFor(new Indices(31, 16), new Indices(31, 16)));

		// crc_inp_1(31 downto 0) <= ips_wdata(31 downto 0);
		assertEquals(new Indices(31, 24), new Indices(31, 24).absoluteFor(new Indices(31, 0), new Indices(31, 0)));
		assertEquals(new Indices(23, 16), new Indices(23, 16).absoluteFor(new Indices(31, 0), new Indices(31, 0)));
		assertEquals(new Indices(15, 8), new Indices(15, 8).absoluteFor(new Indices(31, 0), new Indices(31, 0)));
		assertEquals(new Indices(7, 0), new Indices(7, 0).absoluteFor(new Indices(31, 0), new Indices(31, 0)));

	}

	@Test
	public void testEquals() {
		/* NULL */
		//noinspection ObjectEqualsNull
		assertFalse(new Indices(0, 0).equals(null));
		/* Different indices */
		assertFalse(new Indices(2, 1).equals(new Indices(3, 1)));
		/* Same indices */
		assertTrue(new Indices(19, 10).equals(new Indices(19, 10)));
		assertTrue(new Indices(0, 0).equals(new Indices(0, 0)));
		assertTrue(new Indices(1991, 1991).equals(new Indices(1991, 1991)));

	}

	@Test(expected = DetectionException.class)
	public void intersectionDetectedWhenCompare() throws DetectionException {
		/* Intersection */
		boolean first = false, second = false, third = false;
		Indices indices = new Indices(9, 2);
		try {
			indices.compareTo(new Indices(13, 5));
		} catch (RuntimeException e) {
			if (e.getMessage().startsWith(Indices.INTERSECTION_TEXT)) {
				first = true;
			}
		}
		try {
			indices.compareTo(new Indices(3, 0));
		} catch (RuntimeException e) {
			if (e.getMessage().startsWith(Indices.INTERSECTION_TEXT)) {
				second = true;
			}
		}
		try {
			indices.compareTo(new Indices(13, 9));
		} catch (RuntimeException e) {
			if (e.getMessage().startsWith(Indices.INTERSECTION_TEXT)) {
				third = true;
			}
		}
		try {
			indices.compareTo(new Indices(2, 1));
		} catch (RuntimeException e) {
			if (e.getMessage().startsWith(Indices.INTERSECTION_TEXT)) {
				if (first && second && third) {
					throw new DetectionException();
				}
			}
		}
	}

	@Test
	public void correctCompare() {
		Indices indices = new Indices(9, 2);
		assertTrue(indices.compareTo(new Indices(10, 10)) < 0);
		assertTrue(indices.compareTo(new Indices(120, 10)) < 0);
		assertTrue(indices.compareTo(new Indices(1, 1)) > 0);
		assertTrue(indices.compareTo(new Indices(1, 0)) > 0);
		assertTrue(indices.compareTo(indices) == 0);
		assertTrue(indices.compareTo(new Indices(9, 2)) == 0);
	}
}
