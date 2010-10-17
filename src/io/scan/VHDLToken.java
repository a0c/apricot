package io.scan;

import java.util.regex.Pattern;

/**
 * Class represents a complex token that VHDLReader-s can process.
 * E.g.:
 * 1) 'ENTITY' entity_name 'IS' 'PORT' '('
 * 2) port_name { ‘,’ port_name }  ‘:’  ‘IN’ | ‘OUT’  type ‘;’
 * ...
 *
 * @author Anton Chepurov
 */
public class VHDLToken {

	private Type type;

	private String value;

	public VHDLToken(Type type, String value) {
		this.type = type;
		this.value = value;
	}

	public static Type diagnoseType(String newValue) {
		return Type.diagnoseType(newValue);
	}

	public Type getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "{" + type + "} " + value;
	}

	interface Matchable {
		@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
		boolean matches(String line);
	}

	/**
	 * Sub-Type for base {@link Type}
	 */
	@SuppressWarnings({"EnumeratedClassNamingConvention", "EnumeratedConstantNamingConvention"})
	static enum Sub implements Matchable {
		LBL("[a-zA-Z][\\w]*"),
		INTEGER("(\\-?\\d+)"),
		BIT("(\"(0|1)+\")|('(0|1)+')"),
		BOOLEAN("((TRUE)|(FALSE))"),
		HEX("([\\da-fA-F]+)"),
		BASED_LITERAL("(\\d+ # (" + HEX.regexp + "|[\\._])+ #( [eE]\\+?\\d+)?)"),
		EXPRESSION("(\\( )?.+( \\))?"),
		TYPE("((BIT)" +
				"|(STD_LOGIC)" +
				"|(((BIT)|(STD_LOGIC))_VECTOR \\( " + EXPRESSION.regexp + " DOWNTO " + EXPRESSION.regexp + " \\))" +
				"|(INTEGER)" +
				"|(INTEGER RANGE " + EXPRESSION.regexp + " ((DOWNTO)|(TO)) " + EXPRESSION.regexp + ")" +
				"|(UNSIGNED \\( " + EXPRESSION.regexp + " ((DOWNTO)|(TO)) " + EXPRESSION.regexp + " \\))" +
				"|(BOOLEAN)" +
				"|(NATURAL)" +
				"|(" + LBL.regexp + "))"), // User declared type
		NUMERIC_VALUE("(" + BIT.regexp + "|" + INTEGER.regexp + "|" + BOOLEAN.regexp + "|" + HEX.regexp + "|" + BASED_LITERAL.regexp + ")"),
		INIT("( :=" + NUMERIC_VALUE.regexp + ")?"),
		MUST_INIT(" :=" + NUMERIC_VALUE.regexp),
		OPERAND("((" + LBL.regexp + ")" +
				"|(" + LBL.regexp + " \\( [\\w\'\"]+? \\))" +
				"|(" + LBL.regexp + " \\( \\d+ DOWNTO \\d+ \\)))"),;

		private final String regexp; // Label
		private final Pattern pattern;

		Sub(String regexp) {
			this.regexp = regexp;
			this.pattern = Pattern.compile(regexp);
		}

		@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
		public boolean matches(String line) {
			return pattern.matcher(line).matches();
		}
	}

	@SuppressWarnings({"EnumeratedClassNamingConvention", "EnumeratedConstantNamingConvention"})
	public static enum Type implements Matchable { //todo: it is probably possible to optimize the regexps: currently they are too complex to simply match the declaration.
		USE_DECL("^USE .+;$"),
		ENTITY_DECL("^ENTITY " + Sub.LBL.regexp + " IS$"),
		DECL_CLOSE("^END .*;$"),
		TYPE_ENUM_DECL("^TYPE " + Sub.LBL.regexp + " IS \\( .* \\) ;$"),
		TYPE_DECL("^TYPE " + Sub.LBL.regexp + " IS .+ ;$"),
		GENERIC_OPEN("^GENERIC \\($"),
		GENERIC_DECL("^" + Sub.LBL.regexp + "( , " + Sub.LBL.regexp + ")* :" + Sub.TYPE.regexp + Sub.INIT.regexp + " ;$"),
		PORT_OPEN("^PORT \\($"),
		PORT_DECL("^(SIGNAL )?" + Sub.LBL.regexp + "( , " + Sub.LBL.regexp + ")* :((IN)|(OUT)) " + Sub.TYPE.regexp + Sub.INIT.regexp + " ;$"),
		BEGIN("^BEGIN$"),
		ARCHITECTURE_DECL("^ARCHITECTURE " + Sub.LBL.regexp + " OF " + Sub.LBL.regexp + " IS$"),
		CONSTANT_DECL("^CONSTANT " + Sub.LBL.regexp + " :" + Sub.TYPE.regexp + Sub.MUST_INIT.regexp + " ?;$"),
		SIGNAL_DECL("^SIGNAL " + Sub.LBL.regexp + "( , " + Sub.LBL.regexp + ")* :" + Sub.TYPE.regexp + Sub.INIT.regexp + " ;$"),
		VARIABLE_DECL("^VARIABLE " + Sub.LBL.regexp + "( , " + Sub.LBL.regexp + ")* :" + Sub.TYPE.regexp + Sub.INIT.regexp + " ;$"),
		PROCESS_DECL("(" + Sub.LBL.regexp + " :)?PROCESS \\( " + Sub.LBL.regexp + "( , " + Sub.LBL.regexp + ")* \\)$"),
		COMPONENT_DECL("^COMPONENT " + Sub.LBL.regexp + "$"),
		PORT_MAP("^" + Sub.LBL.regexp + " :" + Sub.LBL.regexp + " PORT MAP \\( .* \\) ;$"),
		ALIAS("^ALIAS " + Sub.LBL.regexp + " :" + Sub.TYPE.regexp + " IS .+ ;$"),
		IF_STATEMENT("^IF .* THEN$"),
		ELSIF_STATEMENT("^ELSIF .* THEN$"),
		ELSE("ELSE"),
		TRANSITION("((^" + Sub.OPERAND.regexp + " ((:=)|(<=)).+? ;$)" +
				"|(^NULL ;$))"),
		CASE_STATEMENT("^CASE .* IS$"),
		WHEN_STATEMENT("^WHEN .* =>$"),
		PACKAGE_DECL("^PACKAGE(?!( BODY )) .+ IS$"),
		PACKAGE_BODY_DECL("^PACKAGE BODY .+ IS$"),
		WITH("^WITH " + Sub.LBL.regexp + " SELECT .+ ;$"),
		UNKNOWN("");

		private final Pattern pattern;

		Type(String regexp) {
			this.pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
		}

		@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
		public boolean matches(String line) {
			return pattern.matcher(line).matches();
		}

		private static Type diagnoseType(String newValue) {
			for (Type type : Type.values()) {
				if (type.matches(newValue)) return type;
			}
			return UNKNOWN;
		}
	}
}
