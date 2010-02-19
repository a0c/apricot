package parsers;

/**
 * <br><br>User: Anton Chepurov
* <br>Date: 08.04.2009
* <br>Time: 2:29:30
*/
class IndicesDeclaration {
    private final String lowestIndex;
    private final String highestIndex;
    private final boolean isDescending;

    public IndicesDeclaration(String lowestIndex, String highestIndex, boolean isDescending) {
        this.lowestIndex = lowestIndex;
        this.highestIndex = highestIndex;
        this.isDescending = isDescending;
    }

    public String getLowestIndex() {
        return lowestIndex;
    }

    public String getHighestIndex() {
        return highestIndex;
    }

    public boolean isDescending() {
        return isDescending;
    }
}
