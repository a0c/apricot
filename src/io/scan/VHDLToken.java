package io.scan;

import java.util.regex.Pattern;

/**
 * Class represents a complex token that VHDLReader-s can process.
 * E.g.:
 * 1) 'ENTITY' entity_name 'IS' 'PORT' '('
 * 2) port_name { ‘,’ port_name }  ‘:’  ‘IN’ | ‘OUT’  type ‘;’
 * ...
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 04.02.2008
 * <br>Time: 22:18:40
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

    /* GETTERS and SETTERS */

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }


    interface Matcheable {
        boolean matches(String line);
    }

    /**
     * Sub-Type for base {@link Type}
     */
    static enum Sub implements Matcheable {
        LBL("[a-zA-Z][\\w]*"),
        INTEGER("(\\-?\\d+)"),
        BIT("(\"(0|1)+\")|('(0|1)+')"),
        BOOLEAN("((TRUE)|(FALSE))"),
//        HEX("([\\dA-F]+)"), //todo: uncomment and correct the regex, now it's wrong
        EXPRESSION("(\\( )?.+( \\))?"),
        TYPE("((BIT)" +
                "|(STD_LOGIC)" +
                "|(((BIT)|(STD_LOGIC))_VECTOR \\( " + EXPRESSION.regex + " DOWNTO " + EXPRESSION.regex + " \\))" + //"|(((BIT)|(STD_LOGIC))_VECTOR \\( " + Sub.INTEGER.regex + " DOWNTO " + Sub.INTEGER.regex + " \\))" +
                "|(INTEGER)" +
                "|(INTEGER RANGE " + EXPRESSION.regex + " ((DOWNTO)|(TO)) " + EXPRESSION.regex + ")" + //"|(INTEGER RANGE (\\( )?" + Sub.INTEGER.regex + "( \\))? ((DOWNTO)|(TO)) (\\( )?" + Sub.INTEGER.regex + "( \\))?)" +
                "|(UNSIGNED \\( " + EXPRESSION.regex + " ((DOWNTO)|(TO)) " + EXPRESSION.regex + " \\))" +
                "|(BOOLEAN)" +
                "|(" + LBL.regex + "))"), // User declared type
        NUMERIC_VALUE("(" + BIT.regex + "|" + INTEGER.regex + "|" + BOOLEAN.regex + ")"), /*"(("+ Sub.INTEGER.regex + ")" + "|(\'"+ Sub.INTEGER.regex + "\')" + "|(\""+ Sub.INTEGER.regex + "\"))"*/
        INIT("( :=" + NUMERIC_VALUE.regex + ")?"),
        MUST_INIT(" :=" + NUMERIC_VALUE.regex),
        OPERAND("((" + LBL.regex + ")" +
                "|(" + LBL.regex + " \\( [\\w\'\"]+? \\))" +
                "|(" + LBL.regex + " \\( \\d+ DOWNTO \\d+ \\)))"),
        ;

        private final String regex; // Label
        private final Pattern pattern;

        Sub(String regex) {
            this.regex = regex;
            this.pattern = Pattern.compile(regex);
        }

        public boolean matches(String line) {
            return pattern.matcher(line).matches();
        }
    }

    public static enum Type implements Matcheable { //todo: it is probably possible to optimize the regexes: currently they are too complex to simply match the declaration.
        USE_DECL("^USE .+;$"),
        ENTITY_DECL("^ENTITY " + Sub.LBL.regex + " IS$"), // PORT \\($
        DECL_CLOSE("^END .*;$"), // "^END " + Sub.LBL.regex + " (" + Sub.LBL.regex + " )?;$"
        TYPE_ENUM_DECL("^TYPE " + Sub.LBL.regex + " IS \\( .* \\) ;$"),
        TYPE_DECL("^TYPE " + Sub.LBL.regex + " IS .+ ;$"), // previously was TYPE_ARRAY_DECL
        GENERIC_OPEN("^GENERIC \\($"),
        GENERIC_DECL("^" + Sub.LBL.regex + "( , " + Sub.LBL.regex + ")* :" + Sub.TYPE.regex + Sub.INIT.regex + " ;$"),
        PORT_OPEN("^PORT \\($"),
        PORT_DECL("^(SIGNAL )?" + Sub.LBL.regex + "( , " + Sub.LBL.regex + ")* :((IN)|(OUT)) " + Sub.TYPE.regex + Sub.INIT.regex + " ;$"),
        BEGIN("^BEGIN$"),
        ARCHITECTURE_DECL("^ARCHITECTURE " + Sub.LBL.regex + " OF " + Sub.LBL.regex + " IS$"),
        CONSTANT_DECL("^CONSTANT " + Sub.LBL.regex + " :" + Sub.TYPE.regex + Sub.MUST_INIT.regex + " ?;$"),
        SIGNAL_DECL("^SIGNAL " + Sub.LBL.regex + "( , " + Sub.LBL.regex + ")* :" + Sub.TYPE.regex + Sub.INIT.regex + " ;$"),
        VARIABLE_DECL("^VARIABLE " + Sub.LBL.regex + "( , " + Sub.LBL.regex + ")* :" + Sub.TYPE.regex + Sub.INIT.regex + " ;$"),
        PROCESS_DECL("(" + Sub.LBL.regex + " :)?PROCESS \\( " + Sub.LBL.regex + "( , " + Sub.LBL.regex + ")* \\)$"),
		COMPONENT_DECL("^COMPONENT " + Sub.LBL.regex + "$"),
		PORT_MAP("^" + Sub.LBL.regex + " :" + Sub.LBL.regex + " PORT MAP \\( .* \\) ;$"),
        IF_STATEMENT("^IF .* THEN$"),
        ELSIF_STATEMENT("^ELSIF .* THEN$"),
        ELSE("ELSE"),
        TRANSITION("((^" + Sub.OPERAND.regex + " ((:=)|(<=)).+? ;$)" +
                "|(^NULL ;$))"),
        CASE_STATEMENT("^CASE .* IS$"),
        WHEN_STATEMENT("^WHEN .* =>$"),
        PACKAGE_DECL("^PACKAGE(?!( BODY )) .+ IS$"),
        PACKAGE_BODY_DECL("^PACKAGE BODY .+ IS$"),
        UNKNOWN("");

        private final Pattern pattern;

        Type(String regex) {
            this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }


        public boolean matches(String line) {
            return pattern.matcher(line).matches();
        }

        
        private static Type diagnoseType(String newValue) {
            for (Type type : Type.values()) {
                // Below are subtypes used for representing standalone VHDL types (the subtypes are
                // never used alone, but rather as a part of a standalone VHDL type):
                /* Condition before introducing Sub:
                        !(type == INTEGER || type == TYPE || type == CONST_TYPE || type == NUMERIC_VALUE
                        || type == UNKNOWN || type == OPERAND || type == LBL)*/
                if (type.matches(newValue)) return type;
            }
            return UNKNOWN;
        }
    }
}
