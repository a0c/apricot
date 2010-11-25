package base.vhdl.structure.utils;

import base.Indices;
import base.TypeResolver;
import base.vhdl.structure.OperandImpl;

import java.util.*;

/**
 * @author Anton Chepurov
 */
public class OperandStorage {

	protected final Map<String, Set<OperandImpl>> operandsByName = new HashMap<String, Set<OperandImpl>>();

	public void store(String variableName, OperandImpl operand) {
		obtainOperandsFor(variableName).add(operand);
	}

	public void store(OperandImpl operand) {
		store(operand.getName(), operand);
	}

	public void storeAll(OperandStorage operandStorage) {

		for (String thatVariableName : operandStorage.operandsByName.keySet()) {

			Set<OperandImpl> thisOperands = this.obtainOperandsFor(thatVariableName);

			thisOperands.addAll(operandStorage.operandsByName.get(thatVariableName));
		}
	}

	private Set<OperandImpl> obtainOperandsFor(String variableName) {
		Set<OperandImpl> operands = operandsByName.get(variableName);
		if (operands == null) {
			operands = new HashSet<OperandImpl>();
			operandsByName.put(variableName, operands);
		}
		return operands;
	}

	public boolean isWholeRangeSet(String variableName, TypeResolver typeResolver) {

		Indices wholeRange = typeResolver.resolveType(variableName).getLength();

		return isRangeSet(wholeRange, variableName, typeResolver);
	}

	private boolean isRangeSet(Indices range, String variableName, TypeResolver typeResolver) {
		Set<Integer> bitsToBeSet = new HashSet<Integer>(range.length());
		for (int bit = range.getLowest(); bit <= range.getHighest(); bit++) {
			bitsToBeSet.add(bit);
		}
		for (OperandImpl operand : obtainOperandsFor(variableName)) {
			if (operand.isWhole()) {
				return true;
			} else {
				Indices opRange = operand.resolveRange(typeResolver);
				for (int i = opRange.getLowest(); i <= opRange.getHighest(); i++) {
					bitsToBeSet.remove(i);
				}
			}
		}
		return bitsToBeSet.isEmpty();
	}

	public boolean contains(OperandImpl operand, TypeResolver typeResolver) {

		Indices range = operand.resolveRange(typeResolver);

		return isRangeSet(range, operand.getName(), typeResolver);
	}

	public Iterable<Item> getItems() {

		List<Item> items = new LinkedList<Item>();

		for (Map.Entry<String, Set<OperandImpl>> entry : operandsByName.entrySet()) {
			items.add(new Item(entry.getKey(), entry.getValue()));
		}

		return items;
	}

	public class Item {
		public final String name;
		public final Set<OperandImpl> operands;

		public Item(String name, Set<OperandImpl> operands) {
			this.name = name;
			this.operands = operands;
		}
	}
}
