package base.vhdl.structure.utils;

import base.Indices;
import base.Type;
import base.TypeResolver;
import base.vhdl.structure.OperandImpl;

import java.util.*;

/**
 * @author Anton Chepurov
 */
public class SplittableOperandStorage extends OperandStorage {

	/**
	 * Splits range operands into non-overlaping regions
	 *
	 * @param typeResolver where to obtain types from
	 */
	public void splitOverlaps(TypeResolver typeResolver) {

		for (Map.Entry<String, Set<OperandImpl>> entry : operandsByName.entrySet()) {

			String varName = entry.getKey();
			Set<OperandImpl> oldRanges = entry.getValue();

			Set<OperandImpl> newRanges = new HashSet<OperandImpl>();

			flattenDynamicRangesToBits(oldRanges, varName, typeResolver);

			/* Use 2 SortedSets: a separate one for START and END markers.
			* 1) Place START and END markers into 2 SortedSets according to the following:
			*      a) Starting index -> "i" for START and "i - 1" for END
			*      b) Ending index   -> "i" for END and "i + 1" for START
			* 2) Create intervals: take next marker from either SortedSet. Number of markers in both sets is equal.
			* */
			SortedSet<Integer> startsSet = new TreeSet<Integer>();
			SortedSet<Integer> endsSet = new TreeSet<Integer>();
			int lowerBound = getLowest(oldRanges);
			int upperBound = getHighest(oldRanges);
			/* Fill SortedSets */
			for (OperandImpl sliceOperand : oldRanges) {
				Indices slice = sliceOperand.getPartedIndices();
				/* Starting index */
				int start = slice.getLowest();
				startsSet.add(start);
				if (start - 1 >= lowerBound) {
					endsSet.add(start - 1);
				}
				/* Ending index */
				int end = slice.getHighest();
				endsSet.add(end);
				if (end + 1 <= upperBound) {
					startsSet.add(end + 1);
				}
			}
			/* Check number of markers */
			if (startsSet.size() != endsSet.size()) throw new RuntimeException("Internal error: " +
					"cannot split overlapping ranges:\nAmounts of START and END markers are different.");
			/* Create intervals */
			ArrayList<Integer> startsArray = new ArrayList<Integer>(startsSet);
			ArrayList<Integer> endsArray = new ArrayList<Integer>(endsSet);
			for (int i = 0; i < startsArray.size(); i++) {
				newRanges.add(new OperandImpl(varName, new Indices(endsArray.get(i), startsArray.get(i)), false));
			}

			fillMissingRanges(newRanges, varName, typeResolver);

			/* Replace the variable set in partialSettingsMap with the new one */
			operandsByName.put(varName, newRanges);
		}

	}

	private void flattenDynamicRangesToBits(Set<OperandImpl> operands, String varName, TypeResolver typeResolver) {

		int wholeLength = typeResolver.resolveType(varName).getLength().length();

		for (OperandImpl operand : operands) {

			if (!operand.isDynamicRange()) {
				continue;
			}

			Type type = typeResolver.resolveType(operand.getDynamicRange().getName());

			int length = type.countPossibleValues(operand.getPartedIndices(), wholeLength);

			operands.remove(operand);
			for (int index = 0; index < length; index++) {
				operands.add(new OperandImpl(varName, new Indices(index, index), false));
			}
		}
	}

	private static int getLowest(Set<OperandImpl> operands) {
		int min = Integer.MAX_VALUE;
		for (OperandImpl operand : operands) {
			int lowest = operand.getPartedIndices().getLowest();
			if (lowest < min) {
				min = lowest;
			}
		}
		if (min == Integer.MAX_VALUE) {
			throw new RuntimeException("Possibly failed to calculate lowerBound while splitting overlapping ranges" +
					": Lower bound = Integer.MAX_VALUE");
		}
		return min;
	}

	private static int getHighest(Set<OperandImpl> operands) {
		int max = -1;
		for (OperandImpl operand : operands) {
			int highest = operand.getPartedIndices().getHighest();
			if (highest > max) {
				max = highest;
			}
		}
		if (max == -1) {
			throw new RuntimeException("Possibly failed to calculate upperBound while splitting overlapping ranges" +
					": Upper bound = -1");
		}
		return max;
	}

	private void fillMissingRanges(Set<OperandImpl> operands, String varName, TypeResolver typeResolver) {

		int wholeLength = typeResolver.resolveType(varName).getLength().length();

		boolean[] bits = new boolean[wholeLength];
		/* Check intersections: if any, inform about them */
		for (OperandImpl operand : operands) {
			if (!operand.isParted())
				throw new RuntimeException("Range assignment operand doesn't contain range: " + operand);
			Indices range = operand.getPartedIndices();
			for (int index = range.getLowest(); index <= range.getHighest(); index++) {
				/* If this bit has already been set, inform about intersection */
				if (bits[index]) throw new RuntimeException("Intersection of range assignment operands:" +
						"\nBit " + index + " has already been set for operand " + operand.getName());
				bits[index] = true;
			}
		}
		/* Check missing partial setting variables: if any, fill the set with missing variables */
		Collection<Indices> unsetIndicesCollect = extractUnsetIndices(bits);
		for (Indices unsetIndices : unsetIndicesCollect) { //todo: It may occur, that the whole variable is unset. Consider this.
			operands.add(new OperandImpl(varName, unsetIndices, false));
		}
	}

	static Collection<Indices> extractUnsetIndices(boolean[] setBits) {
		List<Indices> indicesList = new LinkedList<Indices>();
		int lowest = -1, highest = -1;
		for (int i = 0; i < setBits.length; i++) {
			if (!setBits[i]) {
				if (lowest == -1) {
					lowest = i;
				}
				highest = i;
			} else {
				if (lowest != -1) {
					indicesList.add(new Indices(highest, lowest));
					lowest = -1;
					highest = -1;
				}
			}
		}
		if (lowest != -1) {
			indicesList.add(new Indices(highest, lowest));
		}
		return indicesList;
	}
}
