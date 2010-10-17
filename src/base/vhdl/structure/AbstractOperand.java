package base.vhdl.structure;

import base.Indices;

/**
 * Class represents an <tt>Abstract Operand</tt> for being extended by <tt>leaf
 * operands</tt> ({@link OperandImpl}) and <tt>composite operands</tt> (
 * {@link Expression} and {@link UserDefinedFunction}).
 * <p/>
 * According to Composite design pattern, the AbstractOperand class corresponds
 * to the <tt>Component</tt> object in the object structure. Class Operand is a
 * <tt>Leaf</tt> component. While classes Expression and UserDefinedFunction
 * are <tt>Composite</tt>.
 * <p/>
 * This abstract class represents both primitives and their containers.
 * <p> todo: to be removed
 * Operand <=> Expression (countConditions -> count isCondition -->)
 *
 * @author Anton Chepurov
 */
public abstract class AbstractOperand {

	private boolean isInverted;
	/**
	 * Length of the operand. Not <code>null</code>, if fixed. <code>null</code> if not fixed yet.
	 * <br> Used only for calculation of the lengths of Constant Variables.
	 */
	private Indices length;

	public AbstractOperand(boolean isInverted) {
		this.isInverted = isInverted;
	}

	public boolean isInverted() {
		return isInverted;
	}

	public void setInverted(boolean inverted) {
		isInverted = inverted;
	}

	public Indices getLength() {
		return length;
	}

	public void setLength(Indices length) {
		this.length = length;
	}

	/**
	 * @return by default <code>null</code> is returned. Subclasses should
	 * 		   override the method to behave differently.
	 * 		   {@link Expression} and {@link UserDefinedFunction} utilize
	 * 		   the default behaviour.
	 */
	public Indices getPartedIndices() {
		return null;
	}

	/**
	 * @return by default <code>false</code> is returned. Subclasses should
	 * 		   override the method to behave differently.
	 * 		   {@link Expression} and {@link UserDefinedFunction} utilize
	 * 		   the default behaviour.
	 */
	public boolean isParted() {
		return false;
	}

	public abstract boolean isIdenticalTo(AbstractOperand comparedOperand);

	public abstract String toString();

}
