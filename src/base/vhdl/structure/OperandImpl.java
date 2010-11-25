package base.vhdl.structure;

import base.HashCodeUtil;
import base.Indices;
import base.Type;
import base.TypeResolver;
import base.hldd.structure.nodes.utils.Condition;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Chepurov
 */
public class OperandImpl extends AbstractOperand {

	private String name;

	private OperandImpl dynamicRange;
	/* todo: for multidimensional array replace dynamicRange with List<OperandImpl> ranges;
	* todo: first partedIndices will reside in partedIndices field of slices.get(0); parent partedIndices will be null;
	* todo: isParted() will become {return !slices.isEmpty()}.
	* todo: This is for values single values. Consider aggregations as well: 
	* todo: - There Map<Condition, OperandImpl> may be required
	* todo: - OTHERS may be inserted to Condition as a generic field. */

	//todo: rename partedIndices to slice; isParted() => isSlice()
	private Indices partedIndices;

	private Map<Condition, OperandImpl> arrayOperands;

	public OperandImpl(String name) {
		super(false);
		this.name = name;
	}

	public OperandImpl(String name, Indices partedIndices, boolean isInverted) {
		super(isInverted);
		this.name = name;
		this.partedIndices = partedIndices;
	}

	public OperandImpl(String name, OperandImpl dynamicRange, boolean isInverted) {
		super(isInverted);
		this.name = name;
		this.dynamicRange = dynamicRange;
	}

	public OperandImpl(Map<Condition, OperandImpl> arrayOperands) {
		super(false);
		this.arrayOperands = arrayOperands;
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

	public boolean isArray() {
		return arrayOperands != null;
	}

	public boolean isParted() {
		return partedIndices != null;
	}

	public boolean isWhole() {
		return !isParted() && !isDynamicRange();
	}

	public boolean isDynamicRange() {
		return dynamicRange != null;
	}

	public OperandImpl getDynamicRange() {
		return dynamicRange;
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
		result = HashCodeUtil.hash(result, dynamicRange);
		result = HashCodeUtil.hash(result, arrayOperands);
		return result;
	}

	public boolean isIdenticalTo(AbstractOperand comparedOperand) {
		if (!(comparedOperand instanceof OperandImpl)) return false;
		OperandImpl comparedOperandImpl = (OperandImpl) comparedOperand;

		/* Check OPERANDS */
		if (!name.equals(comparedOperandImpl.getName())) return false;

		/* Check PARTED_INDICES */
		if (!Indices.equals(partedIndices, comparedOperandImpl.partedIndices)) return false;

		if (isDynamicRange() ^ comparedOperandImpl.isDynamicRange()) return false;
		if (isDynamicRange()) {
			if (!dynamicRange.isIdenticalTo(comparedOperandImpl.dynamicRange)) return false;
		}

		if (isArray() ^ comparedOperandImpl.isArray()) return false;
		if (isArray()) {
			if (!arrayOperands.equals(comparedOperandImpl.arrayOperands)) return false;
		}
		/* Check IsInverted */
		return isInverted() == comparedOperandImpl.isInverted();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (isArray())
			return sb.append("ARRAY (").append(arrayOperands.size()).append(")").toString();

		if (isInverted()) sb.append("NOT ");
		sb.append(name);
		if (isParted()) sb.append(partedIndices);
		if (isDynamicRange()) sb.append("( ").append(dynamicRange).append(" )");

		return sb.toString();
	}

	public static String generateNameForDynamicRangeRead(OperandImpl operandImpl) {
		StringBuilder sb = new StringBuilder();
		sb.append(operandImpl.name).append("__(").append(operandImpl.dynamicRange).append(")");
		return sb.toString();
	}

	public boolean contains(OperandImpl thatOperand, TypeResolver typeResolver) {

		if (!name.equals(thatOperand.name)) return false;

		Indices thisRange = resolveRange(typeResolver);
		Indices thatRange = thatOperand.resolveRange(typeResolver);

		return thisRange.contain(thatRange);
	}

	public Indices resolveRange(TypeResolver typeResolver) {

		if (isParted()) {
			return partedIndices;
		}

		if (isDynamicRange()) {
			if (dynamicRange.isParted()) {
				return dynamicRange.getPartedIndices().deriveValueRange();
			} else {
				//todo: may overflow out of this type slice;
				Type dynamicRangeType = typeResolver.resolveType(dynamicRange.getName());
				return dynamicRangeType.resolveValueRange();
			}
		}

		Type thisType = typeResolver.resolveType(name);
		return thisType.getLength(); // whole variable length
	}

	public Iterable<Element> getElements() {
		List<Element> elements = new LinkedList<Element>();
		for (Map.Entry<Condition, OperandImpl> entry : arrayOperands.entrySet()) {
			elements.add(new Element(entry.getKey(), entry.getValue()));
		}
		return elements;
	}

	public class Element {
		public final Condition index;
		public final OperandImpl operand;

		public Element(Condition index, OperandImpl operand) {
			this.index = index;
			this.operand = operand;
		}
	}
}
