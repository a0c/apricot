package base.hldd.structure.models.utils;

import base.HashCodeUtil;
import base.Range;
import base.hldd.structure.variables.AbstractVariable;

/**
 * Class is used to pass both a variable and its range to and fro.
 *
 * @author Anton Chepurov
 */
public class RangeVariableHolder {
	public static final int NO_TRUE_VALUE = Integer.MIN_VALUE;

	private AbstractVariable variable;
	private Range range;
	private final int trueValue;

	public RangeVariableHolder(AbstractVariable variable, Range range, int trueValue) {
		this.variable = variable;
		this.range = range;
		this.trueValue = trueValue;
	}

	public RangeVariableHolder(AbstractVariable variable, Range range) {
		this(variable, range, NO_TRUE_VALUE); //todo: instead of NO_TRUE_VALUE extend RangeVariableHolder class ...
	}

	public AbstractVariable getVariable() {
		return variable;
	}

	public Range getRange() {
		return range;
	}

	public int getTrueValue() {
		return trueValue;
	}

	public boolean isInverted() {
		return trueValue == 0;
	}

	public boolean isRange() {
		return range != null;
	}

	public boolean isIdenticalTo(RangeVariableHolder comparedHolder) {
		/* Compare Variables */
		if (!variable.isIdenticalTo(comparedHolder.variable)) return false;
		/* Compare Ranges */
		return Range.equals(range, comparedHolder.range);
	}

	public void setVariable(AbstractVariable variable) {
		this.variable = variable;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public String toString() {
		return variable + Range.toString(range);
	}

	@Override
	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, variable);
		result = HashCodeUtil.hash(result, range);
		result = HashCodeUtil.hash(result, trueValue);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		RangeVariableHolder thatHolder = (RangeVariableHolder) obj;

		if (variable != thatHolder.variable) return false;

		if (range == null ^ thatHolder.range == null) return false;

		//noinspection SimplifiableIfStatement
		if (range != null && !range.equals(thatHolder.range)) {
			return false;
		}

		return trueValue == thatHolder.trueValue;
	}
}
