package base.psl.structure;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
 */
public class RangeTest {
	private static final String[] CORRECT_RANGES = {
			"something [12 to 90]",
			"someth [1 TO 0]",
			"smth [n]",
			"a[start to end]",
			"j[j To k]",
			"bla[1 tO end]"
	};
	private static final String[] RANGE_DECLARATIONS = {
			"next[n] TOP;",
			"next_e[start to end] TOP;",
			"next_a[start to end] TOP;",
			"\t\t0\t0:\t(n___) (0=>1 1=>2 2=>3)\tV = 0 \"TOP\"\t<1:0> [0 to END]",	  // ALWAYS + NEVER
			"\t\t0\t0:\t(n___) (0=>1 1=>2 2=>3)\tV = 0 \"TOP\"\t<1:0> [1   to   1]",	// NEXT
			"\t\t0\t0:\t(n___) (0=>1 1=>2 2=>3)\tV = 0 \"TOP\"\t<1:0> [n   to   n]",	// NEXT[n]
			"\t\t0\t0:\t(n___) (0=>1 1=>2 2=>3)\tV = 0 \"TOP\"\t<1:0> [start to end]"   // NEXT_E[start to end] + NEXT_A[start to end]
	};
	private static final String[][] PARSED_RANGE_DECLARATIONS = {
			{"n"},
			{"start", "end"},
			{"start", "end"},
			{"0", "END"},
			{"1", "1"},
			{"n", "n"},
			{"start", "end"}
	};

	@Test
	public void rangeRecognized() {
		for (String correctRange : CORRECT_RANGES) {
			assertTrue(Range.isRangeDeclaration(correctRange));
		}
	}

	@Test
	public void rangeDeclarationParsed() {
		for (int i = 0; i < RANGE_DECLARATIONS.length; i++) {
			assertArrayEquals(PARSED_RANGE_DECLARATIONS[i], Range.parseRangeDeclaration(RANGE_DECLARATIONS[i]));
		}
	}
}
