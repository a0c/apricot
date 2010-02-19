package io;

import java.io.*;
import java.util.Stack;

/**
 * Class is a wrapper for BufferedReader.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 19.11.2007
 * <br>Time: 20:45:08
 */
public class PSLBufferedReader extends BufferedReader {

    private static final String defaultComment = "--";

    private String comment;

    private char lastReadChar;
    private String lastReadWord;

    public PSLBufferedReader(File pslFile) throws FileNotFoundException {
        this(new FileReader(pslFile));
    }

    public PSLBufferedReader(Reader in) {
        super(in);
        this.comment = defaultComment;
        this.lastReadWord = "";
        this.lastReadChar = ' ';
    }

    public PSLBufferedReader(String block) {
        this(new StringReader(block));
    }

    /**
     *
     * @param regex regular expression to match
     *
     * @return last read word matching regex, or <code>null</code> if EOF reached.
     *
     * @throws IOException If an I/O error occurs
     */
    public String readWordMatchingRegex(String regex) throws IOException {
        StringBuffer readChars = new StringBuffer();
        char tempChar;

        do {
            tempChar = (char) read();

            /* EOF reached, return null */
            if (tempChar == -1 || tempChar == 65535) return null;
            /* SAVE read char */
            lastReadChar = tempChar;
            /* Skip whitespaces */
            if (Character.isWhitespace(lastReadChar) && readChars.length() == 0) {readChars = new StringBuffer(); continue;}
            /* Skip comments */
            if (comment.equals(readChars.toString())) {readChars = new StringBuffer(); readLine(); continue;}

            /* Collect chars into a StringBuffer */
            readChars.append(lastReadChar);

        } while (!readChars.toString().matches(regex) || comment.contains(readChars.toString()));

        /* If only 1 character is read, then return it as is.
         * If more than 1 character is read, then trim the last character. */
        lastReadWord = readChars.length() == 1 ? readChars.toString() : readChars.substring(0, readChars.length() - 1).trim();
        return lastReadWord;

    }

    /**
     * Read significant character from the stream.
     * Insignificant characters are whitespace characters.
     *
     * @return      last read significant character or <code>null</code> if EOF reached.
     *
     * @throws IOException If an I/O error occurs
     */
    public Character readSignificantChar() throws IOException {
        char tempChar;

        do {
            tempChar = (char) read();

            /* EOF reached, return null */
            if (tempChar == -1) return null;
            /* SAVE last read characher */
            lastReadChar = tempChar;
            /* Skip whitespaces */
            if (Character.isWhitespace(tempChar)) continue;

            return tempChar;

        } while (true);


    }

    /**
     *
     * @param regex regular expression to skip to
     *
     * @return <code>true</code> if matching was found. <code>false</code> otherwise.
     * 
     * @throws IOException If an I/O error occurs
     */
    public boolean skipToRegex(String regex) throws IOException {
        String word = readWordMatchingRegex(regex);
        lastReadWord = word;
        return word != null && word.length() > 0;
    }

    public char getLastReadChar() {
        return lastReadChar;
    }

    public String getLastReadWord() {
        return lastReadWord;
    }

    public String log() {

        return "\nLast read word: \'" + lastReadWord + "\'. Last read char: \'" + lastReadChar + "\'";

    }

    /**
     *
     * @param blockStarts
     * @param blockEnds
     * @param checkLastReadChar
     *
     * @return      String containing the contents of the first occurrence of a block
     *              enclosed in <code>blockStarts</code> and <code>blockEnds</code>, or
     *              <code>null</code> if EOF was reached while such a block has not been found yet
     *              (either a blockStarts was not found or blockEnds was not found).
     *
     * @throws      IOException If an I/O error occurs
     */
    public String readBlock(char blockStarts, char blockEnds, boolean checkLastReadChar) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        /* Stack for keeping track of inner blocks. Used to distinguish the real blockEnd. */
        Stack<Boolean> stackOfBlocks = new Stack<Boolean>();
        char tempChar;

        /* try to append last read char, if asked */
        if (checkLastReadChar && lastReadChar == blockStarts) {
            stringBuffer.append(lastReadChar);
            stackOfBlocks.push(true);
        }

        /* Check for the START of a block */
        if (stackOfBlocks.isEmpty()) {
            /* Skip to the START of a block */
            if (!skipToRegex(String.valueOf(".*\\" + blockStarts))) return null;
            stringBuffer.append(blockStarts);
            stackOfBlocks.push(true);
        }

        while (true) {
            /* read next character */
            tempChar = (char) read();
            /* EOF reached, return null */
            if (tempChar == -1 || tempChar == 65535) return null;
            /* Save read char */
            lastReadChar = tempChar;
            /* Collect chars into a StringBuffer */
            stringBuffer.append(lastReadChar);

            /* New block is met. Push block. */
            if (lastReadChar == blockStarts) stackOfBlocks.push(true);

            else if (lastReadChar == blockEnds) {
                /* Block end is met. Pop block. */
                stackOfBlocks.pop();
                if (stackOfBlocks.isEmpty()) break; /* Real blockEnd is reached. Break the loop. */
            }
        }

        /* Block is read. Return it without enclosing characters. */
        return stringBuffer.substring(1, stringBuffer.length() - 1).trim();

    }

    public void readTerminatingSemicolon() throws Exception {

        if (readWordMatchingRegex("[\\s]*;") == null) throw new Exception("\';\' is expected to terminate declaration." + log());


    }

    /**
     *
     * @param endChar
     * @param useLastReadChar
     * @param includeEndChar
     *
     * @return      String containing the contents of a block terminated by <code>endChar</code>, or
     *              <code>null</code> if EOF was reached while such a block has not been found yet
     *              (endChar was not found).
     *
     * @throws      IOException If an I/O error occurs
     */
    public String readBlock(char endChar, boolean useLastReadChar, boolean includeEndChar) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        char tempChar;
        Stack<Boolean> stackOfSeres = new Stack<Boolean>();

        if (useLastReadChar) stringBuffer.append(lastReadChar);
        if (lastReadChar == '{') stackOfSeres.push(true);

        while (true) {
            /* Read next char */
            tempChar = (char) read();
            /* EOF reached, return null */
            if (tempChar == -1 || tempChar == 65535) return null;
            /* Save read char */
            lastReadChar = tempChar;
            /* Collect chars into a StringBuffer */
            stringBuffer.append(lastReadChar);
            /* Add SERE if found */
            if (lastReadChar == '{') stackOfSeres.push(true);
            /* Remove SERE if found */
            if (lastReadChar == '}') stackOfSeres.pop();

            if (lastReadChar == endChar) {
                if (stackOfSeres.isEmpty()) break;
            }
        }


        return includeEndChar ? stringBuffer.toString() : stringBuffer.substring(0, stringBuffer.length() - 1);
    }

    public static String getDefaultComment() {
        return defaultComment;
    }
}
