package base.vhdl.structure;

import base.Type;

/**
 * @author Anton Chepurov
 */
public class Variable extends ASTObject {

	private String name;

	private Type type;

	public Variable(String name, Type type) {
		super(null);
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

	@Override
	public Type getType() {
		return type;
	}

}
