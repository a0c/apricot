package base.hldd.structure.variables;

import base.hldd.structure.Flags;
import base.Indices;
import base.Type;

/**
 * Class represents a variable as it is defined in AGM.<br>
 *
 * <p> User: Anton Chepurov
 * <p>Date: 10.02.2007
 * <p>Time: 21:55:25
 */
public class Variable extends AbstractVariable {

    /**
     * Variable NAME
     */
    protected String name;
    /**
     * Variable INDEX.
     * set index to '-1'. Needed for indexation (just to avoid indexing a variable twice)
     */
    protected int index = -1;
    /**
     * Variable TYPE.
     * Stores the length of the variable and other minor stuff.
     */
    protected Type type = null; //-1;
    /**
     * Variable FLAGS
     */
    private Flags flags;

    /**
     * Constructor for OVERRIDING in inherited classes (ConstantVariable)
     */
    protected Variable() {
        // set index to '-1'. Needed for indexation (just to avoid indexing a variable twice)
        flags = new Flags();
    }

    public Variable(String varName, Type type, Flags flags) {
        this.name = varName;
        this.type = type;
        this.flags = flags;
    }

    public String toString() {
        return "VAR#\t" + index + ":  " + flagsToString() + "\t\"" + getName() + "\"\t" + lengthToString();
    }

    public boolean isIdenticalTo(AbstractVariable comparedAbsVariable) {
        /* Compare links */
        if (this == comparedAbsVariable) return true;
        /* Compare classes */
        if (comparedAbsVariable.getClass() != Variable.class) return false;
        Variable comparedVariable = (Variable) comparedAbsVariable;
        
        /* Compare NAMES */
        if (!name.equals(comparedVariable.name)) return false;
        /* Compare TYPE */
        if (type == null ^ comparedVariable.type == null) return false;
        if (type != null) {
            if (!type.equals(comparedVariable.type)) return false;
        }

        /* All tests passed. Variables are identical */
        return true;
    }

    public String lengthToString() {
        return type == null ? "" : type.lengthToString(); // "<" + highestSB + ":0>";
    }

    protected String flagsToString() {
        return "(" + flags + ")";
    }

    public boolean isIndexSet() {
        return this.index != -1;
    }

    /* Getters START */

    public String getName() {
        return name;
    }

    public String getPureName() {
        return getName();
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public boolean isInput() {
        return flags.isInput();
    }

    public boolean isOutput() {
        return flags.isOutput();
    }

    public boolean isReset() {
        return flags.isReset();
    }

    public boolean isState() {
        return flags.isState();
    }

    public boolean isCout() {
        return flags.isCout();
    }

    public boolean isFSM() {
        return flags.isFSM();
    }

    /**
     * VHDL signed variable status. Used to create signed/unsigned functions.
     */
    public boolean isSigned() {
        if (type == null) throw new RuntimeException("Trying to ask for isSigned() on FSM variable (it does not have type) or other variable without type");
        return type.isSigned();
    }

    public Indices getLength() {
        if (type == null) throw new RuntimeException("Trying to ask for getLength() on FSM variable (it does not have type) or other variable without type");
        return type.getLength();
    }

    public void setLength(Indices length) {
        if (type == null) {
            if (isFSM()) {
                throw new RuntimeException("Trying to set length to FSM variable (it does not have length and does not have type)");
            }
            throw new RuntimeException("Trying to set length to variable without type");
//            type = new Type(length); return;
        }
        type.setLength(length);
    }

    /* Getters END */

    /* Setters START */

    public void setIndex(int index) {

        if (!isIndexSet()) this.index = index;
        
    }

    /*todo: May be rename this method to "setIndex" and remove the real setIndex method above... */
    public void forceSetIndex(int index) {

        this.index = index;

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConstant(boolean isConstant) {
        flags.setConstant(isConstant);
    }

    public void setCout(boolean isCout) {
        flags.setCout(isCout);
    }

    public void setFunction(boolean isFunction) {
        flags.setFunction(isFunction);
    }

    public void setState(boolean isState) {
        flags.setState(isState);
    }

    public void setOutput(boolean isOutput) {
        flags.setOutput(isOutput);
    }

    public void setInput(boolean isInput) {
        flags.setInput(isInput);
    }

    public void setDelay(boolean isDelay) {
        flags.setDelay(isDelay);
    }

    public boolean isDelay() {
        return flags.isDelay();
    }

	public boolean isExpansion() {
		return flags.isExpansion();
	}

	public Flags getFlags() {
        return flags;
    }

    /* Setters END */

}
