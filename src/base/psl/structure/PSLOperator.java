package base.psl.structure;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.variables.GraphVariable;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.12.2007
 * <br>Time: 23:11:31
 */
public class PSLOperator {
    private final String name; // as it is defined in "Operators" section of the PPG Library file
    private final String matchingRegex;
    private final String splittingRegex;
    private final int splitLimit;
    private String[] windowPlaceholders;
    private final Map<String, Integer> operandIndexByName;
    private BehModel model;
//    private Range window;


    public PSLOperator(String name, String matchingRegex, String splittingRegex, int splitLimit, String[] windowPlaceholders, Map<String, Integer> operandIndexByName) {
        this.name = name;
        this.matchingRegex = matchingRegex;
        this.splittingRegex = splittingRegex;
        this.splitLimit = splitLimit;
        this.windowPlaceholders = windowPlaceholders;
        this.operandIndexByName = operandIndexByName;
    }


    public String[] extractOperandLinesFrom(String propertyLine) {
        List<String> operandList = new LinkedList<String>();
        String[] operands = propertyLine.split(splittingRegex, splitLimit);
        for (String operand : operands) {
            operand = operand.trim();
            if (operand.length() > 0) {
                operandList.add(operand);
            }
        }
        return operandList.toArray(new String[operandList.size()]);
    }

    /* GETTERS */

    public String getName() {
        return name;
    }

    public String getMatchingRegex() {
        return matchingRegex;
    }

    public BehModel getModel() {
        return model;
    }

//    public Range getWindow() {
//        return window;
//    }

    public String[] getWindowPlaceholders() {
        return windowPlaceholders;
    }

    public GraphVariable getPropertyGraph() {
        return (GraphVariable) model.getVariableByIndex(model.graphOffset());
    }

    public void setModel(BehModel model) {
        this.model = model;
    }

    public void setWindowPlaceholders(String[] windowPlaceholders) {
        this.windowPlaceholders = windowPlaceholders;
    }

    /**
     *
     * @param       operandName the name of operand (e.g. BOP, TOP) to get index of
     *
     * @return      index of the operand with name <code>operandName</code>,
     *              or -1 if no such name is found.
     */
    public int getOperandIndex(String operandName) {
        return operandIndexByName.containsKey(operandName) ? operandIndexByName.get(operandName) : -1;
    }
}
