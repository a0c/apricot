package io;

import org.junit.Test;
import static junit.framework.Assert.*;

import java.io.File;

import base.psl.structure.PPGLibraryTest;
import base.psl.structure.PSLOperator;
import helpers.PSLOperators;
import io.helpers.PSLOperatorDataHolder;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 18.06.2008
 * <br>Time: 12:57:10
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
        /* Check number of splitted tokesn */
        assertEquals(
                "Wrong number of splitted tokens for operationDeclaration \"" + fullOpDeclaration + "\"",
                expectedTokens.length,
                actualTokens.length
        );
        /* Check each splitted token */
        for (int i = 0; i < actualTokens.length; i++) {
            assertEquals(expectedTokens[i], actualTokens[i]);
        }
    }


    /**
     * To be run in Debug mode
     * @throws Exception if error occurs
     */
    @Test
    public void tempTest() throws Exception {

        PPGLibraryTest.createLibrary();
//        System.out.println("PPG Library read successfully");

    }

    @Test
    public void correctOperationDeclarationParse() {
        for (int i = 0; i < PSLOperators.operatorDeclarations.length; i++) {
            String operatorDeclaration = PSLOperators.operatorDeclarations[i];
            PSLOperatorDataHolder data = PPGLibraryReader.parseOperationDeclaration(operatorDeclaration);
            /* Check MATCHING REGEX */
            assertEquals("Matching regex is incorrect for operator \"" + operatorDeclaration + "\"",
                    PSLOperators.matchingRegexes[i], data.getMatchingRegex());
            /* Check SPLITTING REGEX */
            assertEquals("Splitting regex is incorrect for operator \"" + operatorDeclaration + "\"",
                    PSLOperators.splittingRegexes[i], data.getSplittingRegex());
        }

    }

}
