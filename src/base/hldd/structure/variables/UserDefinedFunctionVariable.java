package base.hldd.structure.variables;

import base.hldd.structure.models.utils.PartedVariableHolder;
import base.Indices;
import base.Type;

import java.util.ArrayList;

/**
 * FunctionVariable with the following properties:<br>
 * 1) <code>operator</code> is user defined (is instance of {@link String}, not of
 * {@link base.vhdl.structure.Operator});<br>
 * 2) arbitrary number of <code>operands</code>.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.10.2008
 * <br>Time: 21:35:21
 */
public class UserDefinedFunctionVariable extends FunctionVariable {
    private final String userDefinedOperator;
    private final int expectedOperandsSize;

    /**
     * 
     * @param userDefinedOperator
     * @param functionName function name
     * @param expectedOperandsSize number of operands to expect. Facilitates the use of
     *        {@link java.util.ArrayList} as operands storage. Thus operands
     *        can be accessed quickly by their index.
     * @param length
     */
    public UserDefinedFunctionVariable(String userDefinedOperator, String functionName,
                                       int expectedOperandsSize, Indices length) {
        super(functionName);
        this.userDefinedOperator = userDefinedOperator;
        this.expectedOperandsSize = expectedOperandsSize;
        this.operands = new ArrayList<PartedVariableHolder>(expectedOperandsSize);
        this.type = new Type(length);
    }

    protected Type adjustType(Type currentType, Type addedType, Indices addedPartedIndices) {
        /* Don't update type. It is finally set in constructor.
        *  Return current type. */
        return type;
    }

    protected boolean isValidAddition() {
        return operands.size() < expectedOperandsSize;
    }

    protected String operatorToString() {
        return userDefinedOperator;
    }

    public String getUserDefinedOperator() {
        return userDefinedOperator;
    }

    public boolean isIdenticalTo(AbstractVariable comparedAbsVariable) /*throws Exception*/ {
        if (getClass() != comparedAbsVariable.getClass()) return false;
        UserDefinedFunctionVariable compUDVariable = (UserDefinedFunctionVariable) comparedAbsVariable;

        if (!userDefinedOperator.equalsIgnoreCase(compUDVariable.userDefinedOperator)) return false;
        if (!type.equals(compUDVariable.type)) return false;
        if (expectedOperandsSize != compUDVariable.expectedOperandsSize) return false;
        for (int i = 0; i < operands.size(); i++) {
            if (!operands.get(i).isIdenticalTo(compUDVariable.operands.get(i))) return false;
        }

        return true;
    }
}
