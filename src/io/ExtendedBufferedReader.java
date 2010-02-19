package io;

import java.io.*;

/**
 * Class represents a wrapper for BufferedReader class.
 *
 * User: Anton Chepurov
 * Date: 10.02.2007
 * Time: 19:18:56
 */
public class ExtendedBufferedReader extends BufferedReader {

    private static final String defaultComment = "--";

    private String comment;

    private String lastReadLine;

    /**
     * Constructor based on file. "--" is used as a default comment.
     * @param file File to read data from
     * @throws IOException - if the file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading.
     */
    public ExtendedBufferedReader(File file) throws IOException {
        this(new FileReader(file), defaultComment);
    }

    /**
     * Constructor based on file and user chosen comment.
     * @param file File to read data from
     * @param comment Comment indicator
     * @throws IOException - if the file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading.
     */
    public ExtendedBufferedReader(File file, String comment) throws IOException {
        this(new FileReader(file), comment);
    }

    public ExtendedBufferedReader(Reader in, String comment) {
        super(in);

        this.comment = comment;
    }

    /* Getters START */
    public String getComment() {
        return comment;
    }

    public String getLastReadLine() {
        return lastReadLine;
    }

    /* Getters END */



    /**
     * Reads a significant line and returns it. Lines started with delimiter are considered insignificant and are skipped.
     * @return A trimmed line or null if EOF reached
     * @throws IOException If an I/O error uccurs
     */
    public String readSignificantLine() throws IOException {

        do {
            lastReadLine = readLine();
            // EOF reached
            if (lastReadLine == null) return null;

            // skip comments
            if (lastReadLine.startsWith(comment)) continue;

            // cut off comments from the end
            if (lastReadLine.contains(comment)) lastReadLine = lastReadLine.substring(0, lastReadLine.indexOf(comment));

            lastReadLine = lastReadLine.trim();

            // cut off "=>" from splitted WHEN statement
            if (lastReadLine.startsWith("=>")) lastReadLine = lastReadLine.substring(2).trim();
            // skip "THEN" (splitted IF statement)
            if (lastReadLine.toUpperCase().equals("THEN")) lastReadLine = "";

        } while (lastReadLine.length() == 0 || lastReadLine.startsWith(comment));

        return lastReadLine;

    }

}
