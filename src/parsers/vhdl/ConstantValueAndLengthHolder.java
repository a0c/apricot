package parsers.vhdl;

import base.Indices;

import java.math.BigInteger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 02.11.2008
 * <br>Time: 1:16:39
 */
public class ConstantValueAndLengthHolder {
    /**
     * BigInteger value of the constant
     */
    private final BigInteger value;
    /**
     * Derived length of the constant (<code>null</code> if not specified)
     */
    private final Indices desiredLength;

    /**
     * @param value {@link #value}
     * @param desiredLength {@link #desiredLength}
     */
    public ConstantValueAndLengthHolder(BigInteger value, Indices desiredLength) {
        this.value = value;
        this.desiredLength = desiredLength;
    }

    /**
     * @return {@link #value}.
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * @return {@link #desiredLength}.
     */
    public Indices getDesiredLength() {
        return desiredLength;
    }
}
