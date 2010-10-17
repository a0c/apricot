package base.helpers;

/**
 * @author Anton Chepurov
 */
public class MatchAndSplitRegexpHolder {
	private String matchingRegexp;
	private String splittingRegexp;

	public MatchAndSplitRegexpHolder(String matchingRegexp, String splittingRegexp) {
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
