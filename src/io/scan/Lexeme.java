package io.scan;

/**
 * @author Anton Chepurov
 */
public class Lexeme {
	private String value;
	private LexemeType type;

	public Lexeme(String value, LexemeType type) {
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public LexemeType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "{" + type + "} " + value;
	}
}
