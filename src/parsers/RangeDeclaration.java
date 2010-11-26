package parsers;

/**
 * @author Anton Chepurov
 */
class RangeDeclaration {
	final String lowestIndex;
	final String highestIndex;
	final boolean isDescending;

	public RangeDeclaration(String lowestIndex, String highestIndex, boolean isDescending) {
		this.lowestIndex = lowestIndex;
		this.highestIndex = highestIndex;
		this.isDescending = isDescending;
	}
}
