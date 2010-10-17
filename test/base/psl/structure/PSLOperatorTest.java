package base.psl.structure;

import org.junit.Test;

import static org.junit.Assert.*;

import helpers.PSLProperties;
import parsers.ExpressionBuilder;

/**
 * @author Anton Chepurov
 */
public class PSLOperatorTest {
	@Test
	public void correctOperandsExtracted() throws Exception {
		PPGLibrary library = PPGLibraryTest.createLibrary();
		for (int i = 0; i < PSLProperties.EXAMPLE_OPERATOR_ARRAY.length; i++) {
			String propertyLine = ExpressionBuilder.trimEnclosingBrackets(PSLProperties.EXAMPLE_OPERATOR_ARRAY[i][0]);
			String[] correctOperands = PSLProperties.EXAMPLE_OPERAND_ARRAY[i];
			PSLOperator pslOperator = library.pslOperators[i];
			if (!PSLProperties.EXAMPLE_OPERATOR_ARRAY[i][1].equals(pslOperator.getName())) {
				assertTrue("Different Operators #" + (i + 1) + " in " + PSLProperties.class.getSimpleName()
						+ " and " + PPGLibrary.class.getSimpleName(), false);
			}
			String[] extractedOperands = pslOperator.extractOperandLinesFrom(propertyLine);

			/* Check OPERANDS */
			assertArrayEquals("Wrong operands extracted for operator " + pslOperator.getName(), correctOperands, extractedOperands);

		}
	}
}
