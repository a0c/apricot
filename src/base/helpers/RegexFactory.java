package base.helpers;

/**
 * @author Anton Chepurov
 */
public class RegexFactory {

	private static final String REGEXP_META_CHARACTERS = "([{\\^-$|]})?*+.";

	public static String createStringRegexp(String sourceString) {
		StringBuffer stringBuffer = new StringBuffer();
		char[] chars = sourceString.toCharArray();

		for (char aChar : chars) {
			if (Character.isLetter(aChar)) {
				stringBuffer.append("[").append(Character.toLowerCase(aChar)).append(Character.toUpperCase(aChar)).append("]");
			} else if (REGEXP_META_CHARACTERS.contains(new StringBuffer("" + aChar))) {
				stringBuffer.append("\\").append(aChar);
			} else {
				stringBuffer.append(aChar);
			}
		}


		return stringBuffer.toString();
	}

	public static MatchAndSplitRegexHolder createMatchAndSplitRegexps(String word, boolean isFirstWordInPhrase, boolean withWindow) {
		String matchingRegexp, splittingRegexp;
		boolean startsWithLetter = Character.isLetter(word.charAt(0));
		boolean endsWithLetter = Character.isLetter(word.charAt(word.length() - 1));
		String splittingRegexpStart;
		String splittingRegexpEnd;
		String baseRegexp = RegexFactory.createStringRegexp(word);

		if (isFirstWordInPhrase) {
			/* Delimiter is the first word in the phrase */
			splittingRegexpEnd = !withWindow ? (endsWithLetter ? "[\\s\\(]" : ".") : "\\s*\\[.+\\]";

			/* Append START */
			splittingRegexp = "^" + baseRegexp;
			/* Append END */
			splittingRegexp += endsWithLetter ? splittingRegexpEnd : "";

			matchingRegexp = "^" + baseRegexp + splittingRegexpEnd + ".*";


		} else {
			/* Delimiter is in the middle of the phrase */
			splittingRegexpStart = startsWithLetter ? "[\\s\\)]" : ".";
			splittingRegexpEnd = endsWithLetter ? "[\\s\\(]" : ".";

			/* Append START */
			splittingRegexp = startsWithLetter ? splittingRegexpStart + baseRegexp : baseRegexp;
			/* Append END */
			splittingRegexp += endsWithLetter ? splittingRegexpEnd : "";

			matchingRegexp = ".*" + splittingRegexpStart + baseRegexp + splittingRegexpEnd + ".*";

		}

		return new MatchAndSplitRegexHolder(matchingRegexp, splittingRegexp);
	}

}
