package base.vhdl.structure;

import base.Type;

/**
 * @author Anton Chepurov
 */
public class Alias {
	private final String name;
	private final Type type;
	private final OperandImpl actual;

	public Alias(String name, Type type, OperandImpl actual) {
		this.name = name;
		this.type = type;
		this.actual = actual;
	}

	public String getName() {
		return name;
	}

	public OperandImpl getActual() {
		return actual;
	}

	public Type getType() {
		return type;
	}
}
