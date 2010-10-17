package base.vhdl.structure;

import base.Type;

/**
 * @author Anton Chepurov
 */
public class Variable {

	private String name;

	private Type type;

	public Variable(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	/* GETTERS and SETTERS */

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

}
