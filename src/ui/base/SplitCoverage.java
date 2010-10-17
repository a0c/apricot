package ui.base;

/**
 * @author Anton Chepurov
 */
public class SplitCoverage extends AbstractCoverage {
	
	private int covered;
	private int total;

	public SplitCoverage(int covered, int total, String title) {
		super(title);
		this.covered = covered;
		this.total = total;
	}

	public String toString() {
		return covered + " / " + total;
	}

	public String percentageAsString() {
		return ((int) (((double) covered) / ((double) total) * 100)) + "";
	}

	public int getCovered() {
		return covered;
	}

	public int getTotal() {
		return total;
	}

}
