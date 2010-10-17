package base.hldd.structure.variables;

import base.hldd.structure.Flags;
import base.Indices;
import base.Type;

/**
 * Class represents a variable as it is defined in AGM.
 *
 * @author Anton Chepurov
 */
public class Variable extends AbstractVariable {

	/**
	 * Variable NAME
	 */
	private String name = "";
	/**
	 * Variable INDEX.
	 * set index to '-1'. Needed for indexation (just to avoid indexing a variable twice)
	 */
	protected int index = -1;
	/**
	 * Variable TYPE.
	 * Stores the length of the variable and other minor stuff.
	 */
	protected Type type = null;
	/**
	 * Variable FLAGS
	 */
	private Flags flags = new Flags();

	/**
	 * Constructor for OVERRIDING in inherited classes (ConstantVariable)
	 */
	protected Variable() {
	}

	public Variable(String varName, Type type, Flags flags) {
		this.name = varName;
		this.type = type;
		this.flags = flags;
	}

	public Variable(String varName, Type type) {
		this(varName, type, new Flags());
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
		/* for FSM Control GraphVariable return "" */
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
		return super.getName() + name;
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
		if (type == null)
			throw new RuntimeException("Trying to ask for isSigned() on FSM variable (it does not have type) or other variable without type");
		return type.isSigned();
	}

	public Indices getLength() {
		if (type == null)
			throw new RuntimeException("Trying to ask for getLength() on FSM variable (it does not have type) or other variable without type");
		return type.getLength();
	}

	public void setLength(Indices length) {
		if (type == null) {
			if (isFSM()) {
				throw new RuntimeException("Trying to set length to FSM variable (it does not have length and does not have type)");
			}
			throw new RuntimeException("Trying to set length to variable without type");
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

	public void setConstant(boolean isConstant) {
		flags.setConstant(isConstant);
	}

	public void setFunction(boolean isFunction) {
		flags.setFunction(isFunction);
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

	/* Setters END */

}
