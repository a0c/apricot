package parsers;

/**
 * @author Anton Chepurov
 */
class IndicesDeclaration {
	final String lowestIndex;
	final String highestIndex;
	final boolean isDescending;

	public IndicesDeclaration(String lowestIndex, String highestIndex, boolean isDescending) {
		this.lowestIndex = lowestIndex;
		this.highestIndex = highestIndex;
		this.isDescending = isDescending;
	}
}
