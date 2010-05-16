package base.vhdl.structure.nodes;

import base.vhdl.visitors.AbstractVisitor;
import base.vhdl.structure.Transition;
import base.vhdl.structure.AbstractOperand;
import base.vhdl.structure.OperandImpl;
import base.vhdl.processors.AbstractProcessor;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.PartedVariable;
import base.Indices;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 22:47:05
 */
public class TransitionNode extends AbstractNode {

    private Transition transition;

    public TransitionNode(Transition transition) {
        this.transition = transition;
    }

    /**
     * Use {@link #getTargetOperand()} instead
     */
    @Deprecated
    public String getVariableName() {
        return getTargetOperand().getName();
    }

    public boolean isNull() {
        return transition.isNull();
    }

    public OperandImpl getTargetOperand() {
        return transition.getTargetOperand();
    }

    public AbstractOperand getValueOperand() {
        return transition.getValueOperand();
    }

    /**
     * @return <code>null</code> if it is a <code>Null-transition</code>
     *         or if valueOperand is not parted. Parted indices, if the
     *         valueOperand is parted.
     */
    public Indices getValueOperandPartedIndices() {
        return isNull() ? null : getValueOperand().getPartedIndices();
    }

    public void traverse(AbstractVisitor visitor) throws Exception {
        visitor.visitTransitionNode(this);
    }

    public void process(AbstractProcessor processor) throws Exception {
        processor.processTransitionNode(this);
    }

    /**
     * TransitionNode is transition of the specified variable if:
     * <br>- Node is <code>Null-transition</code> node (can only occur in Beh
     *       DD trees).
     * <br>  (<b>NB!</b> For the rest of the cases node is supposed to be a
     *       non-<code>Null-transition</code>)
     * <br>- Node's variableOperand has the same name as Variable and one of
     *       the following holds:
     * <br>- - none of them is parted;
     * <br>- - Variable is parted and node's variableOperand is either not
     *       parted or has parted indices that contain the parted indices of
     *       the Variable;
     *
     * @param variable checked variable
     * @param isNullATransition whether to treat null-transition as a transition of the specified variable
     * @return <code>true</code> if this Transition Node sets the specified
     *         variable. <code>false</code> otherwise.
     */
    public boolean isTransitionOf(AbstractVariable variable, boolean isNullATransition) {

        if (!isNullATransition && isNull()) {
            return false;
        }

        return isNull()
                || (getTargetOperand().getName().equals(variable.getPureName())
                    && (!getTargetOperand().isParted() && variable.getClass() != PartedVariable.class
                    || (variable.getClass() == PartedVariable.class
                        && (!getTargetOperand().isParted()
                        || getTargetOperand().getPartedIndices().contain(((PartedVariable) variable).getPartedIndices())))));
    }

    public boolean isIdenticalTo(AbstractNode comparedNode) {
        return comparedNode instanceof TransitionNode && transition.isIdenticalTo(((TransitionNode) comparedNode).transition);

    }

    public String toString() {
        return transition.toString();
    }

}
