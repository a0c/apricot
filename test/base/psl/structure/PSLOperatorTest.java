package base.psl.structure;

import org.junit.Test;
import static org.junit.Assert.*;
import helpers.PSLProperties;
import parsers.ExpressionBuilder;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 27.10.2008
 * <br>Time: 10:21:05
 */
public class PSLOperatorTest {
    @Test
    public void correctOperandsExtracted() throws Exception {
        PPGLibrary library = PPGLibraryTest.createLibrary();
        for (int i = 0; i < PSLProperties.exampleOperatorArray.length; i++) {
            String propertyLine = ExpressionBuilder.trimEnclosingBrackets(PSLProperties.exampleOperatorArray[i][0]);
            String[] correctOperands = PSLProperties.exampleOperandArray[i];
            PSLOperator pslOperator = library.pslOperators[i];
            if (!PSLProperties.exampleOperatorArray[i][1].equals(pslOperator.getName())) {
                assertTrue("Different Operators #" + (i + 1) + " in " + PSLProperties.class.getSimpleName()
                        + " and " + PPGLibrary.class.getSimpleName(), false);
            }
            String[] extractedOperands = pslOperator.extractOperandLinesFrom(propertyLine);

            /* Check OPERANDS */
//            System.out.println("Correct: \n" + java.util.Arrays.toString(correctOperands));
//            System.out.println("Extracted: \n" + java.util.Arrays.toString(extractedOperands));
            assertArrayEquals("Wrong operands extracted for operator " + pslOperator.getName(), correctOperands, extractedOperands);

        }
    }
}
