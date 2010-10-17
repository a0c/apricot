package io.scan;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
 */
@SuppressWarnings({"InstanceMethodNamingConvention"})
public class VHDLTokenTest {
	/* ############# TOKEN.SUB ############# */
	/* BIT */
	private static final String[] BITS_MATCH = {"\"0101\"", "\"0\"", "\"1\"", "\'0101\'", "\'0\'", "\'1\'"};
	private static final String[] BITS_NO_MATCH = {"\"\"", "\'\'", "\'01201\'", "\"8983\""};
	/* BOOLEAN */
	private static final String[] BOOLEANS_MATCH = {"TRUE", "FALSE"};
	private static final String[] BOOLEANS_NO_MATCH = {"BOOLEAN", "F", "T"};
	/* INTEGER */
	private static final String[] INTEGERS_MATCH = {"10", "2310", "-1", "0", "1", "-12342"};
	private static final String[] INTEGERS_NO_MATCH = {"829a", "AB2", "FFF", "fff"};
	/* HEX */
	private static final String[] HEX_MATCH = {"829a", "10", "AB2", "2310", "FFF", "fff", "0", "1", "ABCDEF"};
	private static final String[] HEX_NO_MATCH = {"142ew", "-1", "-12342"};
	/* BASED_LITERAL */
	private static final String[] BASED_LITERAL_MATCH = {"16 # 01 #", "16 # 02 #", "2 # 010010 #", "2 # 01001 # E1",
			"3 # 012011 # E+2", "2 # 0100.101 #", "2 # 0100_1001 #"};
	/* NUMERIC (no match) */
	private static final String[] NUMERIC_NO_MATCH = {"abcdefg", "23423ew"};
	/* ############# TOKEN.TYPE ############# */
	/* CONSTANT_DECL */
	private static final String[] CONSTANT_DECLARATIONS_MATCH = {
			"CONSTANT SOME_CONSTANT :INTEGER :=231;",
			"CONSTANT SOME_CONSTANT :INTEGER :=231 ;"};
	private static final String[] CONSTANT_DECLARATIONS_NO_MATCH = {
			"CONSTANT SOME_CONSTANT :INTEGER;",
			"CONSTANT SOME_CONSTANT :INTEGER ;"};
	/* VARIABLE_DECL */
	private static final String[] VARIABLE_DECLARATIONS_MATCH = {
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
	private static final String[] SIGNAL_DECLARATIONS_MATCH = Replacer.replace(new String[]{"VARIABLE"}, new String[]{"SIGNAL"}, VARIABLE_DECLARATIONS_MATCH);
	/* PORT_DECL */
	private static final String[] PORT_DECLARATIONS_MATCH = Replacer.replace(
			new String[]{":S", ":B", ":I", "_VARIABLE", "VARIABLE "},
			new String[]{":OUT S", ":OUT B", ":IN I", "_PORT", ""}, VARIABLE_DECLARATIONS_MATCH);
	private static final String[] PORT_DECLARATIONS_MATCH2 = {"IPS_ADDR :IN STD_LOGIC_VECTOR ( MOD_EN_BITS - 1 DOWNTO 2 ) ;"};
	/* PORT_DECL */
	private static final String[] GENERIC_DECLARATIONS_MATCH = {
			"CRC_CNTX_NUM :INTEGER RANGE 1 TO CRC_CNTX_MAX_NUM :=1 ;"
	};
	/* TYPE_ENUM_DECL */
	private static final String[] TYPE_DECLARATIONS_MATCH = {
			"TYPE STATETYPE IS ( WAITING , INIT , MULTIN , ADDIT , SHIFTING , ENDCOMP , ACCBUS ) ;",
			"TYPE STATETYPE IS ( WAITING ) ;"};
	private static final String[] TYPE_DECLARATIONS_NO_MATCH = {"TYPE IS ( WAITING ) ;"};


	/* ############# TOKEN.SUB ############# */

	@Test
	public void correct_BIT() {
		/* MATCH */
		doCheckMatch(VHDLToken.Sub.BIT, BITS_MATCH);
		/* NO MATCH */
		doCheckNoMatch(VHDLToken.Sub.BIT, BITS_NO_MATCH);
	}

	@Test
	public void correct_BOOLEAN() {
		/* MATCH */
		doCheckMatch(VHDLToken.Sub.BOOLEAN, BOOLEANS_MATCH);
		/* NO MATCH */
		doCheckNoMatch(VHDLToken.Sub.BOOLEAN, BOOLEANS_NO_MATCH);
	}

	@Test
	public void correct_INTEGER() {
		/* MATCH */
		doCheckMatch(VHDLToken.Sub.INTEGER, INTEGERS_MATCH);
		/* NO MATCH */
		doCheckNoMatch(VHDLToken.Sub.INTEGER, INTEGERS_NO_MATCH);
	}

	@Test
	public void correct_HEX() {
		/* MATCH */
		doCheckMatch(VHDLToken.Sub.HEX, HEX_MATCH);
		/* NO MATCH */
		doCheckNoMatch(VHDLToken.Sub.HEX, HEX_NO_MATCH);
	}

	@Test
	public void correct_BASED_LITERAL() {
		/* MATCH */
		doCheckMatch(VHDLToken.Sub.BASED_LITERAL, BASED_LITERAL_MATCH);
	}

	@Test
	public void correct_NUMERIC_VALUE() {
		/* MATCH */
		doCheckMatch(VHDLToken.Sub.NUMERIC_VALUE, BITS_MATCH);
		doCheckMatch(VHDLToken.Sub.NUMERIC_VALUE, BOOLEANS_MATCH);
		doCheckMatch(VHDLToken.Sub.NUMERIC_VALUE, INTEGERS_MATCH);
		doCheckMatch(VHDLToken.Sub.NUMERIC_VALUE, HEX_MATCH);
		doCheckMatch(VHDLToken.Sub.NUMERIC_VALUE, BASED_LITERAL_MATCH);

		/* NO MATCH */
		doCheckNoMatch(VHDLToken.Sub.NUMERIC_VALUE, BITS_NO_MATCH);
		doCheckNoMatch(VHDLToken.Sub.NUMERIC_VALUE, NUMERIC_NO_MATCH);
	}

	@Test
	public void correct_INIT() {
		/* MATCH */
		doCheckMatch(VHDLToken.Sub.INIT, createINITarray(BOOLEANS_MATCH));
		doCheckMatch(VHDLToken.Sub.INIT, createINITarray(INTEGERS_MATCH));
		doCheckMatch(VHDLToken.Sub.INIT, "", ""); // no INITialization
		doCheckMatch(VHDLToken.Sub.INIT); // no INITialization
		/* NO MATCH */
		doCheckNoMatch(VHDLToken.Sub.INIT, createINITarray(NUMERIC_NO_MATCH));

	}

	@Test
	public void correct_MUST_INIT() {
		/* MATCH */
		doCheckMatch(VHDLToken.Sub.MUST_INIT, createINITarray(BOOLEANS_MATCH));
		doCheckMatch(VHDLToken.Sub.MUST_INIT, createINITarray(INTEGERS_MATCH));
		/* NO MATCH */
		doCheckNoMatch(VHDLToken.Sub.MUST_INIT, createINITarray(NUMERIC_NO_MATCH));
		doCheckNoMatch(VHDLToken.Sub.MUST_INIT, "", ""); // no INITialization
		doCheckNoMatch(VHDLToken.Sub.MUST_INIT); // no INITialization

	}

	/* ############# TOKEN.TYPE ############# */

	@Test
	public void correct_TYPE_DECL() {
		/* MATCH */
		doCheckMatch(VHDLToken.Type.TYPE_ENUM_DECL, TYPE_DECLARATIONS_MATCH);
		/* NO MATCH */
		doCheckNoMatch(VHDLToken.Type.TYPE_ENUM_DECL, TYPE_DECLARATIONS_NO_MATCH);
	}

	@Test
	public void correct_CONSTANT_DECL() {
		/* MATCH */
		doCheckMatch(VHDLToken.Type.CONSTANT_DECL, CONSTANT_DECLARATIONS_MATCH);
		/* NO MATCH */
		doCheckNoMatch(VHDLToken.Type.CONSTANT_DECL, CONSTANT_DECLARATIONS_NO_MATCH);
	}

	@Test
	public void correct_VARIABLE_DECL() {
		/* MATCH */
		doCheckMatch(VHDLToken.Type.VARIABLE_DECL, VARIABLE_DECLARATIONS_MATCH);
	}

	@Test
	public void correct_SIGNAL_DECL() {
		/* MATCH */
		doCheckMatch(VHDLToken.Type.SIGNAL_DECL, SIGNAL_DECLARATIONS_MATCH);
	}

	@Test
	public void correct_PORT_DECL() {
		/* MATCH */
		doCheckMatch(VHDLToken.Type.PORT_DECL, PORT_DECLARATIONS_MATCH);
		doCheckMatch(VHDLToken.Type.PORT_DECL, PORT_DECLARATIONS_MATCH2);
	}

	@Test
	public void correct_GENERIC_DECL() {
		/* MATCH */
		doCheckMatch(VHDLToken.Type.GENERIC_DECL, GENERIC_DECLARATIONS_MATCH);
	}

	/* ########################################################## */
	/* ########################################################## */

	/* HELPER METHODS */

	private void doCheckMatch(VHDLToken.Matchable matchable, String... lines) {
		for (String line : lines) {
			assertTrue(line + " didn't match " + matchable, matchable.matches(line));
		}
	}

	private void doCheckNoMatch(VHDLToken.Matchable matchable, String... lines) {
		for (String line : lines) {
			assertFalse(line + " matched " + matchable, matchable.matches(line));
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
