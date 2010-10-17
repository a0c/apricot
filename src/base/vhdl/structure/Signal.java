package base.vhdl.structure;

import base.Type;

import java.math.BigInteger;

/**
 * @author Anton Chepurov
 */
public class Signal {

	private final String name;

	private final Type type;

	private final BigInteger defaultValue;

	public Signal(String name, Type type, BigInteger defaultValue) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public BigInteger getDefaultValue() {
		return defaultValue;
	}
}
