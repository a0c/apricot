package ui.base;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.06.2008
 * <br>Time: 0:16:12
 */
public class PureCoverage extends AbstractCoverage {
    private double coverage;

    public PureCoverage(double coverage, String title) {
        super(title);
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
