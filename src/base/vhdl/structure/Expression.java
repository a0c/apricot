package base.vhdl.structure;

import java.util.List;
import java.util.ArrayList;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 26.02.2008
 * <br>Time: 11:28:08
 */
public class Expression extends AbstractOperand {
    private Operator operator;
    private List<AbstractOperand> operands;

    public Expression(Operator operator, boolean isInverted) {
        super(isInverted);
        this.operator = operator;
        operands = new ArrayList<AbstractOperand>(5);
    }

    public void addOperand(AbstractOperand operand) {
        operands.add(operand);
    }

    public List<AbstractOperand> getOperands() {
        return operands;
    }

    public Operator getOperator() {
        return operator;
    }

    //todo: override equals()! Expressions are used in HashMaps as Keys!!!
    //todo: No! Think carefully! FixedHighestSBStorage.fixedHighestSBByOperand -> identical operand may be returned by getHighestSBImposedBy()...
    //todo: but is it bad??? if all the expressions are immutable (and only 1 copy exists for identical expressions), then everything is fine! Even quicker!

    public boolean isInverseOf(AbstractOperand comparedOperand) {

        if (!(comparedOperand instanceof Expression)) return false;
        Expression comparedExpression = (Expression) comparedOperand;

        /* Check EVERYTHING BUT ISINVERTED to be equal */
        /* Check OPERATORS */
        if (operator != comparedExpression.getOperator()) return false;

        /* Check the AMOUNT of OPERANDS */
        List<AbstractOperand> comparedOperands = comparedExpression.getOperands();
        if (operands.size() != comparedOperands.size()) return false;
        /* Check every OPERAND */
        for (int i = 0; i < operands.size(); i++) {
            if (!operands.get(i).isIdenticalTo(comparedOperands.get(i))) return false;
        }

        /* Check isInverted */
        return isInverted() != comparedExpression.isInverted();
    }

    public boolean isIdenticalTo(AbstractOperand comparedAbstrOperand) {
        if (!(comparedAbstrOperand instanceof Expression)) return false;
        Expression comparedExpression = (Expression) comparedAbstrOperand;

        /* Check OPERATORS */
        if (operator != comparedExpression.getOperator()) return false;

        /* Check the AMOUNT of OPERANDS */
        List<AbstractOperand> comparedOperands = comparedExpression.getOperands();
        if (operands.size() != comparedOperands.size()) return false;
        /* Check every OPERAND */
        for (int i = 0; i < operands.size(); i++) {
            if (!operands.get(i).isIdenticalTo(comparedOperands.get(i))) return false;
        }

        /* Check isInverted */
        return isInverted() == comparedExpression.isInverted();

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isInverted()) {
            sb.append("NOT ");
        }
        sb.append("(");

        for (int i = 0; i < operands.size(); i++) {
            AbstractOperand operand = operands.get(i);
            sb.append(operand);
            if (i < operands.size() - 1) {
                sb.append(operator.getDelim());
            }
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * Expression is a Composite Condition, if its <code>operator</code> is not
     * actually a condition operator, but is an AND/OR operator, with all its
     * operands being a typical condition expression.
     * <p> 
     * In other words, expression is a Composite Condition, if it contains 
     * several Condition expressions or booleans wrapped with either AND or OR.
     * <p>
     * e.g.:<br>
     * 1) VOTO0 ='1' AND VOTO1 ='1' AND VOTO2 ='1' AND VOTO3 ='1' <br>
     * 2) ( ( VOTO0 ='1' ) AND ( ( VOTO1 ='1' ) AND ( ( VOTO2 ='1' ) AND ( VOTO3 ='1' ) ) ) ) <br>
     * 3) VOTO0 ='1' AND ( VOTO2 >'1' ) AND ( VOTO3 ='1' ) <br>
     * 4) non-composite: VOTO0 ='1' AND ( VOTO2 >'1' ) AND BOOLEAN_VARIABLE <br>
     * 5) non-composite: (((REG1) AND REG2) = '11' AND REG3) = '11' AND SOME_CONDITION
     *
     * @return whether the expression is a Composite Condition
     */
    public boolean isCompositeCondition() {
        return new CompositeConditionDetector(this).isComposite();
    }


	private class CompositeConditionDetector {
        private Expression rootExpression;

        public CompositeConditionDetector(Expression rootExpression) {
            this.rootExpression = rootExpression;
        }

        /**
         * @see Expression#isCompositeCondition() 
         * @return whether the <code>rootExpression</code> is composite
         */
        public boolean isComposite() {

            Operator rootOperator = rootExpression.getOperator();
            /* Only AND/OR expressions can be composite */
            if (rootOperator == Operator.AND || rootOperator == Operator.OR) {
                /* Expression is Composite only if all its children are conditional expressions */
                for (AbstractOperand operand : rootExpression.getOperands()) {
                    if (operand instanceof Expression) {
                        if (!((Expression) operand).getOperator().isCondition()
                                /*&& !((Expression) operand).isCompositeCondition()*/)
                            return false;
                    } else return false;
                }
                /* All children passed the check. rootExpression is composite. */
                return true;

            } else return false;
        }
    }

}
