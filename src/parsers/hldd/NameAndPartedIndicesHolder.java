package parsers.hldd;

import base.Indices;

/**
* <br><br>User: Anton Chepurov
* <br>Date: 10.05.2010
* <br>Time: 14:32:12
*/
class NameAndPartedIndicesHolder {
	String name;
	Indices partedIndices;

	NameAndPartedIndicesHolder(String name, Indices partedIndices) {
		this.name = name;
		this.partedIndices = partedIndices;
	}
}
