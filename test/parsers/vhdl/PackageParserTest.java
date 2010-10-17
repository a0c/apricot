package parsers.vhdl;

import org.junit.Test;

import static org.junit.Assert.*;

import java.math.BigInteger;

import base.Type;

/**
 * @author Anton Chepurov
 */
public class PackageParserTest {
	private final ParsingConstant[] parsingConstants = {
			new ParsingConstant("0", 0, 1),
			new ParsingConstant("'0'", 0, 1),
			new ParsingConstant("1", 1, 1),
			new ParsingConstant("'1'", 1, 1),
			new ParsingConstant("10", 10, 4),
			new ParsingConstant("'10'", 2, 2),
			new ParsingConstant("X \"10\"", 16, 8),
			new ParsingConstant("1000", 1000, 10),
			new ParsingConstant("'1000'", 8, 4),
			new ParsingConstant("X \"1\"", 1, 4),
			new ParsingConstant("X \"0\"", 0, 4),
			new ParsingConstant("X \"11\"", 17, 8),
			new ParsingConstant("X \"100\"", 256, 12),
			new ParsingConstant("X \"FFFF\"", 65535, 16),
			new ParsingConstant("X \"FFFFFF\"", 16777215, 24),
			new ParsingConstant("X \"000000\"", 0, 24),
			new ParsingConstant("'000000'", 0, 6),
			new ParsingConstant("000000", 0, 1),
			new ParsingConstant("true", 1, 1),
			new ParsingConstant("TRUE", 1, 1),
			new ParsingConstant("false", 0, 1),
			new ParsingConstant("FALSE", 0, 1),
			new ParsingConstant("16 # 00 #", 0, 1),
			new ParsingConstant("16 # 01 #", 1, 1),
			new ParsingConstant("16 # 02 #", 2, 2),
			new ParsingConstant("16 # 03 #", 3, 2),
			new ParsingConstant("2 # 010010 #", 18, 5),
			new ParsingConstant("3 # 012011 #", 139, 8),
	};

	@Test
	public void correctParseConstantValue() {
		/* BINARY, DECIMAL, HEX, BOOLEAN */
		for (ParsingConstant parsingConstant : parsingConstants) {
			assertEquals(parsingConstant.valueInt, PackageParser.parseConstantValue(parsingConstant.valueAsString));
		}

		assertNull(PackageParser.parseConstantValue("NamedConstant"));
		assertNull(PackageParser.parseConstantValue("CONST"));
		assertNull(PackageParser.parseConstantValue("CONST_8"));
	}

	@SuppressWarnings({"InstanceMethodNamingConvention"})
	@Test
	public void correctParseConstantValueWithLength() {
		for (ParsingConstant parsingConstant : parsingConstants) {
			ConstantValueAndLengthHolder holder = PackageParser.parseConstantValueWithLength(parsingConstant.valueAsString);
			assertEquals("Incorrect parsed value for " + parsingConstant.valueAsString + ":",
					parsingConstant.valueInt, holder.getValue());
			int length = holder.getDesiredLength() != null ? holder.getDesiredLength().length()
					: base.Indices.deriveLengthForValues(holder.getValue().intValue(), 0).length();
			assertEquals("Incorrect parsed length for " + parsingConstant.valueAsString + ":",
					parsingConstant.length, length);
		}
		assertNull(PackageParser.parseConstantValueWithLength("NamedConstant"));
		assertNull(PackageParser.parseConstantValueWithLength("CONST"));
		assertNull(PackageParser.parseConstantValueWithLength("CONST_8"));

	}

	private class ParsingConstant {
		private final String valueAsString;
		private final BigInteger valueInt;
		private final int length;

		public ParsingConstant(String valueAsString, int valueInt, int length) {
			this.valueAsString = valueAsString;
			this.valueInt = BigInteger.valueOf(valueInt);
			this.length = length;
		}
	}


	@Test
	public void parseType() throws Exception {
		StructureBuilder builder = new StructureBuilder();
		/* ##########################################
		* INTEGER RANGE 32767 DOWNTO -32768 */
		/* INTEGER RANGE 0 TO 3
		* ##########################################*/
		String typeString;
		Type type = PackageParser.parseType(typeString = "INTEGER RANGE 32767 DOWNTO -32768", builder).type;
		assertTrue("Type is not enum: " + typeString, type.isEnum());
		assertTrue("Type is not signed: " + typeString, type.isSigned());
		assertEquals("Wrong cardinality for type: " + typeString, 65536, type.getCardinality());
		assertEquals("Wrong highestSB for type: " + typeString, 15, type.getHighestSB());
		assertEquals("Wrong offset for type: " + typeString, 0, type.getOffset());
		assertEquals("Wrong length printing for type: " + typeString, "<15:0>", type.lengthToString());
		type = PackageParser.parseType(typeString = "INTEGER RANGE 0 TO 3", builder).type;
		assertTrue("Type is not enum: " + typeString, type.isEnum());
		assertFalse("Type is signed: " + typeString, type.isSigned());
		assertEquals("Wrong cardinality for type: " + typeString, 4, type.getCardinality());
		assertEquals("Wrong highestSB for type: " + typeString, 1, type.getHighestSB());
		assertEquals("Wrong offset for type: " + typeString, 0, type.getOffset());
		assertEquals("Wrong length printing for type: " + typeString, "<1:0>", type.lengthToString());

		/* ########################################################
		*  BIT_VECTOR ( 8 DOWNTO 0) */
		/* {IN} STD_LOGIC_VECTOR(MOD_EN_BITS-3 DOWNTO 0)
		* ########################################################*/
		type = PackageParser.parseType(typeString = "BIT_VECTOR ( 8 DOWNTO 0 )", builder).type;
		assertFalse("Type is enum: " + typeString, type.isEnum());
		assertFalse("Type is signed: " + typeString, type.isSigned());
		RuntimeException e = null;
		try {
			assertEquals("Wrong cardinality for type: " + typeString, 0, type.getCardinality());
		} catch (RuntimeException e1) {
			e = e1;
		}
		assertNotNull("Exception not thrown when obtaining cardinality for: " + typeString, e);
		assertEquals("Wrong highestSB for type: " + typeString, 8, type.getHighestSB());
		assertEquals("Wrong offset for type: " + typeString, 0, type.getOffset());
		assertEquals("Wrong length printing for type: " + typeString, "<8:0>", type.lengthToString());
		type = PackageParser.parseType(typeString = "STD_LOGIC_VECTOR ( 10 - 3 DOWNTO 0 )", builder).type;
		assertFalse("Type is enum: " + typeString, type.isEnum());
		assertFalse("Type is signed: " + typeString, type.isSigned());
		e = null;
		try {
			assertEquals("Wrong cardinality for type: " + typeString, 0, type.getCardinality());
		} catch (RuntimeException e1) {
			e = e1;
		}
		assertNotNull("Exception not thrown when obtaining cardinality for: " + typeString, e);
		assertEquals("Wrong highestSB for type: " + typeString, 7, type.getHighestSB());
		assertEquals("Wrong offset for type: " + typeString, 0, type.getOffset());
		assertEquals("Wrong length printing for type: " + typeString, "<7:0>", type.lengthToString());

		/* ########################################################
		* ARRAY ((PROCESSOR_WIDTH -1) DOWNTO -1) OF STD_LOGIC;
		* ########################################################*/
		type = PackageParser.parseType(typeString = "ARRAY ( ( 9 - 1 ) DOWNTO -1 ) OF STD_LOGIC ;", builder).type;
		assertFalse("Type is enum: " + typeString, type.isEnum());
		assertFalse("Type is signed: " + typeString, type.isSigned());
		e = null;
		try {
			assertEquals("Wrong cardinality for type: " + typeString, 0, type.getCardinality());
		} catch (RuntimeException e1) {
			e = e1;
		}
		assertNotNull("Exception not thrown when obtaining cardinality for: " + typeString, e);
		assertEquals("Wrong highestSB for type: " + typeString, 9, type.getHighestSB()); /* ########################### !!!!!! #################### */
		assertEquals("Wrong offset for type: " + typeString, -1, type.getOffset()); /* ########################### !!!!!! #################### */
		assertEquals("Wrong length printing for type: " + typeString, "<9:0>", type.lengthToString());

		/* ##############
		* BIT or STD_LOGIC
		* ############## */
		type = PackageParser.parseType(typeString = "BIT", builder).type;
		assertFalse("Type is enum: " + typeString, type.isEnum());
		assertFalse("Type is signed: " + typeString, type.isSigned());
		e = null;
		try {
			assertEquals("Wrong cardinality for type: " + typeString, 0, type.getCardinality());
		} catch (RuntimeException e1) {
			e = e1;
		}
		assertNotNull("Exception not thrown when obtaining cardinality for: " + typeString, e);
		assertEquals("Wrong highestSB for type: " + typeString, 0, type.getHighestSB());
		assertEquals("Wrong offset for type: " + typeString, 0, type.getOffset());
		assertEquals("Wrong length printing for type: " + typeString, "<0:0>", type.lengthToString());
		type = PackageParser.parseType(typeString = "STD_LOGIC", builder).type;
		assertFalse("Type is enum: " + typeString, type.isEnum());
		assertFalse("Type is signed: " + typeString, type.isSigned());
		e = null;
		try {
			assertEquals("Wrong cardinality for type: " + typeString, 0, type.getCardinality());
		} catch (RuntimeException e1) {
			e = e1;
		}
		assertNotNull("Exception not thrown when obtaining cardinality for: " + typeString, e);
		assertEquals("Wrong highestSB for type: " + typeString, 0, type.getHighestSB());
		assertEquals("Wrong offset for type: " + typeString, 0, type.getOffset());
		assertEquals("Wrong length printing for type: " + typeString, "<0:0>", type.lengthToString());

		/* ##############
		*  BOOLEAN
		* ##############*/
		type = PackageParser.parseType(typeString = "BOOLEAN", builder).type;
		assertTrue("Type is enum: " + typeString, type.isEnum());
		assertFalse("Type is signed: " + typeString, type.isSigned());
		assertEquals("Wrong cardinality for type: " + typeString, 2, type.getCardinality());
		assertEquals("Wrong highestSB for type: " + typeString, 0, type.getHighestSB());
		assertEquals("Wrong offset for type: " + typeString, 0, type.getOffset());
		assertEquals("Wrong length printing for type: " + typeString, "<0:0>", type.lengthToString());

		/* ##############
		*  EXISTING
		* ##############*/
		//todo...

	}


}
