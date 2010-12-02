package ui.base;

/**
 * @author Anton Chepurov
 */
public class VariableItem extends AbstractItem {

	private final int varIndex;

	public VariableItem(int varIndex) {
		this.varIndex = varIndex;
	}

	@SuppressWarnings({"QuestionableName", "RedundantIfStatement"})
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		VariableItem that = (VariableItem) obj;

		if (varIndex != that.varIndex) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return varIndex;
	}
}
