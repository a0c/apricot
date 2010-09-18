package parsers.vhdl;

import base.SourceLocation;
import io.scan.VHDLScanner;
import io.scan.VHDLToken;

import java.util.*;
import java.math.BigInteger;

import base.Type;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 08.02.2008
 * <br>Time: 11:30:06
 */
public class VHDLStructureParser {

    private VHDLScanner scanner;

    private VHDLStructureBuilder builder;

    public VHDLStructureParser(VHDLScanner scanner, VHDLStructureBuilder builder) {
        this.scanner = scanner;
        this.builder = builder;
    }

    public void parse() throws Exception {
        VHDLToken token;
        String value, name, conditionString, valueString;
        Type type;
        BigInteger valueInt;
        boolean isInput;

        while ((token = scanner.next()) != null) {
            value = token.getValue();

			SourceLocation source = scanner.getCurrentSource();

            switch (token.getType()) {
                case USE_DECL:
					//todo: Fix package loading. For this use crc_half_modified.vhd and crc_pkg.vhd from C:\Documents and Settings\Randy\Desktop\TTU temp\Elsevier IST paper\Designs\CRC_41\CRC\
					//todo: After fixing, uncomment the following.
//                    /* Split to {library_name, package_name, all} */
//                    String[] packageParts = value.substring(4, value.lastIndexOf(";")).trim().split("\\.");
//                    if (packageParts.length == 3) {
//                        /* USE LIBRARY_NAME.PACKAGE_NAME.ALL */
//                        /* Process WORK library (current working directory)
//                        * and skip IEEE library and others... */
//                        if (packageParts[0].equalsIgnoreCase("WORK")) {
//                            PackageParser.parse(scanner.getSourceFile(), packageParts[1], builder);
//                        } else continue;
//                    } else if (packageParts.length == 2) {
//                        /* USE PACKAGE_NAME.ALL; */
//                        PackageParser.parse(scanner.getSourceFile(), packageParts[0], builder);
//                    } else throw new Exception("Malformed USE PACKAGE declaration: \"" + value + "\"");
                    break;
                case ENTITY_DECL:
                    name = value.split("\\s")[1];
                    builder.buildEntity(name);
                    break;
                case GENERIC_OPEN:
                case PORT_OPEN:
                case BEGIN:
                    /* Do nothing for GENERIC and PORT opening and BEGIN */
                    break;
                case GENERIC_DECL:
                    VHDLToken[] tokens = splitMultipleDeclaration(token);
                    for (VHDLToken aToken : tokens) {
                        value = aToken.getValue();
                        /* Generic NAME */
                        name = value.substring(0, value.indexOf(":")).trim();
                        /* Generic VALUE */
                        String valueAsString = PackageParser.extractInitializationString(value);
                        if (valueAsString == null) throw new Exception("Generic constant " + name + " is not initialized");
                        valueInt = PackageParser.parseConstantValue(valueAsString);
                        builder.buildGeneric(name, valueInt);
                    }
                    break;
                case PORT_DECL:
                    tokens = splitMultipleDeclaration(token);
                    for (VHDLToken aToken : tokens) {
                        value = aToken.getValue();
                        /* Port NAME */
                        name = value.substring(0, value.indexOf(":")).trim();
                        /* Port IN/OUT */
                        String[] typeString = value.substring(value.indexOf(":") + 1).trim().split("\\s", 2);
                        isInput = typeString[0].trim().equals("IN");
                        /* Port HIGHEST SIGNIFICANT BIT */
//                        highestSB = extractHSB(typeString[1].trim());
                        type = PackageParser.parseType(typeString[1].trim(), builder);
                        /* Create new PORT */
                        builder.buildPort(name, isInput, type);
                    }
                    break;
                case DECL_CLOSE:
                    builder.buildCloseDeclaration();
                    break;
                case ARCHITECTURE_DECL:
                    name = value.substring(13, value.indexOf(" OF ")).trim();
                    String affiliation = value.substring(value.indexOf(" OF ") + 4, value.indexOf(" IS")).trim();
                    builder.buildArchitecture(name, affiliation);
                    break;
                case TYPE_ENUM_DECL:
                    PackageParser.parseAndBuildTypeEnumDecl(builder, value);
                    break;
                case CONSTANT_DECL:
                    PackageParser.parseAndBuildConstantDecl(builder, value);
                    break;
                case TYPE_DECL:
                    /* TYPE N_STDLV IS ARRAY ((PROCESSOR_WIDTH -1) DOWNTO -1) OF STD_LOGIC; */
                    PackageParser.parseAndBuildTypeDecl(builder, value);
//                    name = PackageParser.parseTypeName(value);
//                    builder.registerType(name, parseType(value.substring(value.indexOf(" IS ") + 4)));
                    break;
                case SIGNAL_DECL:
                    tokens = splitMultipleDeclaration(token);
                    for (VHDLToken aToken : tokens) {
                        value = aToken.getValue();
                        /* Signal NAME */
                        name = value.substring(6, value.indexOf(":")).trim();
                        /* Signal HIGHEST SIGNIFICANT BIT */
//                        highestSB = extractHSB(value.substring(value.indexOf(":") + 1).trim());
                        type = PackageParser.parseType(value.substring(value.indexOf(":") + 1).trim(), builder);
                        /* Create new SIGNAL */
                        builder.buildSignal(name, type);
                    }
                    break;
                case PROCESS_DECL:
                    String processName = value.contains(":") ? value.substring(0, value.indexOf(":")).trim() : null;
                    builder.buildProcess(processName, extractSensitivityList(value));
                    break;
                case VARIABLE_DECL:
                    tokens = splitMultipleDeclaration(token);
                    for (VHDLToken aToken : tokens) {
                        value = aToken.getValue();
                        /* Variable NAME */
                        name = value.substring(8, value.indexOf(":")).trim();
                        /* Variable HIGHEST SIGNIFICANT BIT */
//                        highestSB = extractHSB(value.substring(value.indexOf(":") + 1).trim());
                        type = PackageParser.parseType(value.substring(value.indexOf(":") + 1).trim(), builder);
                        /* Create new VARIABLE */
                        builder.buildVariable(name, type);
                    }
                    break;
                case IF_STATEMENT:
                    /* If CONDITION */
                    conditionString = value.substring(2, value.indexOf(" THEN")).trim();
                    /* Create new IF_STATEMENT */
                    builder.buildIfStatement(conditionString, source);
                    break;
                case TRANSITION:
                    /* Variable NAME and VALUE */
                    if (value.contains(":=")) {
                        name = value.substring(0, value.indexOf(":=")).trim();
                        valueString = PackageParser.extractInitializationString(value);
                    } else if (value.contains("<=")) {
                        name = value.substring(0, value.indexOf("<=")).trim();
                        valueString = value.substring(value.indexOf("<=") + 2, value.indexOf(";")).trim();
                    } else {
                        name = "";
                        valueString = "NULL";
                    }

                    if (valueString.contains(" WHEN ") && valueString.contains(" ELSE ")) {
                        /* ##### WHEN-ELSE outside PROCESSES (in ARCHITECTURE) */
                        // Split to parts
						String[] varValueParts = valueString.split("(( WHEN )|( ELSE ))"); //todo: use "\\bWHEN\\b" word boundary!
						int maxIdx = varValueParts.length - 1;
						for (int i = 0; i < varValueParts.length; i+=2) {
							int conditionIdx = i + 1;
							if (conditionIdx <= maxIdx) {
								if (i == 0) {
									//todo: substitute current lines with calculated lines, for a finer-grained location...
									builder.buildIfStatement(varValueParts[conditionIdx], source);
								} else {
									builder.buildElsifStatement(varValueParts[conditionIdx], source);
								}
							} else {
								builder.buildElseStatement();
							}
							//todo: substitute current lines with calculated lines...
							builder.buildTransition(name, varValueParts[i], source);
						}
						builder.buildCloseDeclaration();
                    } else {
                        /* Create new TRANSITION */
                        builder.buildTransition(name, valueString, source);
                    }
                    break;
                case ELSIF_STATEMENT:
                    /* If CONDITION */
                    conditionString = value.substring(5, value.indexOf(" THEN")).trim();
                    /* Create new ELSIF_STATEMENT */
                    builder.buildElsifStatement(conditionString, source);
                    break;
                case ELSE:
                    builder.buildElseStatement();
                    break;
                case CASE_STATEMENT:
                    /* Variable NAME */
                    name = value.substring(4, value.indexOf(" IS")).trim();
                    /* Create new CASE_STATEMENT */
                    builder.buildCaseStatement(name, source);
                    break;
                case WHEN_STATEMENT:
                    /* CONDITION */
                    conditionString = value.substring(4, value.indexOf(" =>")).trim();
                    String[] conditionStrings = conditionString.split(" ?\\| ?");
                    for (int i = 0; i < conditionStrings.length; i++) conditionStrings[i] = conditionStrings[i].trim();
                    builder.buildWhenStatement(conditionStrings);
                    break;
                default:
                    System.out.println("Unknown TOKEN is met: \"" + value + "\"");
            }

        }

    }

    static Collection<String> extractSensitivityList(String processDeclString) {
        Collection<String> sensitivityList = new LinkedList<String>();
        if (processDeclString.contains("(") && processDeclString.contains(")")) {
            String sensListString = processDeclString.substring(processDeclString.indexOf("(") + 1, processDeclString.indexOf(")"));
            String[] sensListArray = sensListString.split(",");
            for (String signal : sensListArray) {
                signal = signal.trim();
                if (signal.length() > 0) {
                    sensitivityList.add(signal);
                }
            }
        }
        return sensitivityList;
    }

    /**
     * Calculates highest sighificant bit for the given type.
     * A minus sign is appended to the returned value of HSB,
     * if the variable is SIGNED.
     *
     * @param typeString where to extract the HSB from
     * @return  highest significant bit required to represent
     *          the value given in <code>typeString</code>
     *          with a minus sign appended if the value is
     *          SIGNED.
     * @throws Exception    if unsupported type is met or
     *                      exception occurred while parsing
     *                      index to INT.
     */
//    int extractHSB(String typeString) throws Exception {
//        int highestSB = 0;
//        boolean signed = false;
//
//        //todo: Here, use ExpressionBuilder to see, whether it produces OperandImpl or ExpressionImpl:
//        //todo: Actually, no need for distinguishing between them: make AbstractOperand Interpretable!
//
//        // Trim ';'
//        if (typeString.endsWith(";")) {
//            typeString = typeString.substring(0, typeString.length() - 1).trim();
//        }
//
//        if (typeString.contains(" RANGE ")) {
//            /* INTEGER RANGE 32767 DOWNTO -32768 */
//            /* INTEGER RANGE 0 TO 3 */
//            Indices indices = builder.buildIndices(typeString.substring(typeString.indexOf(" RANGE ") + 7));
//
//            // Calculate the LENGTH of the register to store the max. possible value of the variable
//            highestSB = PackageParser.calcRegLengthForValue(indices.getHighest(), indices.getLowest());
//            // Decide whether the value holder must be SIGNED or UNSIGNED
//            if (indices.getLowest() < 0) signed = true;
//
//        } else if ((typeString.startsWith("BIT_VECTOR ") || typeString.startsWith("STD_LOGIC_VECTOR ") || typeString.startsWith("UNSIGNED"))
//                && ExpressionBuilder.bitRangePattern.matcher(typeString).matches()) {
//            /* BIT_VECTOR ( 8 DOWNTO 0) */
//            /* {IN} STD_LOGIC_VECTOR(MOD_EN_BITS-3 DOWNTO 0) */
//            Indices indices = builder.buildIndices(typeString);
//            /* LENGTH of bus */
//            highestSB = indices.getHighest() - indices.getLowest();
//
//        } else if (typeString.startsWith("ARRAY ")) {
//            /* ARRAY ((PROCESSOR_WIDTH -1) DOWNTO -1) OF STD_LOGIC; */
//            /* Check subtype */
//            int ofIndex = typeString.lastIndexOf(" OF ");
//            String subType = typeString.substring(ofIndex + 4).trim();
//            if (!(subType.equals("BIT") || subType.equals("STD_LOGIC")))
//                throw new Exception("Unsupported type: \'" + typeString + "\'\n" +
//                        "Only BIT and STD_LOGIC are supported as array subtypes.");
//            /* Parse indices */
//            Indices indices = builder.buildIndices(
//                    ExpressionBuilder.trimEnclosingBrackets(typeString.substring(6, ofIndex)));
//            //todo: signed? what does the last -1 mean here: "(PROCESSOR_WIDTH -1) DOWNTO -1"
//            /* LENGTH of bus */
//            highestSB = indices.getHighest() - indices.getLowest();
//
//        } else if (builder.containsType(typeString)) {
//            highestSB = builder.getType(typeString);
//        } else {
//            if (!(typeString.equals("BIT") || typeString.equals("STD_LOGIC") || typeString.equals("BOOLEAN")))
//                throw new Exception("Unsupported type: \'" + typeString + "\'");
//            // If BIT, STD_LOGIC or BOOLEAN, then do nothing (leave highestSB = 0;)
//        }
//
//        return signed ? -highestSB : highestSB;
//
//    }

    /**
     * Splits multiple declaration of PORTS, VARIABLES, CONSTANTS, SIGNALS and
     * GENERICS into an array of single declarations.
     *
     * @param token a token which declares N (1, 2 etc) variables/constants/signals
     * @return an array of Tokens that contains N single variable/constant/signal declarations.
     * @throws Exception if token to be splitted doesn't declare neither PORT,
     *         nor VARIABLE, nor CONSTANT, nor SIGNAL and nor GENERIC
     */
    private VHDLToken[] splitMultipleDeclaration(VHDLToken token) throws Exception {

        /* Declarations of PORT, VARIABLE, CONSTANT, SIGNAL or GENERIC are accepted only */
        VHDLToken.Type type = token.getType();
        if (!(type == VHDLToken.Type.PORT_DECL || type == VHDLToken.Type.VARIABLE_DECL
                || type == VHDLToken.Type.CONSTANT_DECL || type == VHDLToken.Type.SIGNAL_DECL
                || type == VHDLToken.Type.GENERIC_DECL)) {
            throw new Exception("Declaration of PORT, VARIABLE, CONSTANT, SIGNAL " +
                    "or GENERIC are only accepted for splitting." +
                    "\nIncompatible token type: " + type + "\nToken value: " + token.getValue());
        }

        /* Cut off optional "SIGNAL" modifyer from PORT declaration */
        if (type == VHDLToken.Type.PORT_DECL) {
            if (token.getValue().matches("SIGNAL .+:.*")) {
                token = new VHDLToken(VHDLToken.Type.PORT_DECL, token.getValue().substring(6).trim());
            }
        }

        if (!token.getValue().contains(",")) {
            /* Return simple declaration as is */
            return new VHDLToken[]{token};
        } else {
            /* Split multiple declarations and return an array of them */
            String multipleDeclaration = token.getValue();
            ArrayList<String> varNames = new ArrayList<String>();

            String modifier = "";

            /* Cut off 'VARIABLE', 'SIGNAL' etc... */
            if (type == VHDLToken.Type.VARIABLE_DECL) modifier = "VARIABLE";
            else if (type == VHDLToken.Type.CONSTANT_DECL) modifier = "CONSTANT";
            else if (type == VHDLToken.Type.SIGNAL_DECL) modifier = "SIGNAL";
            multipleDeclaration = multipleDeclaration.substring(modifier.length()).trim();

            /* Extract variable NAMES */
            String[] varNamesString = multipleDeclaration.substring(0, multipleDeclaration.indexOf(":")).split(",");
            for (String varName : varNamesString) {
                varNames.add(varName.trim());
            }

            /* Extract variable TYPE */
            String typeString = " " + multipleDeclaration.substring(multipleDeclaration.indexOf(":"));

            /* Compose a list of single declarations */
            VHDLToken[] declarationsArray = new VHDLToken[varNames.size()];
            modifier += modifier.equals("") ? "" : " ";
            for (int i = 0; i < declarationsArray.length; i++) {
                declarationsArray[i] = new VHDLToken(type, modifier + varNames.get(i) + typeString);
            }

            return declarationsArray;
        }
    }

    /**
     * <b>NB! {@link PackageParser#parseConstantValue(String)} should be used instead.
     * <br>HEX, DEC, BIN are not considered here.</b>
     * @param variableAsString line to check
     * @return whether the line declares a constant
     */
    @Deprecated
    public static boolean isConstant(String variableAsString) {
        return Character.isDigit(variableAsString.charAt(0));
    }

}
