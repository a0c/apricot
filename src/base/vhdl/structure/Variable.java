package base.vhdl.structure;

import base.Type;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 21:34:15
 */
public class Variable {

    private String name;
    /* Highest Significant Bit. Is negative if signed. */
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
