package base.vhdl.structure;

import org.junit.Test;
import static org.junit.Assert.assertTrue;import static org.junit.Assert.assertFalse;

import java.util.Arrays;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 10.11.2008
 * <br>Time: 11:48:49
 */
public class OperatorTest {
    private static final String[][] minusValidRegionsArray = {
            {"-32767"},
            {"-1"},
            {"-AB1"},
            {"-110101"},
            {"-1892D"}
    };
    private static final String[][] nonMinusValidRegionsArray = {
            {"-qwe"},
            {"-1G"},
            {"AB1"},
            {"110101"},
            {"var-var2"},
            {"VAR - VAR22"},
            {"VAR -12"}
    };


    @Test public void correctIsMinusNumber() {
        for (String[] minusValidRegions : minusValidRegionsArray) {
            assertTrue("Regions " + Arrays.toString(minusValidRegions) + " are not reckognized as minus number", 
                    Operator.isMinusNumber(minusValidRegions));
        }
        for (String[] nonMinusValidRegions : nonMinusValidRegionsArray) {
            assertFalse("Regions " + Arrays.toString(nonMinusValidRegions) + " are reckognized as minus number",
                    Operator.isMinusNumber(nonMinusValidRegions));
        }
    }

}
