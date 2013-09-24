package base.hldd.structure.variables;

import base.Range;
import base.SourceLocation;
import base.Type;
import base.hldd.structure.models.utils.RangeVariableHolder;
import base.vhdl.structure.Operator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class represents a FUNCTION VARIABLE as it is defined in AGM.<br>
 *
 * @author Anton Chepurov
 */
public class FunctionVariable extends Variable {

	private static final int INITIAL_OPERAND_COUNT = 5;

	/**
	 * TYPE of the function
	 */
	private Operator operator;
	/**
	 * Operands
	 */
	protected List<RangeVariableHolder> operands;
	/**
	 * Index of function (denotes order)
	 */
	private int nameIdx;

	private SourceLocation source;

	private String arrayIdxs = "";

	/**
	 * Constructor to override in inherited classes
	 */
	protected FunctionVariable() {
		setFunction(true);
	}

	/**
	 * General purpose constructor. Only receives <code>operator</code> and
	 * <code>nameIdx</code> as parameters.
	 * <p/>
	 * Operands are filled using {@link #addOperand(AbstractVariable, Range)}
	 * method. Each <code>operator</code> knows the number of supported
	 * operands. So an Exception is thrown if this number gets exceeded when
	 * calling {@link #addOperand(AbstractVariable, Range)}.
	 *
	 * @param operator operator (from the set of supported ones)
	 * @param nameIdx  name index of the variable
	 */
	public FunctionVariable(Operator operator, int nameIdx) {
		this(nameIdx);
		this.operator = operator;
		operands = new ArrayList<RangeVariableHolder>(INITIAL_OPERAND_COUNT);
		if (operator == Operator.ARRAY) {
			setMemory(true);
		}
	}

	protected FunctionVariable(int nameIdx) {
		this();
		this.nameIdx = nameIdx;
	}

	public String toString() {
		return super.toString() + arrayIdxs +
				"\nFUN#\t" + operatorToString() + "\t(" + operandsToString() + ")";
	}

	public void setArrayIdxs(Type arrayType) {
		Range length = arrayType.getLength();
		arrayIdxs = String.format(" [%d-%d]", length.getHighest(), length.getLowest());
	}

	@Override
	public String getName() {
		if (operator == Operator.ARRAY) {
			return super.getName();
		}
		return super.getName() + operatorToString() + "____" + nameIdx;
	}

	protected String operatorToString() {
		return operator.toString();
	}

	private String operandsToString() {
		StringBuilder sb = new StringBuilder();
		int i = 1;
		String delim = operands.size() > 2 ? " " : "\t";
		for (RangeVariableHolder operand : operands) {
			sb.append("A").append(i++).append("<= ");
			String rangeAsString = operand.isRange()
					? operand.getRange().toStringAngular(false)
					: operand.getVariable().lengthToString();
			sb.append(operand.getVariable().getIndex()).append(rangeAsString);
			sb.append(",").append(delim);
		}
		sb.delete(sb.lastIndexOf(","), sb.length());
		return sb.toString();
	}

	/**
	 * In order to use this method, after creation of a new FunctionVariable always search for an existent identical FunctionVariable
	 *
	 * @param comparedAbsVariable variable to be compared with
	 * @return <code>true</code> if the fuctions are identical
	 */
	public boolean isIdenticalTo(AbstractVariable comparedAbsVariable) {
		/* Compare links */
		if (this == comparedAbsVariable) return true;
		/* Compare classes */
		if (getClass() != comparedAbsVariable.getClass()) return false;
		FunctionVariable comparedVar = (FunctionVariable) comparedAbsVariable;

		/* Compare OPERATORS */
		if (operator != comparedVar.operator) return false;

		/* Compare OPERANDS */
		if (operands == null ^ comparedVar.operands == null) return false;
		if (operands == null) {
			/* Both are null */
			return true;
		} else {
			/* Both are not null */
			/* Compare NUMBER OF OPERANDS */
			if (operands.size() != comparedVar.operands.size()) return false;
			/* Compare EACH OPERAND */
			for (int i = 0; i < operands.size(); i++) {
				if (!operands.get(i).isIdenticalTo(comparedVar.operands.get(i))) return false;
			}
		}

		/* All checks done */
		return true;
	}

	/**
	 * @param operandVariable operand to add
	 * @param range		   range of the operand to add
	 */
	public void addOperand(AbstractVariable operandVariable, Range range) {
		if (!isValidAddition()) throw new RuntimeException("Cannot add operand to Function." +
				"\nOperator " + operator + " supports only " + operator.getNumberOfOperands() + " operand(s).");
		operands.add(new RangeVariableHolder(operandVariable, range));
		/* Update highestSB */
		type = adjustType(type, operandVariable.getType(), range);
	}

	public boolean isValidAddition() {
		return operands.size() < operator.getNumberOfOperands();
	}

	protected Type adjustType(Type currentType, Type addedType, Range addedRange) {
		if (operator == Operator.EXP) {
			if (operands.size() != 2) {
				return null;
			}
			throw new RuntimeException("Don't know how to adjust type for EXPONENT function (while adding operand to function)");
		}
		if (operator == Operator.ARRAY) {
			if (currentType != null) {
				return currentType;
			}
			return addedType.getTargetElementType();
		}
		/* Update highestSB */
		Range addedLength = addedType.getLength();
		if (currentType == null) {
			Range adjustedLength = operator.adjustLength(null, addedLength, addedRange);
			currentType = new Type(adjustedLength);
		} else {
			Range adjustedLength = operator.adjustLength(currentType.getLength(), addedLength, addedRange);
			currentType.setLength(adjustedLength);
		}
		return currentType;
	}

	/* Getters START */

	public Operator getOperator() {
		return operator;
	}

	public List<RangeVariableHolder> getOperands() {
		return operands;
	}

	public int getNameIdx() {
		return nameIdx;
	}

	/* Getters END */

	/* Setters START */

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public void setOperand(int index, RangeVariableHolder operandHolder) {
		operands.set(index, operandHolder);
	}

	public void setNameIdx(int nameIdx) {
		this.nameIdx = nameIdx;
	}

	/* Setters END */

	public static Comparator<FunctionVariable> getComparator() {
		return new FunctionsComparator();
	}

	public void setSource(SourceLocation source) {
		this.source = source;
	}

	public SourceLocation getSource() {
		return source;
	}

	public void addSource(SourceLocation source) {
		if (this.source == null) {
			this.source = source;
			return;
		}
		setSource(this.source.addSource(source));
	}

	//todo: useless comparator, consider removing it. See the difference with and without it.

	public static class FunctionsComparator implements Comparator<FunctionVariable> {

		@Override
		public int compare(FunctionVariable o1, FunctionVariable o2) {

			int compNames = o1.operatorToString().compareTo(o2.operatorToString());

			if (compNames != 0) {
				return compNames < 0 ? -1 : 1;
			}

			return new Integer(o1.nameIdx).compareTo(o2.nameIdx);
		}
	}
}
