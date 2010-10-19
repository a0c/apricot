package parsers.vhdl;

import base.Type;

import java.math.BigInteger;

/**
 * @author Anton Chepurov
 */
class TypeAndValueHolder {
	final Type type;
	final BigInteger value;
	final String valueAsString;

	TypeAndValueHolder(Type type, BigInteger value, String valueAsString) {
		this.type = type;
		this.value = value;
		this.valueAsString = valueAsString;
	}
}
