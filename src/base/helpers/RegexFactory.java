package base.helpers;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.12.2007
 * <br>Time: 10:03:11
 */
public class RegexFactory {

    private static final String REGEX_METACHARACTERS = "([{\\^-$|]})?*+.";

    public static String createStringRegex(String sourceString) {
        StringBuffer stringBuffer = new StringBuffer();
        char[] chars = sourceString.toCharArray();

        for (char aChar : chars) {
            if (Character.isLetter(aChar)) {
                stringBuffer.append("[").append(Character.toLowerCase(aChar)).append(Character.toUpperCase(aChar)).append("]");
            } else if (REGEX_METACHARACTERS.contains(new StringBuffer("" + aChar))) {
                stringBuffer.append("\\").append(aChar);
            } else {
                stringBuffer.append(aChar);
            }
        }


        return stringBuffer.toString();
    }

    public static MatchAndSplitRegexHolder createMatchAndSplitRegexes(String word, boolean isFirstWordInPhrase, boolean withWindow) {
        String matchingRegex, splittingRegex;
        boolean startsWithLetter = Character.isLetter(word.charAt(0));
        boolean endsWithLetter = Character.isLetter(word.charAt(word.length() - 1));
        String splittingRegexStart;
        String splittingRegexEnd;
        String baseRegex = RegexFactory.createStringRegex(word);

        if (isFirstWordInPhrase) {
            /* Delimiter is the first word in the phrase */
            splittingRegexEnd = !withWindow ? (endsWithLetter ? "[\\s\\(]" : ".") : "\\s*\\[.+\\]";

            /* Append START */
            splittingRegex = "^" + baseRegex;
            /* Append END */
            splittingRegex += endsWithLetter ? splittingRegexEnd : "";

            matchingRegex = "^" + baseRegex + splittingRegexEnd + ".*";


        } else {
            /* Delimiter is in the middle of the phrase */
            splittingRegexStart = startsWithLetter ? "[\\s\\)]" : ".";
            splittingRegexEnd = endsWithLetter ? "[\\s\\(]" : ".";

            /* Append START */
            splittingRegex = startsWithLetter ? splittingRegexStart + baseRegex : baseRegex;
            /* Append END */
            splittingRegex += endsWithLetter ? splittingRegexEnd : "";

            matchingRegex = ".*" + splittingRegexStart + baseRegex + splittingRegexEnd + ".*";

        }

        return new MatchAndSplitRegexHolder(matchingRegex, splittingRegex);
    }

}
