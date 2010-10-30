package ui.base;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractCoverage {
	private final String title;
	private final String tooltip;

	protected AbstractCoverage(String title, String tooltip) {
		this.title = title;
		this.tooltip = tooltip;
	}

	public abstract String toString();

	public abstract String percentageAsString();

	public String getTitle() {
		return title;
	}

	public String getTooltip() {
		return tooltip;
	}
}
