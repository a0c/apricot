package base.vhdl.structure;

import base.Type;

/**
 * @author Anton Chepurov
 */
public class Port extends ASTObject {

	private String name;

	private boolean isInput;

	private Type type;

	public Port(String name, boolean isInput, Type type) {
		super(null);
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

	@Override
	public Type getType() {
		return type;
	}
}
