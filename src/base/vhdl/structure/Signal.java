package base.vhdl.structure;

import base.Type;

/**
 * @author Anton Chepurov
 */
public class Signal {

	private final String name;

	private final Type type;

	private final OperandImpl defaultValue;

	public Signal(String name, Type type, OperandImpl defaultValue) {
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

	public OperandImpl getDefaultValue() {
		return defaultValue;
	}
}
