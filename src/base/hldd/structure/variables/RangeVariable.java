package base.hldd.structure.variables;

import base.Range;
import base.Type;
import base.hldd.structure.Flags;

/**
 * @author Anton Chepurov
 */
public class RangeVariable extends Variable {
	
	private final Range range;

	public RangeVariable(String varName, Type baseType, Range range) {
		this(varName, baseType, range, new Flags());
	}

	public RangeVariable(String varName, Type baseType, Range range, Flags flags) {
		super(varName, baseType.deriveRangeType(range), flags);
		this.range = range;
	}

	public Range getRange() {
		return range;
	}

	public boolean isIdenticalTo(AbstractVariable comparedAbsVariable) {
		/* Compare class */
		if (comparedAbsVariable.getClass() != this.getClass()) return false;
		/* Compare Range */
		Range compRange = ((RangeVariable) comparedAbsVariable).getRange();
		if (!range.equals(compRange)) return false;
		/* Compare the remaining parts */
		return super.isIdenticalTo(comparedAbsVariable);
	}

	public String getPureName() {
		return super.getName();
	}

	@Override
	public String getName() {
		return getPureName() + range.toString();
	}
}
