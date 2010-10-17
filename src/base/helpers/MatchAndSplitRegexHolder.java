package base.helpers;

/**
 * @author Anton Chepurov
 */
public class MatchAndSplitRegexHolder {
	private String matchingRegexp;
	private String splittingRegexp;

	public MatchAndSplitRegexHolder(String matchingRegexp, String splittingRegexp) {
		this.matchingRegexp = matchingRegexp;
		this.splittingRegexp = splittingRegexp;
	}

	public String getMatchingRegexp() {
		return matchingRegexp;
	}

	public String getSplittingRegexp() {
		return splittingRegexp;
	}
}
