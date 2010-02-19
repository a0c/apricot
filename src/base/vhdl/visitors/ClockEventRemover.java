package base.vhdl.visitors;

import base.vhdl.structure.nodes.*;
import base.vhdl.structure.Expression;
import base.vhdl.structure.*;
import base.vhdl.structure.Process;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 13.02.2008
 * <br>Time: 23:28:38
 */
public class ClockEventRemover extends AbstractVisitor {
    /**
     * Flag for speed up. It is implied that a process contains only 1 Clocking node in its tree.
     */
    private boolean isDone;
    private Process currentProcess;
    /* Here only request processing of AbstractNodes(ParseTree) */
    public void visitEntity(Entity entity) throws Exception {}

    public void visitArchitecture(Architecture architecture) throws Exception {}

    public void visitProcess(Process process) throws Exception {
        isDone = false;
        currentProcess = process;
        process.getRootNode().traverse(this);
    }

    public void visitIfNode(IfNode ifNode) throws Exception {
        Expression condition = ifNode.getConditionExpression();

        /* Speed Up */
        if (isDone) return;

        /*
        * 1)   IF clock'event and clock='1' THEN
        * 2)   elsif CLOCK'event and CLOCK='1' then
        * */
        if (isClockingExpression(condition)) {
            /* Link its true part to its parent */
            ifNode.replaceWith(ifNode.getTruePart(), currentProcess);
            isDone = true;
            return;
        }

        /*##########################################
        *    P r o c e s s    T R U E   P A R T
        * ##########################################*/
        ifNode.getTruePart().traverse(this);

        /*##########################################
        *    P r o c e s s    F A L S E   P A R T
        * ##########################################*/
        if (ifNode.getFalsePart() != null) {
            ifNode.getFalsePart().traverse(this);
        }


    }

    /**
     * CLOCK'EVENT and CLOCK = '1'
     * @param condition condition to check
     * @return  <code>true</code> if the <code>condition</code> represents a clocking expression.
     *          <code>false</code> otherwise.
     */
    public static boolean isClockingExpression(Expression condition) {
        return condition.getOperator() == Operator.AND
                && (condition.getOperands().get(0) instanceof OperandImpl)
                && ((OperandImpl) condition.getOperands().get(0)).getName().contains("\'EVENT")
                || isComparingClockForEquality(condition);
    }

    /**
     * Checks whether the expression compares the CLOCK signal with smth.
     * {@link base.vhdl.visitors.GraphGenerator#isClockName(String)} is
     * used for this.
     * @param expression expression to check
     * @return <code>true</code> if the specified expression compares the
     *         CLOCK signal with smth. <code>false</code> othetwise.
     */
    static boolean isComparingClockForEquality(Expression expression) {
        /* Check to be the EQ expression */
        if (expression.getOperator() != Operator.EQ) return false;
        /* Search for CLOCK variable amongst operands */
        for (AbstractOperand operand : expression.getOperands()) {
            if (operand instanceof OperandImpl) {
                if (GraphGenerator.isClockName(((OperandImpl) operand).getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public void visitTransitionNode(TransitionNode transitionNode) throws Exception {}

    public void visitCaseNode(CaseNode caseNode) throws Exception {}

    public void visitWhenNode(WhenNode whenNode) throws Exception {}
}
