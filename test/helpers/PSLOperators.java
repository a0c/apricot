package helpers;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 27.10.2008
 * <br>Time: 8:28:35
 */
public interface PSLOperators {
    /**
     * Array of SUPPORTED operators
     */
    public static final String[] operatorDeclarations = {
            "always TOP",
            "never BOP",
            "BOP1 <-> BOP2",
            "BOP -> TOP",
            "TOP until BOP",
            "BOP1 before BOP2",
            "next TOP",
            "next[k] TOP",
            "next_e[j to k] BOP",
            "next_a[j to k] TOP",
            "next_event BOP[k] TOP",
            "BOP or TOP",
            "TOP1 and TOP2",
            "! BOP"
    };

    public static final String[] matchingRegexes = {
            "^[aA][lL][wW][aA][yY][sS][\\s\\(].*",
            "^[nN][eE][vV][eE][rR][\\s\\(].*",
            ".*.<\\->..*",
            ".*.\\->..*",
            ".*[\\s\\)][uU][nN][tT][iI][lL][\\s\\(].*",
            ".*[\\s\\)][bB][eE][fF][oO][rR][eE][\\s\\(].*",
            "^[nN][eE][xX][tT][\\s\\(].*",
            "^[nN][eE][xX][tT]\\s*\\[.+\\].*",
            "^[nN][eE][xX][tT]_[eE]\\s*\\[.+\\].*",
            "^[nN][eE][xX][tT]_[aA]\\s*\\[.+\\].*",
            "^[nN][eE][xX][tT]_[eE][vV][eE][nN][tT]\\s*\\[.+\\].*", //todo...
            ".*[\\s\\)][oO][rR][\\s\\(].*",
            ".*[\\s\\)][aA][nN][dD][\\s\\(].*",
            "^!..*",

    };
    public static final String[] splittingRegexes = {
            "^[aA][lL][wW][aA][yY][sS][\\s\\(]",
            "^[nN][eE][vV][eE][rR][\\s\\(]",
            "<\\->",
            "\\->",
            "[\\s\\)][uU][nN][tT][iI][lL][\\s\\(]",
            "[\\s\\)][bB][eE][fF][oO][rR][eE][\\s\\(]",
            "^[nN][eE][xX][tT][\\s\\(]",
            "^[nN][eE][xX][tT]\\s*\\[.+\\]",
            "^[nN][eE][xX][tT]_[eE]\\s*\\[.+\\]",
            "^[nN][eE][xX][tT]_[aA]\\s*\\[.+\\]",
            "^[nN][eE][xX][tT]_[eE][vV][eE][nN][tT]\\s*\\[.+\\]", //todo...
            "[\\s\\)][oO][rR][\\s\\(]",
            "[\\s\\)][aA][nN][dD][\\s\\(]",
            "^!"                                               
    };
    public static final int[] splitLimits = { 2,2,2,2,2,2,2,2,2,2,2,2,2,2 };




}
