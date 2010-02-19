package io.scan;

import base.VHDL2HLDDMapping;

import java.io.*;

import io.QuietCloser;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 26.02.2008
 * <br>Time: 16:48:46
 */
public class LexemeComposer {

    private char lastReadChar;
    private BufferedReader bReader;
    private boolean toUpperCase = true;
    private VHDL2HLDDMapping vhdl2hlddMapping = VHDL2HLDDMapping.getInstance();

    private static final char NEW_LINE = '\r';
    private static final char END_OF_FILE = 65535;
    public static final String DEFAULT_COMMENT = "--";

    public LexemeComposer(String sourceString, boolean toUpperCase) {
        this(sourceString);
        this.toUpperCase = toUpperCase;
    }

    public LexemeComposer(File sourceFile, boolean toUpperCase) throws FileNotFoundException {
        this(sourceFile);
        this.toUpperCase = toUpperCase;
    }

    /**
     * Constructor with <code>toUpperCase == true</code> by default 
     * @param sourceString the source of lexemes
     */
    public LexemeComposer(String sourceString) {
        this(new StringReader(sourceString));
    }

    /**
     * Constructor with <code>toUpperCase == true</code> by default
     * @param sourceFile the source of lexemes
     * @throws FileNotFoundException see {@link java.io.FileReader#FileReader(java.io.File)}
     */
    public LexemeComposer(File sourceFile) throws FileNotFoundException {
        this(new FileReader(sourceFile));
    }

    /**
     * Constructor with <code>toUpperCase == true</code> by default 
     * @param stream the source of lexemes
     */
    public LexemeComposer(InputStream stream) {
        this(new InputStreamReader(stream));
    }

    private LexemeComposer(Reader reader) {
        bReader = new BufferedReader(reader);
    }


    /**
     * Reads next lexeme out of sourceString/sourceFile.
     * The set of all possible lexemes is defined by {@link LexemeType}.
     *
     * @return next read lexeme in UpperCase or null if EOF is reached
     * @throws java.io.IOException if an I/O error occurs
     * @throws java.lang.Exception {@link LexemeType#diagnoseType(char)}
     *
     */
    public Lexeme nextLexeme() throws Exception {
        StringBuilder newLexemeValue = new StringBuilder();
        LexemeType newLexemeType = null;

        /* Start reading a new character (without checking the lastReadChar)
        * either if it is the first scan or the lastReadChar was a whitespace */
        boolean ignoreLastReadChar = Character.isWhitespace(lastReadChar) || lastReadChar == 0;
        while (true) {
            /* Read next char */
            if (ignoreLastReadChar) {
                lastReadChar = (char) bReader.read();
                /* Append EVERY NEW READ char to VHDL2HLDD mapper */
                vhdl2hlddMapping.append(lastReadChar);
            }
            ignoreLastReadChar = true;
            /* Check EOF */
            if (lastReadChar == END_OF_FILE) {
                return newLexemeValue.length() == 0 ? null : new Lexeme(applyCase(newLexemeValue), newLexemeType);
            }
                            
            /* Skip whitespaces that precede the lexeme */
            if (newLexemeValue.length() == 0 && Character.isWhitespace(lastReadChar)) continue;
            /* Define the TYPE of the lexem. Do it only once, right at the beginning. */
            if (newLexemeType == null) {
                newLexemeType = LexemeType.diagnoseType(lastReadChar);
            }
            /* Append the character */
            newLexemeValue.append(lastReadChar);
            /* Check COMMENTS */
            if (newLexemeType == LexemeType.OP_SUBTR && newLexemeValue.toString().equals(DEFAULT_COMMENT)) {
                bReader.readLine();
                vhdl2hlddMapping.append(NEW_LINE);
//                while((lastReadChar = (char) bReader.read()) != '\n' && lastReadChar != 65535); // Skip Line
//                if (lastReadChar == 65535) return null;
                newLexemeType = null;
                newLexemeValue = new StringBuilder();
                continue;
            }
            /* Check if LexemeType accepts appended character  */
            if (!newLexemeType.accepts(newLexemeValue.toString())) {
                newLexemeValue.deleteCharAt(newLexemeValue.length() - 1);
                break;
            }
        }

        return new Lexeme(applyCase(newLexemeValue), newLexemeType);
    }

    private String applyCase(StringBuilder line) {
        return toUpperCase ? line.toString().toUpperCase() : line.toString();
    }

    public void close() {
        QuietCloser.closeQuietly(bReader);
    }

}
