package parsers.vhdl;

import base.Type;
import base.hldd.structure.nodes.utils.Condition;

import java.math.BigInteger;
import java.util.Map;

/**
 * @author Anton Chepurov
 */
class TypeAndValueHolder {
	final Type type;
	final BigInteger value;
	final Map<Condition, String> valuesAsString;

	TypeAndValueHolder(Type type, BigInteger value, Map<Condition, String> valuesAsString) {
		this.type = type;
		this.value = value;
		this.valuesAsString = valuesAsString;
	}
}
