package base.hldd.structure.variables;

import base.Indices;
import base.Type;
import base.hldd.structure.Flags;

/**
 * @author Anton Chepurov
 */
public class PartedVariable extends Variable {
	private final Indices partedIndices;

	public PartedVariable(String varName, Type baseType, Indices partedIndices) {
		this(varName, baseType, partedIndices, new Flags());
	}

	public PartedVariable(String varName, Type baseType, Indices partedIndices, Flags flags) {
		super(varName, baseType.derivePartedType(partedIndices), flags);
		this.partedIndices = partedIndices;
	}

	public Indices getPartedIndices() {
		return partedIndices;
	}

	public boolean isIdenticalTo(AbstractVariable comparedAbsVariable) {
		/* Compare class */
		if (comparedAbsVariable.getClass() != this.getClass()) return false;
		/* Compare Indices */
		Indices compIndices = ((PartedVariable) comparedAbsVariable).getPartedIndices();
		if (!partedIndices.equals(compIndices)) return false;
		/* Compare the remaining parts */
		return super.isIdenticalTo(comparedAbsVariable);
	}

	public String getPureName() {
		return super.getName();
	}

	@Override
	public String getName() {
		return getPureName() + partedIndices.toString();
	}
}
