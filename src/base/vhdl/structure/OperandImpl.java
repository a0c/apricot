package base.vhdl.structure;

import base.Indices;
import base.HashCodeUtil;

/**
 * @author Anton Chepurov
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
		//noinspection SimplifiableIfStatement
		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		return isIdenticalTo((OperandImpl) obj);
	}

	public int hashCode() {
		int result = HashCodeUtil.SEED;
		result = HashCodeUtil.hash(result, name);
		result = HashCodeUtil.hash(result, partedIndices);
		return result;
	}

	public boolean isIdenticalTo(AbstractOperand comparedOperand) {
		if (!(comparedOperand instanceof OperandImpl)) return false;
		OperandImpl comparedOperandImpl = (OperandImpl) comparedOperand;

		/* Check OPERANDS */
		if (!name.equals(comparedOperandImpl.getName())) return false;

		/* Check PARTED_INDICES */
		if (!Indices.equals(partedIndices, comparedOperandImpl.partedIndices)) return false;

		/* Check IsInverted */
		return isInverted() == comparedOperandImpl.isInverted();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (isInverted()) sb.append("NOT ");
		sb.append(name);
		if (isParted()) sb.append(partedIndices);

		return sb.toString();
	}
}
