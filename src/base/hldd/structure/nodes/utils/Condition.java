package base.hldd.structure.nodes.utils;

import base.HLDDException;
import base.HashCodeUtil;

import java.util.*;

/**
 * @author Anton Chepurov
 */
public final class Condition implements Comparable<Condition> {
	/* Single value */
	private int value = -1;
//	/* Enum */
//	private int enumOrd = -1;
	/* Array */
	private Condition[] arrayConditions = null;

	private Condition(int[] values) {

		arrayConditions = new Condition[values.length];

		for (int i = 0; i < values.length; i++) {
			arrayConditions[i] = createCondition(values[i]);
		}

		Arrays.sort(arrayConditions);
	}

	private Condition(int value) {
		this.value = value;
	}

	public int getValue() throws HLDDException {
		if (isArray()) {
			throw new HLDDException("Condition: getValue(): scalar value expected, found array: " + toString());
		} //todo: enum
		return value;
	}

	public int getElementsCount() {
		return arrayConditions.length;
	}

	public Condition getElementAt(int idx) throws HLDDException {
		if (!isArray()) {
			throw new HLDDException("Condition: getElementAt(): array expected, found scalar: " + toString());
		}
		checkBoundaries(idx, 0, getElementsCount() - 1, "Condition: getElementAt(): array index out of bounds: ");

		return arrayConditions[idx];
	}

	public int getValueAt(int idx) throws HLDDException {
		return getElementAt(idx).getValue();
	}

	public boolean isArray() {
		return arrayConditions != null;
	}

	private int getLeft() {
		return isArray() ? arrayConditions[0].value : value;
	}

	private int getOrd() throws HLDDException {
		if (isArray()) {
			throw new HLDDException("Condition: getOrd(): scalar type expected, actual type is array: " + toString());
		} else {
			return value;
		} //todo: enum
	}

	public void toArray(boolean[] destConditions) throws HLDDException {
		int maxIdx = destConditions.length - 1;
		if (isArray()) {
			for (Condition condition : arrayConditions) {
				int value = condition.getOrd();
				checkBoundaries(value, 0, maxIdx, "Condition: toArray(): condition out of bounds: ");
				destConditions[value] = true;
			}
		} else {
			checkBoundaries(value, 0, maxIdx, "Condition: toArray(): condition out of bounds: ");
			destConditions[value] = true;
		} //todo: enum
	}

	public static void checkBoundaries(int idx, int minIdx, int maxIdx, String msgStart) throws HLDDException {
		if (idx < minIdx || idx > maxIdx) {
			throw new HLDDException(msgStart + idx + ", bounds were " + minIdx + " to " + maxIdx);
		}
	}

	@Override
	public int hashCode() {
		int result = HashCodeUtil.SEED;
		if (isArray()) {
			result = HashCodeUtil.hash(result, arrayConditions);
		} else {
			result = HashCodeUtil.hash(result, value);
		} //todo: enum
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;

		Condition second = (Condition) obj;
		if (isArray() ^ second.isArray()) return false;
		if (isArray()) {
			/* Here both are arrays */
			return Arrays.equals(arrayConditions, second.arrayConditions);
		} else {
			/* Here both are non-arrays */
			return value == second.value;
		} //todo: enum
	}

	public int compareTo(Condition o) {
		int left = getLeft();
		int oLeft = o.getLeft();
		if (left < oLeft) {
			return -1;
		}
		if (left > oLeft) {
			return 1;
		}
		// here left and oLeft are equal ( e.g. {0} and {0,8,19} )
		if (this.equals(o)) {
			return 0;
		}
		// here left and oLeft are equal, but the internal structure is different
		Iterator<Condition> listIterator = asList().iterator();
		Iterator<Condition> oListIterator = o.asList().iterator();
		listIterator.next();
		oListIterator.next(); // skip 1st items (here left and oLeft are equal anyway)
		while (true) {
			// the one with more items (conditions) is greater
			if (listIterator.hasNext() ^ oListIterator.hasNext()) {
				return listIterator.hasNext() ? 1 : -1;
			}
			Condition condition = listIterator.next();
			Condition oCondition = oListIterator.next();
			int res = condition.compareTo(oCondition);
			if (res != 0) {
				return res;
			}
		}
		//todo: enum
	}

	public List<Condition> asList() {
		return isArray() ? Arrays.asList(arrayConditions) : Arrays.asList(this);
	}

	@Override
	public String toString() {
		if (isArray()) {
			StringBuilder builder = new StringBuilder();
			int start = -1, end = -1; // start of range, end of range

			for (Condition cond : arrayConditions) {
				int condition = cond.value;

				if (start == -1) {
					start = end = condition;
					continue;
				}
				if (end + 1 == condition) { //todo: enum
					end = condition;
					continue;
				}
				/* Print, and update start/end with condition */
				if (builder.length() > 0) builder.append(",");
				builder.append(start);
				if (start != end) builder.append("-").append(end);
				start = end = condition;
			}
			/* Print */
			if (builder.length() > 0) builder.append(",");
			builder.append(start);
			if (start != end) builder.append("-").append(end);

			return builder.toString();
		} else {
			return value + "";
		} //todo: enum 
	}

	public String[] toStringArray() {
		String[] conditionAsString;
		if (isArray()) {
			conditionAsString = new String[arrayConditions.length];
			int i = 0;
			for (Condition condition : arrayConditions) {
				conditionAsString[i++] = String.valueOf(condition.value); //todo: enum (array of enums, like S_INIT | S_STOP | S_RESTART )
			}
		} else {
			conditionAsString = new String[]{String.valueOf(value)};
		} //todo: enum
		return conditionAsString;
	}

	public boolean contains(Condition condition) {
		if (condition == null) {
			return false;
		}

		if (!this.isArray()) {
			return !condition.isArray() && this.value == condition.value;
		}

		Set<Condition> conditionsToFind = condition.isArray()
				? new HashSet<Condition>(Arrays.asList(condition.arrayConditions))
				: new HashSet<Condition>(Arrays.asList(condition));
		for (Condition cond : arrayConditions) {
			conditionsToFind.remove(cond);
			if (conditionsToFind.isEmpty()) {
				return true;
			}
		}

		return conditionsToFind.isEmpty();
		//todo: enum
	}

	public Condition invert() throws HLDDException {
		if (!this.equals(TRUE) && !this.equals(FALSE)) {
			throw new HLDDException("Condition: invert(): trying to invert a non-TRUE/non-FALSE value: " + this);
		}

		return this.equals(TRUE) ? FALSE : TRUE;
	}


	private static HashMap<Integer, Condition> conditionPool = new HashMap<Integer, Condition>();
	public static final Condition TRUE = createCondition(1);
	public static final Condition FALSE = createCondition(0);

	public static Condition createCondition(int... values) { //todo: enum
		return values.length == 0 ? null : getFromPool(values.length == 1 ? new Condition(values[0]) : new Condition(values));
	}

	public static Condition createCondition(Collection<Condition> sourceConditions, int conditionValuesCount, boolean fromPresent) throws HLDDException {
		/* Create free space and fill it with available conditions */
		boolean[] usedConditions = new boolean[conditionValuesCount];
		for (Condition condition : sourceConditions) {
			condition.toArray(usedConditions);
		}
		/* Filter present/missing(others) conditions */
		LinkedList<Integer> filteredConditions = new LinkedList<Integer>();
		if (fromPresent) {
			/* Collect present */
			for (int condition = 0; condition < conditionValuesCount; condition++) {
				if (usedConditions[condition]) {
					filteredConditions.add(condition);
				}
			}
		} else {
			/* Collect others */
			for (int condition = 0; condition < conditionValuesCount; condition++) {
				if (!usedConditions[condition]) {
					filteredConditions.add(condition);
				}
			}
		}
		int[] filteredIntArray = integerList2IntArray(filteredConditions);
		/* Create condition */
		return createCondition(filteredIntArray);

	}

	//todo: remove this method. use Collection in Condition's constructor

	private static int[] integerList2IntArray(LinkedList<Integer> conditionsList) {
		int[] filteredIntArray = new int[conditionsList.size()];
		int i = 0;
		for (Integer condition : conditionsList) {
			filteredIntArray[i++] = condition;
		}
		return filteredIntArray;
	}

	private static Condition getFromPool(Condition condition) {
		int hashCode = condition.hashCode();
		if (conditionPool.containsKey(hashCode)) {
			return conditionPool.get(hashCode);
		}
		conditionPool.put(hashCode, condition);
		return condition;
	}

	public static Condition parse(String conditionAsString) throws HLDDException {

		if (conditionAsString == null || conditionAsString.length() == 0) {
			return null;
		}

		LinkedList<Integer> listConditions = new LinkedList<Integer>();

		// 0-1,3-6,9
		String[] ranges = conditionAsString.split(",");
		for (String range : ranges) {
			String[] indices = range.split("-");
			if (indices.length == 1) { // 9
				listConditions.add(Integer.parseInt(indices[0].trim()));
			} else if (indices.length == 2) { // 0-1
				int end = Integer.parseInt(indices[1].trim());
				for (int condition = Integer.parseInt(indices[0].trim()); condition <= end; condition++) {
					listConditions.add(condition);
				}
			} else {
				throw new HLDDException("Condition: parse(): cannot parse condition from line: " + conditionAsString);
			}
		}
		return createCondition(integerList2IntArray(listConditions));
	}

	public static int countValues(Collection<Condition> conditions) {
		int valuesCount = 0;
		for (Condition condition : conditions) {
			valuesCount += condition.isArray() ? condition.arrayConditions.length : 1;
		}
		return valuesCount;
	}

}
