package ui.base;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.06.2008
 * <br>Time: 0:15:13
 */
public class SplittedCoverage extends AbstractCoverage {
    private int covered;
    private int total;

    public SplittedCoverage(int covered, int total, String title) {
        super(title);
        this.covered = covered;
        this.total = total;
    }

    public String toString() {
        return covered + " / " + total;
    }

    public String percentageAsString() {
        return ((int)(((double) covered) / ((double) total) * 100)) + "";
    }

    public int getCovered() {
        return covered;
    }

    public int getTotal() {
        return total;
    }

}
