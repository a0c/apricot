package base.vhdl.structure;

import base.Type;

import java.math.BigInteger;

/**
 * @author Anton Chepurov
 */
public class Constant {

	private String name;

	private final Type type;

	private BigInteger value;

	public Constant(String constantName, Type type, BigInteger value) {
		this.name = constantName;
		this.type = type;
		this.value = value;
	}

	/* GETTERS and SETTERS */

	public String getName() {
		return name;
	}

	public BigInteger getValue() {
		return value;
	}

	public Type getType() {
		return type;
	}
}
