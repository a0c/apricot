package base.helpers;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 27.10.2008
 * <br>Time: 9:47:38
 */
public class MatchAndSplitRegexHolder {
    private String matchingRegex;
    private String splittingRegex;

    public MatchAndSplitRegexHolder(String matchingRegex, String splittingRegex) {
        this.matchingRegex = matchingRegex;
        this.splittingRegex = splittingRegex;
    }

    public String getMatchingRegex() {
        return matchingRegex;
    }

    public String getSplittingRegex() {
        return splittingRegex;
    }
}
