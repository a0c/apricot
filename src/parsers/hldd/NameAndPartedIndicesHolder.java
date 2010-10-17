package parsers.hldd;

import base.Indices;

/**
 * @author Anton Chepurov
 */
class NameAndPartedIndicesHolder {
	String name;
	Indices partedIndices;

	NameAndPartedIndicesHolder(String name, Indices partedIndices) {
		this.name = name;
		this.partedIndices = partedIndices;
	}
}
