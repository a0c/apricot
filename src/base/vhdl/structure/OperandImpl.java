package base.vhdl.structure;

import base.Indices;
import base.HashCodeUtil;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 26.02.2008
 * <br>Time: 11:31:21
 */
public class OperandImpl extends AbstractOperand {

    private String name;
    private Indices partedIndices;

    public OperandImpl(String name) {
        super(false);
        this.name = name;
    }

    public OperandImpl(String name, Indices partedIndices, boolean isInverted) {
        super(isInverted);
        this.name = name;
        this.partedIndices = partedIndices;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Indices getPartedIndices() {
        return partedIndices;
    }

    public boolean isParted() {
        return partedIndices != null;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ((obj == null) || (obj.getClass() != this.getClass())) return false;
        return isIdenticalTo((OperandImpl) obj);
    }

    public int hashCode() {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash(result, name);
        result = HashCodeUtil.hash(result, partedIndices);
        return result;
    }

    public boolean isIdenticalTo(AbstractOperand comparedAbstrOperand) {
        if (!(comparedAbstrOperand instanceof OperandImpl)) return false;
        OperandImpl comparedOperand = (OperandImpl) comparedAbstrOperand;

        /* Check OPERANDS */
        if (!name.equals(comparedOperand.getName())) return false;

        /* Check PARTED_INDICES */
        if (!Indices.equals(partedIndices, comparedOperand.partedIndices)) return false;

        /* Check IsInverted */
        return isInverted() == comparedOperand.isInverted();
    }

    public String toString() {
        StringBuilder b = new StringBuilder();

        if (isInverted()) b.append("NOT ");
        b.append(name);
        if (isParted()) b.append(partedIndices);

        return b.toString();
    }
}
