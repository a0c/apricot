package base.hldd.structure.variables;

import base.Indices;
import base.Type;
import base.hldd.structure.Flags;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 13.10.2008
 * <br>Time: 23:27:20
 */
public class PartedVariable extends Variable {
    private final Indices partedIndices;

    public PartedVariable(String varName, Type baseType, Indices partedIndices) {
//        super(varName, partedIndices.highestSB(), new Flags());
        super(varName, baseType.derivePartedType(partedIndices), new Flags());
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

    public String toString() {
        return "VAR#\t" + index + ":  " + flagsToString() + "\t\"" + getUniqueName() + "\"\t" + lengthToString();
    }

    public String getUniqueName() {
        return name + partedIndices.toString();
    }
}
