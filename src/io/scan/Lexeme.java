package io.scan;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 10.09.2008
 * <br>Time: 0:36:45
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
