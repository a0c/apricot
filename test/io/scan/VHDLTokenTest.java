package io.scan;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 07.10.2008
 * <br>Time: 9:35:28
 */
public class VHDLTokenTest {
    /* ############# TOKEN.SUB ############# */
    /* BOOLEAN */
    private static final String[] booleansMatch = { "\"0101\"", "\"0\"", "\"1\"", "\'0101\'", "\'0\'", "\'1\'" };
    private static final String[] booleansNoMatch = { "\"\"", "\'\'", "\'01201\'", "\"8983\"" };
    /* INTEGER */
    private static final String[] integersMatch = { "10", "2310", "-1", "0", "1", "-12342" };
    private static final String[] integersNoMatch = { "829a", "AB2", "FFF", "fff" };
    /* ############# TOKEN.TYPE ############# */
    /* CONSTANT_DECL */
    private static final String[] constantDeclsMatch = {
            "CONSTANT SOME_CONSTANT :INTEGER :=231;",
            "CONSTANT SOME_CONSTANT :INTEGER :=231 ;"};
    private static final String[] constantDeclsNoMatch = {
            "CONSTANT SOME_CONSTANT :INTEGER;",
            "CONSTANT SOME_CONSTANT :INTEGER ;"};
    /* VARIABLE_DECL */
    private static final String[] variableDeclsMatch = {
            "VARIABLE SOME_VARIABLE :STD_LOGIC :=0 ;",
            "VARIABLE SOME_VARIABLE :BIT :=1 ;",
            "VARIABLE SOME_VARIABLE :BIT ;",
            "VARIABLE SOME_VARIABLE :INTEGER RANGE 231 DOWNTO 1 :=231 ;",
            "VARIABLE SOME_VARIABLE :INTEGER RANGE ( 231 ) DOWNTO ( -1 ) :=231 ;",
            "VARIABLE SOME_VARIABLE :INTEGER RANGE 231 DOWNTO 1 ;",
            "VARIABLE SOME_VARIABLE :INTEGER RANGE ( 231 ) DOWNTO ( -1 ) ;",
            "VARIABLE SOME_VARIABLE :INTEGER :=231 ;",
            "VARIABLE SOME_VARIABLE :INTEGER ;"};
    /* SIGNAL_DECL */
    private static final String[] signalDeclsMatch = Replacer.replace(new String[]{"VARIABLE"}, new String[]{"SIGNAL"}, variableDeclsMatch);
    /* PORT_DECL */
    private static final String[] portDeclsMatch = Replacer.replace(
            new String[]{":S", ":B", ":I", "_VARIABLE", "VARIABLE "},
            new String[]{":OUT S", ":OUT B", ":IN I", "_PORT", ""}, variableDeclsMatch);
    private static final String[] portDeclsMatch2 = {"IPS_ADDR :IN STD_LOGIC_VECTOR ( MOD_EN_BITS - 1 DOWNTO 2 ) ;"};
    /* PORT_DECL */
    private static final String[] genericDeclsMatch ={
            "CRC_CNTX_NUM :INTEGER RANGE 1 TO CRC_CNTX_MAX_NUM :=1 ;"
    };
    /* TYPE_ENUM_DECL */
    private static final String[] typeDeclsMatch = {
            "TYPE STATETYPE IS ( WAITING , INIT , MULTIN , ADDIT , SHIFTING , ENDCOMP , ACCBUS ) ;",
            "TYPE STATETYPE IS ( WAITING ) ;"};
    private static final String[] typeDeclsNoMatch = { "TYPE IS ( WAITING ) ;" };


    /* ############# TOKEN.SUB ############# */

    @Test
    public void correct_BOOLEAN() {
        /* MATCH */
        doCheckMatch(VHDLToken.Sub.BOOLEAN, booleansMatch);
        /* NO MATCH */
        doCheckNoMatch(VHDLToken.Sub.BOOLEAN, booleansNoMatch);
    }

    @Test
    public void correct_INTEGER() {
        /* MATCH */
        doCheckMatch(VHDLToken.Sub.INTEGER, integersMatch);
        /* NO MATCH */
        doCheckNoMatch(VHDLToken.Sub.INTEGER, integersNoMatch);
    }

    @Test
    public void correct_NUMERIC_VALUE() {
        /* MATCH */
        doCheckMatch(VHDLToken.Sub.NUMERIC_VALUE, booleansMatch);
        doCheckMatch(VHDLToken.Sub.NUMERIC_VALUE, integersMatch);

        /* NO MATCH */
        doCheckNoMatch(VHDLToken.Sub.NUMERIC_VALUE, integersNoMatch);
        //todo... HEX
    }

    @Test
    public void correct_INIT() {
        /* MATCH */
        doCheckMatch(VHDLToken.Sub.INIT, createINITarray(booleansMatch));
        doCheckMatch(VHDLToken.Sub.INIT, createINITarray(integersMatch));
        doCheckMatch(VHDLToken.Sub.INIT, "", ""); // no INITialization
        doCheckMatch(VHDLToken.Sub.INIT); // no INITialization
        /* NO MATCH */
        doCheckNoMatch(VHDLToken.Sub.INIT, createINITarray(integersNoMatch));

    }
    @Test
    public void correct_MUST_INIT() {
        /* MATCH */
        doCheckMatch(VHDLToken.Sub.MUST_INIT, createINITarray(booleansMatch));
        doCheckMatch(VHDLToken.Sub.MUST_INIT, createINITarray(integersMatch));
        /* NO MATCH */
        doCheckNoMatch(VHDLToken.Sub.MUST_INIT, createINITarray(integersNoMatch));
        doCheckNoMatch(VHDLToken.Sub.MUST_INIT, "", ""); // no INITialization
        doCheckNoMatch(VHDLToken.Sub.MUST_INIT); // no INITialization

    }

    /* ############# TOKEN.TYPE ############# */

    @Test
    public void correct_TYPE_DECL() {
        /* MATCH */
        doCheckMatch(VHDLToken.Type.TYPE_ENUM_DECL, typeDeclsMatch);
        /* NO MATCH */
        doCheckNoMatch(VHDLToken.Type.TYPE_ENUM_DECL, typeDeclsNoMatch);
    }

    @Test
    public void correct_CONSTANT_DECL() {
        /* MATCH */
        doCheckMatch(VHDLToken.Type.CONSTANT_DECL, constantDeclsMatch);
        /* NO MATCH */
        doCheckNoMatch(VHDLToken.Type.CONSTANT_DECL, constantDeclsNoMatch);

    }

    @Test
    public void correct_VARIABLE_DECL() {
        /* MATCH */
        doCheckMatch(VHDLToken.Type.VARIABLE_DECL, variableDeclsMatch);
    }

    @Test
    public void correct_SIGNAL_DECL() {
        /* MATCH */
        doCheckMatch(VHDLToken.Type.SIGNAL_DECL, signalDeclsMatch);
    }

    @Test
    public void correct_PORT_DECL() {
        /* MATCH */
        doCheckMatch(VHDLToken.Type.PORT_DECL, portDeclsMatch);
        doCheckMatch(VHDLToken.Type.PORT_DECL, portDeclsMatch2);
    }

    @Test
    public void correct_GENERIC_DECL() {
        /* MATCH */
        doCheckMatch(VHDLToken.Type.GENERIC_DECL, genericDeclsMatch);
    }

    /* ########################################################## */
    /* ########################################################## */

    /* HELPER METHODS */
    private void doCheckMatch(VHDLToken.Matcheable matcheable, String... lines) {
        for (String line : lines) {
            assertTrue(matcheable.matches(line));
        }
    }

    private void doCheckNoMatch(VHDLToken.Matcheable matcheable, String... lines) {
        for (String line : lines) {
            assertFalse(matcheable.matches(line));
        }
    }

    private String[] createINITarray(String... lines) {
        String[] newLines = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            newLines[i] = " :=" + lines[i];
        }
        return newLines;
    }

    static class Replacer {
        static String[] replace(String[] what, String[] with, String... where) {
            String[] newArray = java.util.Arrays.copyOf(where, where.length);
            if (what.length != with.length) {
                return newArray;
            }
            for (int j = 0; j < what.length; j++) {
                for (int i = 0; i < where.length; i++) {
                    newArray[i] = newArray[i].replace(what[j], with[j]);
                }
            }
            return newArray;
        }
    }

}
