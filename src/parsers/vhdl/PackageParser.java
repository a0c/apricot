package parsers.vhdl;

import base.Indices;
import base.Type;
import base.vhdl.structure.Package;
import io.scan.VHDLScanner;
import io.scan.VHDLToken;
import parsers.ExpressionBuilder;

import java.io.File;
import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * @author Anton Chepurov
 */
public class PackageParser {

	private static final Pattern OTHERS_0_PATTERN = Pattern.compile("^(\\( )?OTHERS => '0'( \\))?$");
	private static final Pattern OTHERS_1_PATTERN = Pattern.compile("^(\\( )?OTHERS => '1'( \\))?$");

	private VHDLScanner scanner;

	private DefaultPackageBuilder builder = new DefaultPackageBuilder();

	public PackageParser(VHDLScanner scanner) {
		this.scanner = scanner;
	}

	/**
	 * Parses VHDL package file with the specified <code>packageFilePath</code> and
	 * adds it to the provided <code>builder</code>.
	 *
	 * @param vhdlFile		base VHDL file
	 * @param packageFileName file path of the VHDL package file to parse
	 * @param builder		 where to add the package structure
	 * @throws Exception if exception occurs while parsing the Package File
	 */
	public static void parse(File vhdlFile, String packageFileName, StructureBuilder builder) throws Exception {
		/* Parse Package Structure */
		Package aPackage = Package.parsePackageStructure(new File(vhdlFile.getParent(), packageFileName + ".vhd"));
		/* Add package to builder */
		builder.addPackage(aPackage);
	}

	public void parse() throws Exception {
		VHDLToken token;

		while ((token = scanner.next()) != null) {
			String value = token.getValue();
			switch (token.getType()) {
				case CONSTANT_DECL:
					parseAndBuildConstantDecl(builder, value);
					break;
				case TYPE_ENUM_DECL:
					parseAndBuildTypeEnumDecl(builder, value);
					break;
				case TYPE_DECL:
					parseAndBuildTypeDecl(builder, value);
					break;
				case PACKAGE_DECL:
					/* Parse package NAME and build Package */
					builder.buildPackage(value.substring(8, value.lastIndexOf(" IS")).trim());
					break;
				case PACKAGE_BODY_DECL:
					/* Stop parser. */
					return;
			}
		}
	}

	public static void parseAndBuildTypeDecl(PackageBuilder builder, String vhdlLine) throws Exception {
		String name = parseTypeName(vhdlLine);
		Type type = null;
		try {
			type = parseTypeAndValue(vhdlLine.substring(vhdlLine.indexOf(" IS ") + 4), (AbstractPackageBuilder) builder).type;
		} catch (UnsupportedConstructException e) {
			System.out.println("Skipping unsupported type: " + e.getUnsupportedConstruct());
		}
		builder.registerType(name, type);
	}

	public Package getPackageStructure() {
		return builder.getPackageStructure();
	}

	/**
	 * Parses <b>type</b> declaration;<br>
	 * using specified builder builds constants to represent ENUMERATED type values<br>
	 * and finally registers maximum value of the type in the specified builder.
	 *
	 * @param builder  to build constants with/into and register max value for the type
	 * @param vhdlLine type declaring line
	 */
	public static void parseAndBuildTypeEnumDecl(PackageBuilder builder, String vhdlLine) {
		/* TYPE OPERAND_TYPE IS (OP_BYTE, OP_WORD, BLA_BLA1, BLA_BLA2); */
		String typeName = parseTypeName(vhdlLine);
		int i = 0;
		String[] enumValues = vhdlLine.substring(vhdlLine.indexOf("(") + 1, vhdlLine.indexOf(")")).split(",");
		Type type = Type.createFromValues(enumValues.length - 1, 0);
		for (String enumValue : enumValues) {
			builder.buildConstant(enumValue.trim(), type, BigInteger.valueOf(i++));
		}
		builder.registerType(typeName, type);
	}

	/**
	 * Parses <b>constant</b> declaration and builds a constant using specified builder.
	 *
	 * @param builder  to build constants with/into
	 * @param vhdlLine constant declaring line
	 * @throws Exception if the constant is not initialized with a value
	 */
	public static void parseAndBuildConstantDecl(AbstractPackageBuilder builder, String vhdlLine) throws Exception {
		/* Constant NAME */
		String name = vhdlLine.substring(8, vhdlLine.indexOf(":")).trim();
		/* Constant TYPE and VALUE*/
		TypeAndValueHolder typeAndValue = parseTypeAndValue(vhdlLine.substring(vhdlLine.indexOf(":") + 1).trim(), builder);
		if (typeAndValue.value == null) throw new Exception("Constant " + name + " is not initialized");

		/* Create new CONSTANT */
		builder.buildConstant(name, typeAndValue.type, typeAndValue.value);
	}

	public static String extractInitializationString(String value) {
		if (!value.contains(":=")) {
			return null;
		}
		if (value.endsWith(";"))
			return value.substring(value.indexOf(":=") + 2, value.lastIndexOf(";")).trim();
		else
			return value.substring(value.indexOf(":=") + 2).trim();
	}

	/**
	 * Parses constant value from a String declaration taking radix into
	 * account.<br>
	 * If the line is enclosed in quotes (single/double), then radix = 2 is
	 * applied. If the line is prefixed by "X" and enclosed in quotes
	 * (single/double), then radix = 16 is applied. Otherwise radix = 10.
	 *
	 * @param valueAsString constant-declaring line
	 * @return BigInteger value of a constant declared with the specified String
	 *         or <code>null</code> if the string doesn't declare a constant
	 *         (for named constants <code>null</code> is returned as well).
	 */
	public static BigInteger parseConstantValue(String valueAsString) {
		ConstantValueAndLengthHolder varHolder = parseConstantValueWithLength(valueAsString);
		return varHolder == null ? null : varHolder.getValue();
	}

	/**
	 * The same as {@link #parseConstantValue(String)} but also returns the length of the constant.
	 *
	 * @param valueAsString constant-declaring line
	 * @return BigInteger value of a constant declared with the specified String and the length of the constant,
	 *         or <code>null</code> if the string doesn't declare a constant (for named constants <code>null</code>
	 *         is returned as well).
	 */
	public static ConstantValueAndLengthHolder parseConstantValueWithLength(String valueAsString) {

		if (OTHERS_0_PATTERN.matcher(valueAsString).matches()) {
			return new ConstantValueAndLengthHolder(BigInteger.valueOf(0), null);
		}

		RadixEnum radix = RadixEnum.parseRadix(valueAsString);
		if (radix == null) {
			return null;
		}
		valueAsString = radix.trimConstantString(valueAsString);
		BigInteger intValue = radix.intValue(valueAsString);
		return intValue == null ? null : new ConstantValueAndLengthHolder(intValue, radix.lengthFor(valueAsString));
	}

	public static String parseTypeName(String vhdlLine) {
		return vhdlLine.substring(5, vhdlLine.indexOf(" IS ")).trim();
	}

	public static TypeAndValueHolder parseTypeAndValue(String type, String value, StructureBuilder builder) throws Exception {
		return parseTypeAndValue(type + " := " + value, builder);
	}

	public static TypeAndValueHolder parseTypeAndValue(String typeAndValue, AbstractPackageBuilder builder) throws Exception {

		//todo: Here, use ExpressionBuilder to see, whether it produces OperandImpl or ExpressionImpl:
		//todo: Actually, no need for distinguishing between them: make AbstractOperand Interpretable!

		// Trim ';'
		if (typeAndValue.endsWith(";")) {
			typeAndValue = typeAndValue.substring(0, typeAndValue.length() - 1).trim();
		}

		/* Initialization VALUE */
		String valueAsString = extractInitializationString(typeAndValue);
		typeAndValue = extractTypeString(typeAndValue);

		Type type;
		if (typeAndValue.startsWith("ARRAY ")) {
			/* ARRAY ((PROCESSOR_WIDTH -1) DOWNTO -1) OF STD_LOGIC; */
			/* todo: ARRAY ( NATURAL RANGE <> ) OF STD_LOGIC_VECTOR ( 31 DOWNTO 0 ) ; */
			/* Check subtype */
			int ofIndex = typeAndValue.lastIndexOf(" OF ");
			String subType = typeAndValue.substring(ofIndex + 4).trim();
			if (!(subType.equals("BIT") || subType.equals("STD_LOGIC")))
				throw new UnsupportedConstructException("Unsupported type: \'" + typeAndValue + "\'\n" +
						"Only BIT and STD_LOGIC are supported as array subtypes.", typeAndValue);
			/* Parse indices */
			Indices indices = builder.buildIndices(
					ExpressionBuilder.trimEnclosingBrackets(typeAndValue.substring(6, ofIndex)));
			//todo: signed? what does the last -1 mean here: "(PROCESSOR_WIDTH -1) DOWNTO -1"

			type = new Type(indices);

		} else if (typeAndValue.contains(" RANGE ")) {
			/* INTEGER RANGE 32767 DOWNTO -32768 */
			/* INTEGER RANGE 0 TO 3 */
			Indices valueRange = builder.buildIndices(typeAndValue.substring(typeAndValue.indexOf(" RANGE ") + 7));

			type = Type.createFromValues(valueRange);/*todo: , valueRange.isDescending() ? */   // todo: <== isDescending() for #length#

		} else if ((typeAndValue.startsWith("BIT_VECTOR ") || typeAndValue.startsWith("STD_LOGIC_VECTOR ") || typeAndValue.startsWith("UNSIGNED"))
				&& ExpressionBuilder.BIT_RANGE_PATTERN.matcher(typeAndValue).matches()) {
			/* BIT_VECTOR ( 8 DOWNTO 0) */
			/* {IN} STD_LOGIC_VECTOR(MOD_EN_BITS-3 DOWNTO 0) */
			Indices indices = builder.buildIndices(typeAndValue);

			type = new Type(indices);

		} else if (builder.containsType(typeAndValue)) {
			type = builder.getType(typeAndValue);
		} else if (typeAndValue.equals("BIT") || typeAndValue.equals("STD_LOGIC")) {
			type = Type.BIT_TYPE;
		} else if (typeAndValue.equals("BOOLEAN")) {
			type = Type.BOOLEAN_TYPE;
		} else if (typeAndValue.equals("INTEGER") || typeAndValue.equals("NATURAL")) {
			BigInteger valueInt = valueAsString == null ? null : parseConstantValue(valueAsString);
			if (valueInt == null) {
				throw new UnsupportedConstructException("Unconstrained type " + typeAndValue + " is not synthesizable.\n" +
						"Cannot derive the length of the variable.", typeAndValue);
			}
			type = Type.createFromValues(valueInt.intValue(), 0);
		} else {
			throw new UnsupportedConstructException("Unsupported type: \'" + typeAndValue + "\'", typeAndValue);
		}

		valueAsString = replaceOthersValue(valueAsString, type.getLength());

		BigInteger valueInt = valueAsString == null ? null : parseConstantValue(valueAsString);

		return new TypeAndValueHolder(type, valueInt, valueAsString);
	}

	public static String extractTypeString(String typeString) {
		if (typeString.contains(":=")) {
			typeString = typeString.substring(0, typeString.lastIndexOf(":=")).trim();
		}
		return typeString;
	}

	public static String replaceOthersValue(String valueAsString, Indices length) {

		if (valueAsString == null) {
			return null;
		}

		char bit;
		if (OTHERS_0_PATTERN.matcher(valueAsString).matches()) {
			bit = '0';
		} else if (OTHERS_1_PATTERN.matcher(valueAsString).matches()) {
			bit = '1';
		} else {
			return valueAsString;
		}

		/* Create a new String and fill it with 0 or 1 */
		StringBuilder sb = new StringBuilder(length.length());
		sb.append("\"");
		for (int i = 0; i < length.length(); i++) { /*length + 1*/
			sb.append(bit);
		}
		sb.append("\"");
		return sb.toString();
	}

	public static boolean isOthers(String valueAsString) {
		return OTHERS_0_PATTERN.matcher(valueAsString).matches() || OTHERS_1_PATTERN.matcher(valueAsString).matches();
	}

	enum RadixEnum {
		BINARY(2), BOOLEAN(2), HEXADECIMAL(16), ARBITRARY(-1), DECIMAL(10);

		private static final Pattern BIN_PATTERN = Pattern.compile("(\'[01]+\')|(\"[01]+\")");
		private static final Pattern HEX_PATTERN = Pattern.compile("^X \" ?[0-9a-f]+ ?\"$", Pattern.CASE_INSENSITIVE);
		private static final Pattern BASED_PATTERN = Pattern.compile("^\\d+ # [\\._0-9a-f]+ #( E\\+?[0-9]+)?$", Pattern.CASE_INSENSITIVE);
		private static final Pattern DEC_PATTERN = Pattern.compile("-?\\d+");

		private final int radixAsInt;

		RadixEnum(int radixAsInt) {
			this.radixAsInt = radixAsInt;
		}

		BigInteger intValue(String valueAsString) {
			try {
				if (this == BOOLEAN) {
					return isTrue(valueAsString) ? BigInteger.ONE : isFalse(valueAsString) ? BigInteger.ZERO : null;
				}
				if (this == ARBITRARY) {
					String[] parts = valueAsString.split("#");
					if (parts.length == 2) {
						if (parts[1].contains(".")) {
							throw new RuntimeException("Decimals are not supported in Based Literal (" + valueAsString + "). Missing implementation...");
						}
						int base = Integer.parseInt(parts[0].trim());
						return new BigInteger(parts[1].replaceAll("_", "").trim(), base);
					} else
						throw new RuntimeException("Exponent is not supported in Based Literal (" + valueAsString + "). Missing implementation...");
				}
				return new BigInteger(valueAsString, radixAsInt);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		String trimConstantString(String variableString) {
			switch (this) {
				case BINARY:
					return variableString.substring(1, variableString.length() - 1).trim();
				case DECIMAL:
				case BOOLEAN:
				case ARBITRARY:
					return variableString;
				default:
					return variableString.substring(3, variableString.length() - 1).trim();
			}
		}

		static RadixEnum parseRadix(String valueAsString) {
			return isBinary(valueAsString) ? BINARY :
					isBoolean(valueAsString) ? BOOLEAN :
							isHex(valueAsString) ? HEXADECIMAL :
									isBased(valueAsString) ? ARBITRARY :
											isDecimal(valueAsString) ? DECIMAL : null;
		}

		private static boolean isBinary(String valueAsString) {
			return BIN_PATTERN.matcher(valueAsString).matches();
		}

		private static boolean isBoolean(String valueAsString) {
			return isTrue(valueAsString) || isFalse(valueAsString);
		}

		private static boolean isFalse(String valueAsString) {
			return valueAsString.equalsIgnoreCase("false");
		}

		private static boolean isTrue(String valueAsString) {
			return valueAsString.equalsIgnoreCase("true");
		}

		private static boolean isHex(String valueAsString) {
			return HEX_PATTERN.matcher(valueAsString).matches();
		}

		private static boolean isBased(String valueAsString) {
			return BASED_PATTERN.matcher(valueAsString).matches();
		}

		private static boolean isDecimal(String valueAsString) {
			return DEC_PATTERN.matcher(valueAsString).matches();
		}

		private Indices lengthFor(String valueAsString) {
			switch (this) {
				case BOOLEAN:
					return Indices.BIT_INDICES;
				case BINARY:
					return new Indices(valueAsString.length() - 1, 0);
				case DECIMAL:
				case ARBITRARY:
					return null;
				default:
					/* HEXADECIMAL */
					return new Indices(4 * valueAsString.length() - 1, 0);
			}
		}
	}
}
