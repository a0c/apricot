package parsers.vhdl;

import io.scan.VHDLScanner;
import io.scan.VHDLToken;
import base.vhdl.structure.Package;
import base.Type;
import base.Indices;

import java.math.BigInteger;
import java.util.regex.Pattern;
import java.io.File;

import parsers.ExpressionBuilder;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 31.03.2009
 * <br>Time: 20:44:47
 */
public class PackageParser {

    private VHDLScanner scanner;

    private DefaultPackageBuilder builder = new DefaultPackageBuilder();

    public PackageParser(VHDLScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Parses VHDL package file with the specified <code>packageFilePath</code> and
     * adds it to the provided <code>builder</code>.
     * @param vhdlFile base VHDL file
     * @param packageFileName file path of the VHDL package file to parse
     * @param builder where to add the package structure 
     * @throws Exception if exception occurs while parsing the Package File
     */
    public static void parse(File vhdlFile, String packageFileName, VHDLStructureBuilder builder) throws Exception {
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
        builder.registerType(
                parseTypeName(vhdlLine),
                parseType(vhdlLine.substring(vhdlLine.indexOf(" IS ") + 4), (AbstractPackageBuilder) builder));
    }

    public Package getPackageStructure(){
        return builder.getPackageStructure();
    }

    /**
     * Parses <b>type</b> declaration;<br>
     * using specified builder builds constants to represent ENUMERATED type values<br>
     * and finally registers maximum value of the type in the specified builder.
     *
     * @param builder to build constants with/into and register max value for the type
     * @param vhdlLine type declaring line
     */
    public static void parseAndBuildTypeEnumDecl(PackageBuilder builder, String vhdlLine) {
        /* TYPE OPERAND_TYPE IS (OP_BYTE, OP_WORD, OP_DWORD, OP_QWORD); */        
        String typeName = parseTypeName(vhdlLine);
        int i = 0;
        for (String valueEnum : vhdlLine.substring(vhdlLine.indexOf("(") + 1, vhdlLine.indexOf(")")).split(",")) {
            builder.buildConstant(valueEnum.trim(), BigInteger.valueOf(i++));
        }
        builder.registerType(typeName, Type.createFromValues(i - 1, 0));
    }

    /**
     * Parses <b>constant</b> declaration and builds a constant using specified builder.
     *
     * @param builder to build constants with/into
     * @param vhdlLine constant declaring line
     * @throws Exception if the constant is not initialized with a value 
     */
    public static void parseAndBuildConstantDecl(PackageBuilder builder, String vhdlLine) throws Exception {
        /* Constant NAME */
        String name = vhdlLine.substring(8, vhdlLine.indexOf(":")).trim();
        /* Constant VALUE */
        String valueAsString = extractInitializationString(vhdlLine);
        if (valueAsString == null) throw new Exception("Constant " + name + " is not initialized");
        BigInteger valueInt = parseConstantValue(valueAsString);
        /* Create new CONSTANT */
        builder.buildConstant(name, valueInt);
    }

    public static String extractInitializationString(String value) {
        return value.contains(":=") && value.contains(";")
                ? value.substring(value.indexOf(":=") + 2, value.lastIndexOf(";")).trim()
                : null;
    }

    /**
     * Parses constant value from a String declaration taking radix into
     * account.<br>
     * If the line is enclosed in quotes (single/double), then radix = 2 is
     * applied. If the line is prefixed by "X" and enclosed in quotes
     * (single/double), then radix = 16 is applied. Otherwise radix = 10.
     * @param valueAsString constant-declaring line
     * @return BigInteger value of a constant declared with the specified String
     *         or <code>null</code> if the string doesn't declare a constant
     *         (for named constants <code>null</code> is returned as well).
     */
    public static BigInteger parseConstantValue(String valueAsString) {
        RadixEnum radix = RadixEnum.parseRadix(valueAsString);
        valueAsString = radix.trimConstantString(valueAsString);
        return radix.intValue(valueAsString);
    }

    /**
     * The same as {@link #parseConstantValue(String)} but also returns the length of the constant.
     * @param valueAsString constant-declaring line
     * @return BigInteger value of a constant declared with the specified String and the length of the constant,
     *         or <code>null</code> if the string doesn't declare a constant (for named constants <code>null</code>
     *         is returned as well).
     */
    public static ConstantValueAndLengthHolder parseConstantValueWithLength(String valueAsString) {
        RadixEnum radix = RadixEnum.parseRadix(valueAsString);
        valueAsString = radix.trimConstantString(valueAsString);
        BigInteger intValue = radix.intValue(valueAsString);
        return intValue == null ? null : new ConstantValueAndLengthHolder(intValue, radix.lengthFor(valueAsString));
    }

    public static String parseTypeName(String vhdlLine) {
        return vhdlLine.substring(5, vhdlLine.indexOf(" IS ")).trim();
    }

    public static Type parseType(String typeString, AbstractPackageBuilder builder) throws Exception {

        //todo: Here, use ExpressionBuilder to see, whether it produces OperandImpl or ExpressionImpl:
        //todo: Actually, no need for distinguishing between them: make AbstractOperand Interpretable!

        // Trim ';'
        if (typeString.endsWith(";")) {
            typeString = typeString.substring(0, typeString.length() - 1).trim();
        }

        if (typeString.contains(" RANGE ")) {
            /* INTEGER RANGE 32767 DOWNTO -32768 */
            /* INTEGER RANGE 0 TO 3 */
            Indices valueRange = builder.buildIndices(typeString.substring(typeString.indexOf(" RANGE ") + 7));

            return Type.createFromValues(valueRange); /*todo: , valueRange.isDescending() ? */   // todo: <== isDescenging() for #length#

        } else if ((typeString.startsWith("BIT_VECTOR ") || typeString.startsWith("STD_LOGIC_VECTOR ") || typeString.startsWith("UNSIGNED"))
                && ExpressionBuilder.bitRangePattern.matcher(typeString).matches()) {
            /* BIT_VECTOR ( 8 DOWNTO 0) */
            /* {IN} STD_LOGIC_VECTOR(MOD_EN_BITS-3 DOWNTO 0) */
            Indices indices = builder.buildIndices(typeString);

            return new Type(indices);

        } else if (typeString.startsWith("ARRAY ")) {
            /* ARRAY ((PROCESSOR_WIDTH -1) DOWNTO -1) OF STD_LOGIC; */
            /* Check subtype */
            int ofIndex = typeString.lastIndexOf(" OF ");
            String subType = typeString.substring(ofIndex + 4).trim();
            if (!(subType.equals("BIT") || subType.equals("STD_LOGIC")))
                throw new Exception("Unsupported type: \'" + typeString + "\'\n" +
                        "Only BIT and STD_LOGIC are supported as array subtypes.");
            /* Parse indices */
            Indices indices = builder.buildIndices(
                    ExpressionBuilder.trimEnclosingBrackets(typeString.substring(6, ofIndex)));
            //todo: signed? what does the last -1 mean here: "(PROCESSOR_WIDTH -1) DOWNTO -1"

            return new Type(indices);

        } else if (builder.containsType(typeString)) {
            return builder.getType(typeString);
        } else if (typeString.equals("BIT") || typeString.equals("STD_LOGIC")) {
            return new Type(Indices.BIT_INDICES);
        } else if (typeString.equals("BOOLEAN")) {
            return Type.createFromValues(1, 0);
        } else {
            throw new Exception("Unsupported type: \'" + typeString + "\'");
        }
    }


    enum RadixEnum {
        BINARY(2), BOOLEAN(2), HEXADECIMAL(16), DECIMAL(10);

        private static final Pattern HEX_PATTERN = Pattern.compile("^X \".+\"$");

        private final int radixAsInt;

        RadixEnum(int radixAsInt) {
            this.radixAsInt = radixAsInt;
        }

        BigInteger intValue(String valueAsString) {
            try {
                if (this == BOOLEAN) {
                    return isTrue(valueAsString) ? BigInteger.ONE : isFalse(valueAsString) ? BigInteger.ZERO : null;
                }
                return new BigInteger(valueAsString, radixAsInt);
//                return Integer.valueOf(valueAsString, radixAsInt);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        String trimConstantString(String variableString) {
            return trimConstantString(variableString, this);
        }

        static String trimConstantString(String variableString, RadixEnum radix) {
            switch (radix) {
                case BINARY:
                    return variableString.substring(1, variableString.length() - 1).trim();
                case DECIMAL:
                case BOOLEAN:
                    return variableString;
                default:
                    return variableString.substring(3, variableString.length() - 1).trim();
            }
        }

        static RadixEnum parseRadix(String valueAsString) {
            return isBinary(valueAsString) ? BINARY : isBoolean(valueAsString) ? BOOLEAN : isHex(valueAsString) ? HEXADECIMAL : DECIMAL;
        }

        private static boolean isBinary(String valueAsString) {
            return valueAsString.startsWith("\'") && valueAsString.endsWith("\'")
                        || valueAsString.startsWith("\"") && valueAsString.endsWith("\"");
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

        private Indices lengthFor(String valueAsString) {
            switch (this) {
                case BOOLEAN:
                    return Indices.BIT_INDICES;
                case BINARY:
                    return new Indices(valueAsString.length() - 1, 0);
                case DECIMAL:
                    return null;
                default:
                    /* HEXADECIMAL */
                    return new Indices(4 * valueAsString.length() - 1, 0);
            }
        }
    }

}
