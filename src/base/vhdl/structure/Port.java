package base.vhdl.structure;

import base.Type;

/**
 * @author Anton Chepurov
 */
public class Port {

	private String name;

	private boolean isInput;

	private Type type;

	public Port(String name, boolean isInput, Type type) {
		this.name = name;
		this.isInput = isInput;
		this.type = type;
	}

	/* GETTERS and SETTER */

	public String getName() {
		return name;
	}

	public boolean isInput() {
		return isInput;
	}

	public boolean isOutput() {
		return !isInput;
	}

	public Type getType() {
		return type;
	}
}
