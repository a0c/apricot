package base.hldd.structure.variables;

import base.HierarchyLocation;
import base.Type;
import base.Range;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractVariable implements Comparable<AbstractVariable> {

	public abstract boolean isIdenticalTo(AbstractVariable comparedAbsVariable);

	public abstract void setIndex(int index);

	public abstract void forceSetIndex(int index);

	public abstract int getIndex();

	public abstract void setDefaultValue(ConstantVariable defaultValue);

	public abstract ConstantVariable getDefaultValue();

	public abstract boolean isInput();

	public abstract boolean isOutput();

	public abstract String getPureName();

	public abstract Type getType();

	public abstract Range getLength();

	public abstract boolean isState();

	public abstract String lengthToString();

	public abstract String toString();

	public abstract boolean isReset();

	public abstract boolean isFSM();

	public abstract boolean isCout();

	public abstract boolean isSigned();

	public abstract boolean isDelay();

	public boolean isExpansion() { // todo: do the same (default behavior) for other methods, where possible (e.g. FSM)
		return false;
	}

	private final HierarchyLocation location = new HierarchyLocation();

	public String getName() {

		return location.toString();
	}

	public void addNamePrefix(String namePrefix) {

		location.addLocation(namePrefix);
	}

	public boolean isTopLevel() {
		return location.isTopLevel();
	}

	@Override
	public int compareTo(AbstractVariable o) {

		return getName().compareTo(o.getName());
	}
}
