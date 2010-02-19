package base.vhdl.structure;

/**
 * <p>User: Anton Chepurov
 * <br>Date: 22.09.2007
 * <br>Time: 22:09:44
 */
public class Transition {

    //todo: when introducing partedIndices for target field, update the following:
    //todo: ConstantLengthAdjuster --> visitTransitionNode --> new OperandImpl(absGraphVariableName)
    private OperandImpl targetOperand;
    private AbstractOperand valueOperand;
    private boolean isNull;


    public Transition(OperandImpl targetOperand, AbstractOperand valueOperand) {
        this.targetOperand = targetOperand;
        this.valueOperand = valueOperand;
        isNull = false;
    }

    /**
     * Constructor for NULL transition.
     */
    public Transition() {
        isNull = true;
    }

    public String toString() {
        return isNull ? "NULL;" : targetOperand + " <= " + valueOperand + ";";
    }

    /**
     * The following Transitions are identical:<br>
     * - <code>Null-transition</code> and either <code>Null-transition</code> or todo... 
     * @param comparedTransition transition to compare with
     * @return <code>true</code> if transitions are identical. <code>false</code> otherwise.
     */
    public boolean isIdenticalTo(Transition comparedTransition) {

        /* Check whether exactly one of them is null.
        * Return false, since 'NULL' can only occur in Beh RTL DD sources,
        * where NULL is used to retain the value (i.e. instead of "reg <= reg" "null" is used) */
        if (isNull ^ comparedTransition.isNull) return false;

        /* Here both are either null, or not null */
        if (isNull) {
            return true;
        } else {
            if (!targetOperand.isIdenticalTo(comparedTransition.targetOperand)) return false;

            if (!valueOperand.isIdenticalTo(comparedTransition.valueOperand)) return false;
        }

        return true;
    }

    /* Getters START */

    public OperandImpl getTargetOperand() {
        return targetOperand;
    }

    public AbstractOperand getValueOperand() {
        return valueOperand;
    }

    public boolean isNull() {
        return isNull;
    }

    /* Getters END */

}
