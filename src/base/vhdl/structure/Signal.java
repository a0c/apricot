package base.vhdl.structure;

import base.Type;

import java.math.BigInteger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 21:28:39
 */
public class Signal {

    private String name;
    /* Highest Significant Bit */
    private Type type;
	
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
