package base.vhdl.structure;

import base.Type;

import java.math.BigInteger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 21:26:54
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
