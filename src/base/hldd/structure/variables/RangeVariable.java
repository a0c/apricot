package base.hldd.structure.variables;

import base.Indices;
import base.Type;
import base.hldd.structure.Flags;

/**
 * @author Anton Chepurov
 */
public class RangeVariable extends Variable {
	private final Indices range;

	public RangeVariable(String varName, Type baseType, Indices range) {
		this(varName, baseType, range, new Flags());
	}

	public RangeVariable(String varName, Type baseType, Indices range, Flags flags) {
		super(varName, baseType.deriveRangeType(range), flags);
		this.range = range;
	}

	public Indices getRange() {
		return range;
	}

	public boolean isIdenticalTo(AbstractVariable comparedAbsVariable) {
		/* Compare class */
		if (comparedAbsVariable.getClass() != this.getClass()) return false;
		/* Compare Indices */
		Indices compIndices = ((RangeVariable) comparedAbsVariable).getRange();
		if (!range.equals(compIndices)) return false;
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
