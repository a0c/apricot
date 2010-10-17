package ui.base;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractCoverage {
	private final String title;

	protected AbstractCoverage(String title) {
		this.title = title;
	}

	public abstract String toString();

	public abstract String percentageAsString();

	public String getTitle() {
		return title;
	}
}
