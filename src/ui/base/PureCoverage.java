package ui.base;

/**
 * @author Anton Chepurov
 */
public class PureCoverage extends AbstractCoverage {

	private double coverage;

	public PureCoverage(double coverage, String title, String tooltip) {
		super(title, tooltip);
		this.coverage = coverage;
	}

	public String toString() {
		return coverage + "";
	}


	public String percentageAsString() {
		return ((int) (coverage * 100)) + "";
	}

	public double getCoverage() {
		return coverage;
	}
}
