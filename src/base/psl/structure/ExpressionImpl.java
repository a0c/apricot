package base.psl.structure;

import java.util.List;
import java.util.LinkedList;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 24.09.2008
 * <br>Time: 13:27:28
 */
public class ExpressionImpl extends AbstractExpression {
    private final PSLOperator pslOperator;
    private final List<AbstractExpression> operands;
    private Range window;

    public ExpressionImpl(PSLOperator pslOperator, Range window) {
        this.pslOperator = pslOperator;
        this.window = window;
        operands = new LinkedList<AbstractExpression>();
    }

    public void addOperand(AbstractExpression operand) {
        operands.add(operand);
    }

    /* #### G E T T E R S #### */

    public PSLOperator getPslOperator() {
        return pslOperator;
    }

    public AbstractExpression[] getOperands() {
        return operands.toArray(new AbstractExpression[operands.size()]);
    }

    public Range getWindow() {
        return window;
    }

    public AbstractExpression getOperandByName(String operandName) {
        return operands.get(pslOperator.getOperandIndex(operandName));
    }
}
