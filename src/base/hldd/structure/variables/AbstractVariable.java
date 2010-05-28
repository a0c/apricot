package base.hldd.structure.variables;

import base.Type;
import base.Indices;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 11.02.2008
 * <br>Time: 9:44:21
 */
//todo: turn into Interface. There is no implementation at all!
public abstract class AbstractVariable {

    public abstract boolean isIdenticalTo(AbstractVariable comparedAbsVariable);

    public abstract void setIndex(int index);

    public abstract void forceSetIndex(int index);

    public abstract int getIndex();

    public abstract boolean isInput();

    public abstract boolean isOutput();

    public abstract String getName();

    public abstract String getPureName();

    public abstract Type getType();

    public abstract Indices getLength();

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
}
