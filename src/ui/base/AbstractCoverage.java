package ui.base;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 23:15:42
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
