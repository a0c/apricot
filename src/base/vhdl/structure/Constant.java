package base.vhdl.structure;

import base.Type;

import java.math.BigInteger;

/**
 * @author Anton Chepurov
 */
public class Constant extends ASTObject {

	private String name;

	private final Type type;

	private BigInteger value;

	public Constant(String constantName, Type type, BigInteger value) {
		super(null);
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

	@Override
	public Type getType() {
		return type;
	}
}
