package parsers.base;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 26.02.2008
 * <br>Time: 11:46:19
 */
public enum OperationEnum {
    ADDER("+"),
    MULT("*"),
    DIV("/"),
    CAT("&"),
    XOR("XOR"),
    SUBTR("-"),
    AND("AND"),
    OR("OR"),
    SHR,
    SHL;

    private final String delim;
    /* REGEXes */
    private static final String REGEX_XOR =
            // XOR at the BEGINNING of the line
            "((^" + XOR.delim + "[\\s\\(].*)" +
            // XOR in the MIDDLE of the line
            "|(.*[\\s\\)]" + XOR.delim + "[\\s\\(].*)" +
            // XOR at the END of the line
            "|(.*[\\s\\)]" + XOR.delim + "$))";
    public static final String REGEX_AND = ".*\\W" + AND.delim + "\\W.*";
    public static final String REGEX_OR = ".*\\W" + OR.delim + "\\W.*";


    /**
     * Constructor for operations with delim (all but SHR and SHL)
     * @param delim operation delimiter
     */
    OperationEnum(String delim) {
        this.delim = delim;
    }

    /**
     * Constructor for operations without delim (SHR and SHL)
     */
    OperationEnum() {
        delim = null;
    }

//    public static boolean isOperation(String line) {
//        /* Check all but SHR and SHL */
//        return isAdder(line)
//                || isMult(line)
//                || isDiv(line)
//                || isCat(line)
//                || isXor(line)
//                || isSubtr(line)
//                || isAnd(line)
//                || isOr(line);
//    }
//
//    private static boolean isOr(String line) {
//        return line.matches(REGEX_OR);
//    }
//
//    private static boolean isAnd(String line) {
//        return line.matches(REGEX_AND);
//    }
//
//    private static boolean isSubtr(String line) {
//        return line.contains(SUBTR.delim);
//    }
//
//    private static boolean isXor(String line) {
//        return line.matches(REGEX_XOR);
//    }
//
//    private static boolean isCat(String line) {
//        return line.contains(CAT.delim);
//    }
//
//    private static boolean isDiv(String line) {
//        return line.contains(DIV.delim);
//    }
//
//    private static boolean isMult(String line) {
//        return line.contains(MULT.delim);
//    }
//
//    private static boolean isAdder(String line) {
//        return line.contains(ADDER.delim);
//    }
}
