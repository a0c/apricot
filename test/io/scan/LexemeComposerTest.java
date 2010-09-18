package io.scan;

import base.SourceLocation;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 10.09.2008
 * <br>Time: 11:06:20
 */
public class LexemeComposerTest {
    private LexemeComposer lexemeComposer;

    @Test
    public void nullReturnedWhenEndFound() throws Exception {
        lexemeComposer = new LexemeComposer("", false);
        assertNull(lexemeComposer.nextLexeme());

        lexemeComposer = new LexemeComposer(" ", false);
        assertNull(lexemeComposer.nextLexeme());

        lexemeComposer = new LexemeComposer(" 12  ", false);
        lexemeComposer.nextLexeme();
        assertNull(lexemeComposer.nextLexeme());
    }

    @Test
    public void underscoreWithinIdentifier() throws Exception {
        String[] sources = {"  _before    ", "  between_between    ", "  after_\t    "};
        String[] results = {"_before", "between_between", "after_"};

        for (int i = 0; i < sources.length; i++) {
            String source = sources[i];
            String result = results[i];
            lexemeComposer = new LexemeComposer(source, false);
            assertEquals(result, lexemeComposer.nextLexeme().getValue());
        }
    }

	/* VHDLLinesTracker */
	@Test
	public void correctLineSeparation() throws Exception {
		/* Double lines with different line separators */
		String sources[] = {
				"asdf\nfdsa", "asdf \nfdsa", "asdf\n fdsa", "asdf \n fdsa", // Unix / Win / modern Mac
				"asdf\rfdsa", "asdf \rfdsa", "asdf\r fdsa", "asdf \r fdsa", // Old Mac
				"asdf\r\nfdsa", "asdf \r\nfdsa", "asdf\r\n fdsa", "asdf \r\n fdsa" // Win
		};
		for (String source : sources) {
			/* Feed LexemeComposer with line and read the whole source through */
			lexemeComposer = new LexemeComposer(source, false);
			while (lexemeComposer.nextLexeme() != null);
			/* Check the number of lines */
			assertEquals(2, lexemeComposer.getCurrentLineCount());
		}
		/* Single line */
		lexemeComposer = new LexemeComposer("asdf");
		lexemeComposer.nextLexeme();
		assertEquals(1, lexemeComposer.getCurrentLineCount());
	}
	@Test public void correctLineSeparationFromFile() throws Exception {
		lexemeComposer = new LexemeComposer(LexemeComposerTest.class.getResourceAsStream("crc_demo_Min.vhd"));
		while (lexemeComposer.nextLexeme() != null);
		/* Check the number of lines */
		assertEquals(354, lexemeComposer.getCurrentLineCount());
	}
	@Test public void correctMultipleTokensOnLine() throws Exception {
		lexemeComposer = new LexemeComposer("wert <= qwer; --somecomment\nasdfds <= fafsd;", false);
		VHDLScanner vhdlScanner = new VHDLScanner(lexemeComposer);
		/* Ask for one next token */
		vhdlScanner.next();
		SourceLocation currentSource = lexemeComposer.getCurrentSource();
		assertNotNull(currentSource);
		assertEquals("1", currentSource.toString());
		/* Ask for another token */
		vhdlScanner.next();
		currentSource = lexemeComposer.getCurrentSource();
		assertNotNull(currentSource);
		assertEquals("2", currentSource.toString());
	}

}
