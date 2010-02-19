package io.helpers;

import java.util.Map;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 27.10.2008
 * <br>Time: 10:06:30
 */
public class PSLOperatorDataHolder {
    private final String operatorName;
    private final String matchingRegex;
    private final String splittingRegex;
    private final int splitLimit;
    private final String[] windowPlaceholders;
    private final Map<String, Integer> operandIndexByName;

    public PSLOperatorDataHolder(String operatorName, String matchingRegex,
                                 String splittingRegex, int splitLimit,
                                 String[] windowPlaceholders,
                                 Map<String, Integer> operandIndexByName) {
        this.operatorName = operatorName;
        this.matchingRegex = matchingRegex;
        this.splittingRegex = splittingRegex;
        this.splitLimit = splitLimit;
        this.windowPlaceholders = windowPlaceholders;
        this.operandIndexByName = operandIndexByName;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getMatchingRegex() {
        return matchingRegex;
    }

    public String getSplittingRegex() {
        return splittingRegex;
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
