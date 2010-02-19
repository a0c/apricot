package base;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import io.scan.LexemeComposer;
import io.scan.VHDLScanner;

import java.util.Set;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 26.03.2009
 * <br>Time: 14:56:46
 */
public class VHDL2HLDDMappingTest {
    private LexemeComposer lexemeComposer;
    private static final VHDL2HLDDMapping mapping = VHDL2HLDDMapping.getInstance();

    @Before public void clearVHDLMapping() {
        mapping.clear();
    }

    @Test
    public void correctLineSeparation() throws Exception {
        /* Double lines with different line separators */
        String sources[] = {
                "asdf\nfdsa", "asdf \nfdsa", "asdf\n fdsa", "asdf \n fdsa", // Unix / Win / modern Mac
                "asdf\rfdsa", "asdf \rfdsa", "asdf\r fdsa", "asdf \r fdsa", // Old Mac
                "asdf\r\nfdsa", "asdf \r\nfdsa", "asdf\r\n fdsa", "asdf \r\n fdsa" // Win
        };
        for (String source : sources) {
            /* Clear VHDL2HLDDMapping */
            clearVHDLMapping();
            /* Feed LexemeComposer with line and read the whole source through */
            lexemeComposer = new LexemeComposer(source, false);
            while (lexemeComposer.nextLexeme() != null);
            /* Check the number of lines */
            assertEquals(2, mapping.getCurrentLineCount());
        }
        /* Single line */
        clearVHDLMapping();
        new LexemeComposer("asdf").nextLexeme();
        assertEquals(1, mapping.getCurrentLineCount());
    }

    @Test public void correctLineSeparationFromFile() throws Exception {
        lexemeComposer = new LexemeComposer(VHDL2HLDDMappingTest.class.getResourceAsStream("crc_demo_Min.vhd"));
        clearVHDLMapping();
        while (lexemeComposer.nextLexeme() != null);
        /* Check the number of lines */
        assertEquals(354, mapping.getCurrentLineCount());
    }

    @Test public void correctMultipleTokensOnLine() throws Exception {
        lexemeComposer = new LexemeComposer("wert <= qwer; --somecomment\nasdfds <= fafsd;", false);
        VHDLScanner vhdlScanner = new VHDLScanner(lexemeComposer);
        /* Ask for one next token */
        vhdlScanner.next();
        Set<VHDL2HLDDMappingItem> currentMappingItems = mapping.getCurrentLines();
        assertEquals(1, currentMappingItems.size());
        assertEquals(1, currentMappingItems.iterator().next().getLineNumber());
        assertEquals("wert <= qwer;", currentMappingItems.iterator().next().getLine());
        /* Ask for the next token */
        vhdlScanner.next();
        currentMappingItems = mapping.getCurrentLines();
        assertEquals(1, currentMappingItems.size());
        assertEquals(2, currentMappingItems.iterator().next().getLineNumber());
        assertEquals("asdfds <= fafsd;", currentMappingItems.iterator().next().getLine());

    }
}
