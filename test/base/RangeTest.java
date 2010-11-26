package base;

import ee.ttu.pld.apricot.DetectionException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
 */
public class RangeTest {

	@Test
	public void fields() {
		Range range = new Range(10, 0);
		assertEquals(10, range.getHighest());
		assertEquals(0, range.getLowest());
		assertTrue(range.isDescending());
		range = new Range(7, 4);
		assertEquals(7, range.getHighest());
		assertEquals(4, range.getLowest());
		assertTrue(range.isDescending());
		range = new Range(9, 9);
		assertEquals(9, range.getHighest());
		assertEquals(9, range.getLowest());
		assertTrue(range.isDescending());

		range = new Range(9, 9);
		assertEquals(9, range.getHighest());
		assertEquals(9, range.getLowest());
		assertTrue(range.isDescending());
	}

	// todo: validate range (error for new Range(0, 10) )


	@Test
	public void deriveLength() {
		/* used with ranges */
		assertEquals(new Range(0, 0), Range.BIT_RANGE.deriveLength());
		assertEquals(new Range(1, 0), new Range(1, 0).deriveLength());
		assertEquals(new Range(7, 0), new Range(7, 0).deriveLength());
		assertEquals(new Range(0, 0), new Range(7, 7).deriveLength());
		assertEquals(new Range(2, 0), new Range(7, 5).deriveLength());
	}

	@Test
	public void deriveValueRange() {
		assertEquals(new Range(1, 0), Range.BIT_RANGE.deriveValueRange());
		assertEquals(new Range(3, 0), new Range(1, 0).deriveValueRange());
		assertEquals(new Range(255, 0), new Range(7, 0).deriveValueRange());
		assertEquals(new Range(1, 0), new Range(7, 7).deriveValueRange());
		assertEquals(new Range(7, 0), new Range(7, 5).deriveValueRange());
	}

	@Test
	public void deriveLengthForValues() {
		Range range = Range.deriveLengthForValues(127, -129);
		assertEquals(new Range(8, 0), range);
		range = Range.deriveLengthForValues(128, -128);
		assertEquals(new Range(8, 0), range);
		range = Range.deriveLengthForValues(127, -128);
		assertEquals(new Range(7, 0), range);
		range = Range.deriveLengthForValues(127, -1);
		assertEquals(new Range(7, 0), range);
		range = Range.deriveLengthForValues(127, 0);
		assertEquals(new Range(6, 0), range);
		range = Range.deriveLengthForValues(127, 126);
		assertEquals(new Range(6, 0), range);
		range = Range.deriveLengthForValues(7, 5);
		assertEquals(new Range(2, 0), range);
	}


	@Test
	public void length() {
		Range range = new Range(10, 0);
		assertEquals(11, range.length());
		range = new Range(7, 4);
		assertEquals(4, range.length());
		range = new Range(12, 11);
		assertEquals(2, range.length());
		range = new Range(19191911, 19191911);
		assertEquals(1, range.length());
		range = new Range(9, 9);
		assertEquals(1, range.length());
	}

	@Test
	public void highestSB() {
		Range range = new Range(10, 0);
		assertEquals(10, range.highestSB());
		range = new Range(7, 4);
		assertEquals(3, range.highestSB());
		range = new Range(9, 4);
		assertEquals(5, range.highestSB());
		range = new Range(9, 9);
		assertEquals(0, range.highestSB());
	}

	@Test
	public void testToString() {
		Range range = new Range(10, 0);
		assertEquals("(10 DOWNTO 0)", range.toString());
		range = new Range(7, 4);
		assertEquals("(7 DOWNTO 4)", range.toString());
		range = new Range(9, 9);
		assertEquals("(9)", range.toString());
	}

	@Test
	public void testToStringAngular() {
		Range range = new Range(10, 0);
		assertEquals("<10:0>", range.toStringAngular(false));
		assertEquals("<10:0>", range.toStringAngular(true));
		range = new Range(7, 4);
		assertEquals("<7:4>", range.toStringAngular(false));
		assertEquals("<7:4>", range.toStringAngular(true));
		range = new Range(9, 9);
		assertEquals("<9>", range.toStringAngular(true));
		assertEquals("<9:9>", range.toStringAngular(false));
	}

	@Test
	public void absoluteFor() {
		/* ############## 1 ##############
		* Both variableRange and valueRange are missing */
		// "DATA_HELD <= DATAIN;" DATA_HELD(24 DOWNTO 10) ===> DATAIN(24 DOWNTO 10) ----------  COPY "this" range
		assertEquals(new Range(24, 10), new Range(24, 10).absoluteFor(null, null));

		/* ############## 2 ##############
		* targetRange is missing */
		// "DATA_HELD <= DATAIN(18 DOWNTO 3);" DATA_HELD(7 DOWNTO 0) ===> DATAIN(10 DOWNTO 3) ----- DATA_HELD is actually (15 DOWNTO 0) -- DERIVE IT
		assertEquals(new Range(10, 3), new Range(7, 0).absoluteFor(null, new Range(18, 3)));
		// "DATA_HELD <= DATAIN(18 DOWNTO 3);" DATA_HELD(7 DOWNTO 5) ===> DATAIN(10 DOWNTO 8) ----- DATA_HELD is actually (15 DOWNTO 0) -- DERIVE IT
		assertEquals(new Range(10, 8), new Range(7, 5).absoluteFor(null, new Range(18, 3)));
		// "DATA_HELD <= DATAIN(18 DOWNTO 3);" DATA_HELD(15 DOWNTO 8) ===> DATAIN(18 DOWNTO 11) ----- DATA_HELD is actually (15 DOWNTO 0) -- DERIVE IT
		assertEquals(new Range(18, 11), new Range(15, 8).absoluteFor(null, new Range(18, 3)));
		assertEquals(new Range(17, 17), new Range(0, 0).absoluteFor(null, new Range(18, 17)));
		assertEquals(new Range(18, 17), new Range(1, 0).absoluteFor(null, new Range(18, 17)));
		assertEquals(new Range(18, 18), new Range(1, 1).absoluteFor(null, new Range(18, 17)));

		/* ############## 3 ##############
		* valueRange is missing */
		// "DATA_HELD(8 DOWNTO 0) <= DATAIN;" DATA_HELD(6 DOWNTO 4) ===> DATAIN(6 DOWNTO 4) ----- DATAIN is actually (8 DOWNTO 0) -- DERIVE IT
		assertEquals(new Range(6, 4), new Range(6, 4).absoluteFor(new Range(8, 0), null));
		// "DATA_HELD(8 DOWNTO 3) <= DATAIN;" DATA_HELD(6 DOWNTO 4) ===> DATAIN(3 DOWNTO 1) ----- DATAIN is actually (5 DOWNTO 0) -- DERIVE IT
		assertEquals(new Range(3, 1), new Range(6, 4).absoluteFor(new Range(8, 3), null));
		/* ############## 3 (*) ##############
        * valueRange is missing, but targetRange is equal to the (base) object */
		// "V_OUT(1) <= VOTO1;" V_OUT(1) ===> VOTO1 ----- VOTO1 is actually (0 DOWNTO 0) -- DERIVE IT
		assertNull(new Range(1, 1).absoluteFor(new Range(1, 1), null)); // no range!!!
		/* valueRange is present, but targetRange is equal to the (base) object */
		// "V_OUT(1) <= VOTO1(3);" V_OUT(1) ===> VOTO1(3) ----- VOTO1 is actually (3 DOWNTO 0) -- DERIVE IT
		assertEquals(new Range(3, 3), new Range(1, 1).absoluteFor(new Range(1, 1), new Range(3, 3)));

		/* ############## 4 ##############
		* Both variableRange and valueRange are present */
		// "DATA_HELD(8 DOWNTO 0) <= DATAIN(11 DOWNTO 3);" DATA_HELD(6 DOWNTO 4) ===> DATAIN(9 DOWNTO 7) ----- NOTHING TO DERIVE
		assertEquals(new Range(9, 7), new Range(6, 4).absoluteFor(new Range(8, 0), new Range(11, 3)));
		// "DATA_HELD(8 DOWNTO 3) <= DATAIN(11 DOWNTO 6);" DATA_HELD(6 DOWNTO 4) ===> DATAIN(9 DOWNTO 7) ----- NOTHING TO DERIVE
		assertEquals(new Range(9, 7), new Range(6, 4).absoluteFor(new Range(8, 3), new Range(11, 6)));
		// "DATA_HELD(33 DOWNTO 0) <= DATAIN(48 DOWNTO 15);" DATA_HELD(24 DOWNTO 10) ===> DATAIN(39 DOWNTO 25)
		assertEquals(new Range(39, 25), new Range(24, 10).absoluteFor(new Range(33, 0), new Range(48, 15)));
//        assertEquals(new Range(10, 3), new Range(7, 0).absoluteFor(new Range(18, 3), null));
//        assertEquals(new Range(10, 8), new Range(7, 5).absoluteFor(new Range(18, 3), null));
//        assertEquals(new Range(18, 11), new Range(15, 8).absoluteFor(new Range(18, 3), null));
//        assertEquals(new Range(17, 17), new Range(0, 0).absoluteFor(new Range(18, 17), null));
//        assertEquals(new Range(18, 17), new Range(1, 0).absoluteFor(new Range(18, 17), null));
//        assertEquals(new Range(18, 18), new Range(1, 1).absoluteFor(new Range(18, 17), null));

		/* The following situation:
		* "DATA_HELD(33 DOWNTO 5) <= DATAIN;" DATA_HELD(24 DOWNTO 0) ===> ...
		* is not possible due to splitOverlaps() method of SplittableOperandStorage.
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
		assertEquals(new Range(6, 4), new Range(8, 6).absoluteFor(new Range(10, 3), new Range(8, 1)));


		/* Examples from CRC.vhd */
		// crc_inp_1 <= ips_wdata;
		assertEquals(new Range(7, 0), new Range(7, 0).absoluteFor(null, null));
		// crc_inp_1(7 downto 0) <= ips_wdata(7 downto 0);
		assertEquals(new Range(7, 0), new Range(7, 0).absoluteFor(new Range(7, 0), new Range(7, 0)));
		// crc_inp_1(15 downto 8) <= ips_wdata(15 downto 8);
		assertEquals(new Range(15, 8), new Range(15, 8).absoluteFor(new Range(15, 8), new Range(15, 8)));
		// crc_inp_1(23 downto 16) <= ips_wdata(23 downto 16);
		assertEquals(new Range(23, 16), new Range(23, 16).absoluteFor(new Range(23, 16), new Range(23, 16)));
		// crc_inp_1(31 downto 24) <= ips_wdata(31 downto 24);
		assertEquals(new Range(31, 24), new Range(31, 24).absoluteFor(new Range(31, 24), new Range(31, 24)));

		// crc_inp_1(15 downto 0) <= ips_wdata(15 downto 0); crc_inp_1(15 downto 8) ===> ips_wdata(15 downto 8)
		// crc_inp_1(15 downto 0) <= ips_wdata(15 downto 0); crc_inp_1(7 downto 0) ===> ips_wdata(7 downto 0)
		assertEquals(new Range(15, 8), new Range(15, 8).absoluteFor(new Range(15, 0), new Range(15, 0)));
		assertEquals(new Range(7, 0), new Range(7, 0).absoluteFor(new Range(15, 0), new Range(15, 0)));

		// crc_inp_1(31 downto 16) <= ips_wdata(31 downto 16); crc_inp_1(31 downto 24) ===> ips_wdata(31 downto 24)
		// crc_inp_1(31 downto 16) <= ips_wdata(31 downto 16); crc_inp_1(23 downto 16) ===> ips_wdata(23 downto 16)
		assertEquals(new Range(31, 24), new Range(31, 24).absoluteFor(new Range(31, 16), new Range(31, 16)));
		assertEquals(new Range(23, 16), new Range(23, 16).absoluteFor(new Range(31, 16), new Range(31, 16)));

		// crc_inp_1(31 downto 0) <= ips_wdata(31 downto 0);
		assertEquals(new Range(31, 24), new Range(31, 24).absoluteFor(new Range(31, 0), new Range(31, 0)));
		assertEquals(new Range(23, 16), new Range(23, 16).absoluteFor(new Range(31, 0), new Range(31, 0)));
		assertEquals(new Range(15, 8), new Range(15, 8).absoluteFor(new Range(31, 0), new Range(31, 0)));
		assertEquals(new Range(7, 0), new Range(7, 0).absoluteFor(new Range(31, 0), new Range(31, 0)));

	}

	@Test
	public void testEquals() {
		/* NULL */
		//noinspection ObjectEqualsNull
		assertFalse(new Range(0, 0).equals(null));
		/* Different ranges */
		assertFalse(new Range(2, 1).equals(new Range(3, 1)));
		/* Same ranges */
		assertTrue(new Range(19, 10).equals(new Range(19, 10)));
		assertTrue(new Range(0, 0).equals(new Range(0, 0)));
		assertTrue(new Range(1991, 1991).equals(new Range(1991, 1991)));

	}

	@Test(expected = DetectionException.class)
	public void intersectionDetectedWhenCompare() throws DetectionException {
		/* Intersection */
		boolean first = false, second = false, third = false;
		Range range = new Range(9, 2);
		try {
			range.compareTo(new Range(13, 5));
		} catch (RuntimeException e) {
			if (e.getMessage().startsWith(Range.INTERSECTION_TEXT)) {
				first = true;
			}
		}
		try {
			range.compareTo(new Range(3, 0));
		} catch (RuntimeException e) {
			if (e.getMessage().startsWith(Range.INTERSECTION_TEXT)) {
				second = true;
			}
		}
		try {
			range.compareTo(new Range(13, 9));
		} catch (RuntimeException e) {
			if (e.getMessage().startsWith(Range.INTERSECTION_TEXT)) {
				third = true;
			}
		}
		try {
			range.compareTo(new Range(2, 1));
		} catch (RuntimeException e) {
			if (e.getMessage().startsWith(Range.INTERSECTION_TEXT)) {
				if (first && second && third) {
					throw new DetectionException();
				}
			}
		}
	}

	@Test
	public void correctCompare() {
		Range range = new Range(9, 2);
		assertTrue(range.compareTo(new Range(10, 10)) < 0);
		assertTrue(range.compareTo(new Range(120, 10)) < 0);
		assertTrue(range.compareTo(new Range(1, 1)) > 0);
		assertTrue(range.compareTo(new Range(1, 0)) > 0);
		assertTrue(range.compareTo(range) == 0);
		assertTrue(range.compareTo(new Range(9, 2)) == 0);
	}
}
