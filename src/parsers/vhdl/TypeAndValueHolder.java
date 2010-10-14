package parsers.vhdl;

import base.Type;

import java.math.BigInteger;

/**
 * @author Anton Chepurov
 */
class TypeAndValueHolder {
	final Type type;
	final BigInteger value;

	TypeAndValueHolder(Type type, BigInteger value) {
		this.type = type;
		this.value = value;
	}
}
