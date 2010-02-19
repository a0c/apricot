package io.scan;

import org.junit.Test;
import static org.junit.Assert.*;
import helpers.ThrowableFlag;
import helpers.PSLProperties;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 10.09.2008
 * <br>Time: 9:39:48
 */
public class PSLScannerTest {
    private PSLScanner pslScanner;


    @Test
    public void nullReturnedWhenEndFound() throws Exception {
        pslScanner = new PSLScanner("");
        assertNull(pslScanner.next());

        pslScanner = new PSLScanner(" ");
        assertNull(pslScanner.next());

        pslScanner = new PSLScanner(" sdf12;  ");
        assertEquals("sdf12 ;", pslScanner.next());
        assertNull(pslScanner.next());
    }

    @Test
    public void semicolonActsAsPropertySeparator() throws Exception {
        pslScanner = new PSLScanner("blabla; bla;   asdfds ; sd;");
        int count = 0;
        while (pslScanner.next() != null) count++;

        assertEquals(4, count);
    }

    @Test (expected = ThrowableFlag.class)
    public void unclosedTokenDetected() throws ThrowableFlag {
        boolean first = false;
        pslScanner = new PSLScanner("    blabla   ");
        try {
            pslScanner.next();
        } catch (Exception e) {
            first = true;
        }
        pslScanner = new PSLScanner("    blabla   ; 3224; as1_");
        try {
            while (pslScanner.next() != null);
        } catch (Exception e) {
            /* If both are detected, then it's OK */
            if (first) {
                throw new ThrowableFlag();
            }
        }
    }

    @Test
    public void spaceAfterIdentifierAndBracket() throws Exception {
        pslScanner = new PSLScanner("one   two\tthree(a and (b));");
        assertEquals("one two three ( a and ( b ) ) ;", pslScanner.next());
    }

    @Test
    public void noSpaceBetweenMinusAndGT() throws Exception {
        pslScanner = new PSLScanner("->;");
        assertEquals("-> ;", pslScanner.next());
    }

    @Test
    public void noSpaceBeforeOpenSquareBracket() throws Exception {
        pslScanner = new PSLScanner("next_e[];");
        assertEquals("next_e[ ] ;", pslScanner.next());
    }

    @Test
    public void noSpaceBetweenDIVAndEQ() throws Exception {
        pslScanner = new PSLScanner("a/=b;");
        assertEquals("a /= b ;", pslScanner.next());
    }

    @Test
    public void noSpaceBetweenLTAndMinusAndGT() throws Exception {
        pslScanner = new PSLScanner("winner<->looser;");
        assertEquals("winner <-> looser ;", pslScanner.next());
        pslScanner = new PSLScanner("winner\t\t<->     looser;");
        assertEquals("winner <-> looser ;", pslScanner.next());
    }

    @Test
    public void completeTest() throws Exception {

        for (int i = 0; i < PSLProperties.unformattedPropertyArray.length; i++) {
            String source = PSLProperties.unformattedPropertyArray[i];
            String result = PSLProperties.formattedPropertyArray[i];

            pslScanner = new PSLScanner(source);
            assertEquals(result, pslScanner.next());
        }

    }


}
