package io.helpers;

import java.util.Map;

/**
 * @author Anton Chepurov
 */
public class PSLOperatorDataHolder {
	private final String operatorName;
	private final String matchingRegexp;
	private final String splittingRegexp;
	private final int splitLimit;
	private final String[] windowPlaceholders;
	private final Map<String, Integer> operandIndexByName;

	public PSLOperatorDataHolder(String operatorName, String matchingRegexp,
								 String splittingRegexp, int splitLimit,
								 String[] windowPlaceholders,
								 Map<String, Integer> operandIndexByName) {
		this.operatorName = operatorName;
		this.matchingRegexp = matchingRegexp;
		this.splittingRegexp = splittingRegexp;
		this.splitLimit = splitLimit;
		this.windowPlaceholders = windowPlaceholders;
		this.operandIndexByName = operandIndexByName;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public String getMatchingRegexp() {
		return matchingRegexp;
	}

	public String getSplittingRegexp() {
		return splittingRegexp;
	}

	public int getSplitLimit() {
		return splitLimit;
	}

	public String[] getWindowPlaceholders() {
		return windowPlaceholders;
	}

	public Map<String, Integer> getOperandIndexByName() {
		return operandIndexByName;
	}
}
