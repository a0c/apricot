package base.vhdl.structure.nodes;

import base.vhdl.visitors.AbstractVisitor;
import base.vhdl.structure.OperandImpl;
import base.vhdl.processors.AbstractProcessor;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 08.02.2008
 * <br>Time: 18:53:30
 */
public class WhenNode extends AbstractNode {

    private OperandImpl[] conditionOperands;

    private CompositeNode transitions;

    public WhenNode(String... condition) throws Exception {
        conditionOperands = new OperandImpl[condition.length]; // new OperandImpl(condition);
        transitions = new CompositeNode();
        transitions.setParent(this);

        for (int i = 0; i < condition.length; i++) {
            conditionOperands[i] = new OperandImpl(condition[i]);
        }
    }

    public void addTransition(AbstractNode newTransition) {
        transitions.addNode(newTransition);
    }

    public boolean isOthers() {
        return conditionOperands.length == 1 && conditionOperands[0].getName().equals("OTHERS");
    }

    public CompositeNode getTransitions() {
        return transitions;
    }

    public void setConditions(String... conditionsAsString) throws Exception {
        conditionOperands = new OperandImpl[conditionsAsString.length];
        for (int i = 0; i < conditionsAsString.length; i++) {
            conditionOperands[i] = new OperandImpl(conditionsAsString[i]);
        }
    }

    public OperandImpl[] getConditionOperands() {
        return conditionOperands;
    }

    public void traverse(AbstractVisitor visitor) throws Exception {
        visitor.visitWhenNode(this);
    }

    public void process(AbstractProcessor processor) {
        processor.processWhenNode(this);
    }

    public boolean isIdenticalTo(AbstractNode comparedNode) {
        if (!(comparedNode instanceof WhenNode)) return false;

        /* Compare transitions */
        return transitions.isIdenticalTo(((WhenNode) comparedNode).getTransitions());
    }

}
