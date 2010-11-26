package parsers.vhdl;

import base.Range;

import java.math.BigInteger;

/**
 * @author Anton Chepurov
 */
public class ConstantValueAndLengthHolder {
	/**
	 * BigInteger value of the constant
	 */
	private final BigInteger value;
	/**
	 * Derived length of the constant (<code>null</code> if not specified)
	 */
	private final Range desiredLength;

	/**
	 * @param value		 {@link #value}
	 * @param desiredLength {@link #desiredLength}
	 */
	public ConstantValueAndLengthHolder(BigInteger value, Range desiredLength) {
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
	public Range getDesiredLength() {
		return desiredLength;
	}
}
