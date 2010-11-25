package base.vhdl.structure;

import base.Type;

/**
 * @author Anton Chepurov
 */
public class Signal extends ASTObject {

	private final String name;

	private final Type type;

	private final OperandImpl defaultValue;

	public Signal(String name, Type type, OperandImpl defaultValue) {
		super(null);
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		return type;
	}

	public OperandImpl getDefaultValue() {
		return defaultValue;
	}
}
