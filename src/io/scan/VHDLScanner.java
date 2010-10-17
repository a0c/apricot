package io.scan;

import base.SourceLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

/**
 * Class represents a Lexical Analyzer (Scanner) for VHDL Subset description
 * given (currently) in "VHDL Beh RTL Subset Description.doc".
 * <p/>
 * It's main purpose is to provide VHDLReader-s with complex tokens (class Token) that readers can process.
 * E.g.: If a vhdl file contains the following:
 * 'ENTITY' entity_name 'IS'
 * 'PORT' '('
 * then it will be rearranged to the following complex token:
 * 'ENTITY' entity_name 'IS' 'PORT' '('
 * <p/>
 * Thus, the class performs line reconstruction and rearranges the input
 * character sequence into a canonical form ready for the parser.
 *
 * @author Anton Chepurov
 */
public class VHDLScanner {
	private static final Pattern CLOCK_EVENT_PATTERN = Pattern.compile(".* 'EVENT $");
	private LexemeComposer lexemeComposer;
	private boolean allowIncompleteTokens = false;
	private File sourceFile = null;

	public VHDLScanner(LexemeComposer lexemeComposer) {
		this.lexemeComposer = lexemeComposer;
		allowIncompleteTokens = true;
	}

	public VHDLScanner(File file) throws FileNotFoundException {
		sourceFile = file;
		lexemeComposer = new LexemeComposer(file);
	}

	public File getSourceFile() {
		return sourceFile;
	}

	/**
	 * Constructs a complex {@link VHDLToken} out of simple {@link Lexeme Lexemes}.
	 * Created Token will be processed by VHDLReader-s.
	 * All possible types of Tokens are defined by {@link VHDLToken.Type} enumeration.
	 *
	 * @return a complex Token with corresponding Token.Type, if the Token has been identified
	 * 		   or an empty Token with UNKNOWN Token.Type, if the Token could not be identified
	 * 		   or null if EOF is reached.
	 * @throws Exception see {@link LexemeComposer#nextLexeme()}
	 */
	public VHDLToken next() throws Exception {
		StringBuffer complexTokenValue = new StringBuffer();
		VHDLToken.Type complexTokenType = VHDLToken.Type.UNKNOWN;
		lexemeComposer.purgeCurrentLines();

		while (true) {
			/* Read next LEXEME */
			Lexeme lexeme = lexemeComposer.nextLexeme();
			if (lexeme == null) {
				/* EOF reached */
				lexemeComposer.close(); // release the resource
				return allowIncompleteTokens && complexTokenValue.length() > 0 ?
						new VHDLToken(complexTokenType, complexTokenValue.toString().trim()) : null;
			}

			/* Append lexeme */
			appendLexemeAndManageSpace(complexTokenValue, lexeme);

			if (lexeme.getType() == LexemeType.SEMICOLON) {
				/* RETURN Token, because SEMICOLON always denotes an end of a statement in VHDL */
				complexTokenValue = trimClosingBracket(complexTokenValue);
				complexTokenType = VHDLToken.diagnoseType(complexTokenValue.toString().trim());
				return new VHDLToken(complexTokenType, complexTokenValue.toString().trim());
			}

			/* Check the TYPE of the Token only for the 4 lexemes as follows: identifier  (   )   >  */
			else if (lexeme.getType() == LexemeType.IDENTIFIER || lexeme.getType() == LexemeType.OPEN_BRACKET
					|| lexeme.getType() == LexemeType.CLOSE_BRACKET || lexeme.getType() == LexemeType.GT) {
				/* Define the TYPE of the Token. */
				complexTokenType = VHDLToken.diagnoseType(complexTokenValue.toString().trim());
				/* RETURN Token, if identifiable Token.Type is found */
				if (complexTokenType != VHDLToken.Type.UNKNOWN) {
					return new VHDLToken(complexTokenType, complexTokenValue.toString().trim());
				}
			}
		}

	}

	/**
	 * Checks for closing bracket of PORT or GENERIC declaration, like, "cont_eql: in bit);"
	 *
	 * @param complexTokenValue StringBuffer where to check closing bracket
	 * @return received unmodified buffer if no closing bracket is present, or
	 * 		   received buffer with deleted closing bracket
	 */
	private StringBuffer trimClosingBracket(StringBuffer complexTokenValue) {
		/* Count open and close bracket occurrences */
		int openBracketCount = complexTokenValue.toString().split("\\(").length - 1;
		int closeBracketCount = complexTokenValue.toString().split("\\)").length - 1;
		/* If the amounts are different, then it's a closing bracket for PORT declaration */
		if (closeBracketCount > openBracketCount) {
			int lastClosingIndex = complexTokenValue.lastIndexOf(")");
			complexTokenValue.delete(lastClosingIndex, lastClosingIndex + 2);
		}
		return complexTokenValue;
	}

	public SourceLocation getCurrentSource() {
		return lexemeComposer.getCurrentSource();
	}

	public void close() {
		lexemeComposer.close();
	}

	/**
	 * Method is used for appending lexemes to each other, according to the
	 * 3 following simple rules:
	 * <p/>
	 * 1) Space character is added after all the lexemes, excluding the following
	 * 9 characters:  - : ' " < = / > #  <br>
	 * 2) The added space character is removed, if it follows a digit and
	 * precedes a single/double quote (e.g. "111111 " )<br>
	 * 3) The added space character is removed, if it follows a single quote<br>
	 * <p/>
	 * The resulting string is accumulated in the {@code destinationStrBuf} parameter
	 *
	 * @param destinationStrBuf StringBuffer to accumulate the {@code lexeme} into
	 * @param lexeme			Lexeme to append to the {@code destinationStrBuf}
	 */
	public void appendLexemeAndManageSpace(StringBuffer destinationStrBuf, Lexeme lexeme) { //todo: maybe rename to manageSpace (see PSLScanner)
		/* Append lexeme */
		destinationStrBuf.append(lexeme.getValue());

		/* Manage SPACE */
		if (!(lexeme.getType() == LexemeType.OP_SUBTR || lexeme.getType() == LexemeType.COLON
				|| lexeme.getType() == LexemeType.SINGLE_QUOTE || lexeme.getType() == LexemeType.DOUBLE_QUOTE
				|| lexeme.getType() == LexemeType.LT || lexeme.getType() == LexemeType.OP_EQ || lexeme.getType() == LexemeType.OP_DIV)
				|| lexeme.getType() == LexemeType.GT || lexeme.getType() == LexemeType.SHARP) {
			/* Don't place a SPACE character after the 8 characters as follows:   - : ' " < = / > #  */
			destinationStrBuf.append(" ");
			// (CLOCK 'EVENT )
			/* Remove space between CLOCK and 'EVENT */
			if (CLOCK_EVENT_PATTERN.matcher(destinationStrBuf).matches()) {
				int expectedSpaceIndex = destinationStrBuf.length() - 8;
				if (Character.isWhitespace(destinationStrBuf.charAt(expectedSpaceIndex))) {
					destinationStrBuf.deleteCharAt(expectedSpaceIndex);
				}
			}
		} else if ((lexeme.getType() == LexemeType.SINGLE_QUOTE || lexeme.getType() == LexemeType.DOUBLE_QUOTE)
				&& destinationStrBuf.length() > 2) {
			/* If appended quote was a closing one ( i.e. before appending a quote the ultimate character was
			* a space and the penultimate one was a digit), then remove the space inside the quotes and add a trailing one */
			int lastCharIndex = destinationStrBuf.length() - 2;
			if (destinationStrBuf.charAt(lastCharIndex) == ' '
					&& Character.isDigit(destinationStrBuf.charAt(lastCharIndex - 1))) {
				destinationStrBuf.deleteCharAt(lastCharIndex);
				destinationStrBuf.append(" ");
			}
		}
	}
}
