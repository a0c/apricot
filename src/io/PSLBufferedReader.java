package io;

import java.io.*;
import java.util.Stack;

/**
 * Class is a wrapper for BufferedReader.
 *
 * @author Anton Chepurov
 */
public class PSLBufferedReader extends BufferedReader {

	public static final String DEFAULT_COMMENT = "--";

	private char lastReadChar;

	private String lastReadWord;

	public PSLBufferedReader(File pslFile) throws FileNotFoundException {
		this(new FileReader(pslFile));
	}

	public PSLBufferedReader(Reader in) {
		super(in);
		this.lastReadWord = "";
		this.lastReadChar = ' ';
	}

	public PSLBufferedReader(String block) {
		this(new StringReader(block));
	}

	/**
	 * @param regexp regular expression to match
	 * @return last read word matching regexp, or <code>null</code> if EOF reached.
	 * @throws IOException If an I/O error occurs
	 */
	public String readWordMatchingRegexp(String regexp) throws IOException {
		StringBuilder readChars = new StringBuilder();
		char tempChar;

		do {
			tempChar = (char) read();

			/* EOF reached, return null */
			if (tempChar == -1 || tempChar == 65535) return null;
			/* SAVE read char */
			lastReadChar = tempChar;
			/* Skip whitespace-s */
			if (Character.isWhitespace(lastReadChar) && readChars.length() == 0) {
				readChars = new StringBuilder();
				continue;
			}
			/* Skip comments */
			if (DEFAULT_COMMENT.equals(readChars.toString())) {
				readChars = new StringBuilder();
				readLine();
				continue;
			}

			/* Collect chars into a StringBuffer */
			readChars.append(lastReadChar);

		} while (!readChars.toString().matches(regexp) || DEFAULT_COMMENT.contains(readChars.toString()));

		/* If only 1 character is read, then return it as is.
		* If more than 1 character is read, then trim the last character. */
		lastReadWord = readChars.length() == 1 ? readChars.toString() : readChars.substring(0, readChars.length() - 1).trim();
		return lastReadWord;
	}

	/**
	 * @param regexp regular expression to skip to
	 * @return <code>true</code> if matching was found. <code>false</code> otherwise.
	 * @throws IOException If an I/O error occurs
	 */
	public boolean trySkippingToRegexp(String regexp) throws IOException {
		String word = readWordMatchingRegexp(regexp);
		lastReadWord = word;
		return word != null && word.length() > 0;
	}

	public String getLastReadWord() {
		return lastReadWord;
	}

	public String printLog() {

		return "\nLast read word: \'" + lastReadWord + "\'. Last read char: \'" + lastReadChar + "\'";
	}

	/**
	 * @param blockStarts char denoting the start of the block to read
	 * @param blockEnds char denoting the end of the block to read
	 * @param checkLastReadChar whether the last read char has to be reprocessed
	 * @return String containing the contents of the first occurrence of a block
	 * 		   enclosed in <code>blockStarts</code> and <code>blockEnds</code>, or
	 * 		   <code>null</code> if EOF was reached while such a block has not been found yet
	 * 		   (either a blockStarts was not found or blockEnds was not found).
	 * @throws IOException If an I/O error occurs
	 */
	public String readBlock(char blockStarts, char blockEnds, boolean checkLastReadChar) throws IOException {
		StringBuilder sb = new StringBuilder();
		/* Stack for keeping track of inner blocks. Used to distinguish the real blockEnd. */
		Stack<Boolean> stackOfBlocks = new Stack<Boolean>();
		char tempChar;

		/* try to append last read char, if asked */
		if (checkLastReadChar && lastReadChar == blockStarts) {
			sb.append(lastReadChar);
			stackOfBlocks.push(true);
		}

		/* Check for the START of a block */
		if (stackOfBlocks.isEmpty()) {
			/* Skip to the START of a block */
			if (!trySkippingToRegexp(String.valueOf(".*\\" + blockStarts))) return null;
			sb.append(blockStarts);
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
			sb.append(lastReadChar);

			/* New block is met. Push block. */
			if (lastReadChar == blockStarts) stackOfBlocks.push(true);

			else if (lastReadChar == blockEnds) {
				/* Block end is met. Pop block. */
				stackOfBlocks.pop();
				if (stackOfBlocks.isEmpty()) break; /* Real blockEnd is reached. Break the loop. */
			}
		}

		/* Block is read. Return it without enclosing characters. */
		return sb.substring(1, sb.length() - 1).trim();
	}
}
