package base.vhdl.structure;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

/**
 * @author Anton Chepurov
 */
public class OperatorTest {
	private static final String[][] MINUS_VALID_REGIONS_ARRAY = {
			{"-32767"},
			{"-1"},
			{"-AB1"},
			{"-110101"},
			{"-1892D"}
	};
	private static final String[][] NON_MINUS_VALID_REGIONS_ARRAY = {
			{"-qwe"},
			{"-1G"},
			{"AB1"},
			{"110101"},
			{"var-var2"},
			{"VAR - VAR22"},
			{"VAR -12"}
	};


	@Test
	public void correctIsMinusNumber() {
		for (String[] minusValidRegions : MINUS_VALID_REGIONS_ARRAY) {
			assertTrue("Regions " + Arrays.toString(minusValidRegions) + " are not recognized as minus number",
					Operator.isMinusNumber(minusValidRegions));
		}
		for (String[] nonMinusValidRegions : NON_MINUS_VALID_REGIONS_ARRAY) {
			assertFalse("Regions " + Arrays.toString(nonMinusValidRegions) + " are recognized as minus number",
					Operator.isMinusNumber(nonMinusValidRegions));
		}
	}

}
