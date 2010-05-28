package base.hldd.structure.models.utils;

import base.HashCodeUtil;
import base.hldd.structure.variables.AbstractVariable;
import base.Indices;

/**
 * Class is used to pass both a variable and its parted indices to and fro.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.10.2008
 * <br>Time: 15:28:45
 */
public class PartedVariableHolder {
    public static final int NO_TRUE_VALUE = Integer.MIN_VALUE;
    
    private AbstractVariable variable;
    private final Indices partedIndices;
    private final int trueValue;

    public PartedVariableHolder(AbstractVariable variable, Indices partedIndices, int trueValue) {
        this.variable = variable;
        this.partedIndices = partedIndices;
        this.trueValue = trueValue;
    }

    public PartedVariableHolder(AbstractVariable variable, Indices partedIndices) {
        this(variable, partedIndices, NO_TRUE_VALUE); //todo: instead of NO_TRUE_VALUE extend PartedVariableHolder class ...
    }

    public AbstractVariable getVariable() {
        return variable;
    }

    public Indices getPartedIndices() {
        return partedIndices;
    }

    public int getTrueValue() {
        return trueValue;
    }

    public boolean isInversed() {
        return trueValue == 0;
    }

    public boolean isIdenticalTo(PartedVariableHolder comparedHolder){
        /* Compare Variables */
        if (!variable.isIdenticalTo(comparedHolder.variable)) return false;
        /* Compare Parted Indices */
        return Indices.equals(partedIndices, comparedHolder.partedIndices);
    }

    public void setVariable(AbstractVariable variable) {
        this.variable = variable;
    }

    public String toString() {
        return variable + Indices.toString(partedIndices);
    }

	@Override
	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, variable);
		result = HashCodeUtil.hash(result, partedIndices);
		result = HashCodeUtil.hash(result, trueValue);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		PartedVariableHolder thatHolder = (PartedVariableHolder) obj;

		if (variable != thatHolder.variable) return false;

		if (partedIndices == null ^ thatHolder.partedIndices == null) return false;

		if (partedIndices != null && !partedIndices.equals(thatHolder.partedIndices)) {
			return false;
		}

		return  trueValue == thatHolder.trueValue;
	}
}
