package base.vhdl.structure;

import base.Type;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 21:28:39
 */
public class Signal {

    private String name;
    /* Highest Significant Bit */
    private Type type;

    public Signal(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
