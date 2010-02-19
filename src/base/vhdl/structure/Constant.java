package base.vhdl.structure;

import java.math.BigInteger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 21:26:54
 */
public class Constant {

    private String name;

    private BigInteger value;

    public Constant(String constantName, BigInteger value) {
        this.name = constantName;
        this.value = value;
    }

    /* GETTERS and SETTERS */

    public String getName() {
        return name;
    }

    public BigInteger getValue() {
        return value;
    }
}
