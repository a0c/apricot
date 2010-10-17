package io;

import org.junit.Test;

import static junit.framework.Assert.*;

import base.psl.structure.PPGLibraryTest;
import helpers.PSLOperators;
import io.helpers.PSLOperatorDataHolder;

/**
 * @author Anton Chepurov
 */
public class PPGLibraryReaderTest {

	@Test
	public void splitToTokensTest() {
		assertCorrect("always TOP", "always", "TOP");
		assertCorrect("never TOP", "never", "TOP");
		assertCorrect("BOP -> TOP", "BOP", "->", "TOP");
		assertCorrect("BOP1 until BOP2", "BOP1", "until", "BOP2");
		assertCorrect("next TOP", "next", "TOP");
		assertCorrect("next[n] TOP", "next[n]", "TOP");


//        next_e[start to end] TOP;
//        next_a[start to end] TOP;
//        BOP or TOP;
//        BOP and TOP;
//        ! TOP;

	}

	private void assertCorrect(String fullOpDeclaration, String... expectedTokens) {
		String[] actualTokens = PPGLibraryReader.splitToTokens(fullOpDeclaration);
		/* Check number of split tokens */
		assertEquals(
				"Wrong number of split tokens for operationDeclaration \"" + fullOpDeclaration + "\"",
				expectedTokens.length,
				actualTokens.length
		);
		/* Check each split token */
		for (int i = 0; i < actualTokens.length; i++) {
			assertEquals(expectedTokens[i], actualTokens[i]);
		}
	}


	/**
	 * To be run in Debug mode
	 *
	 * @throws Exception if error occurs
	 */
	@Test
	public void tempTest() throws Exception {

		/* Check PPG Library to be read successfully */
		PPGLibraryTest.createLibrary();

	}

	@Test
	public void correctOperationDeclarationParse() {
		for (int i = 0; i < PSLOperators.OPERATOR_DECLARATIONS.length; i++) {
			String operatorDeclaration = PSLOperators.OPERATOR_DECLARATIONS[i];
			PSLOperatorDataHolder data = PPGLibraryReader.parseOperationDeclaration(operatorDeclaration);
			/* Check MATCHING REGEXP */
			assertEquals("Matching regexp is incorrect for operator \"" + operatorDeclaration + "\"",
					PSLOperators.MATCHING_REGEXPS[i], data.getMatchingRegexp());
			/* Check SPLITTING REGEXP */
			assertEquals("Splitting regexp is incorrect for operator \"" + operatorDeclaration + "\"",
					PSLOperators.SPLITTING_REGEXPS[i], data.getSplittingRegexp());
		}

	}

}
