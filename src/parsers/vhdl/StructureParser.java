package parsers.vhdl;

import base.SourceLocation;
import io.scan.VHDLScanner;
import io.scan.VHDLToken;

import java.text.ParseException;
import java.util.*;

import base.Type;

/**
 * @author Anton Chepurov
 */
public class StructureParser {

	private VHDLScanner scanner;

	private StructureBuilder builder;

	public StructureParser(VHDLScanner scanner, StructureBuilder builder) {
		this.scanner = scanner;
		this.builder = builder;
	}

	public void parse() throws Exception {
		VHDLToken token;
		String value, name, conditionString, valueString;
		Type type;
		TypeAndValueHolder typeAndValue;
		boolean isInput;

		while ((token = scanner.next()) != null) {
			value = token.getValue();

			SourceLocation source = scanner.getCurrentSource();

			switch (token.getType()) {
				case USE_DECL:
					//todo: Fix package loading. For this use crc_half_modified.vhd and crc_pkg.vhd from C:\Documents and Settings\Randy\Desktop\TTU temp\Elsevier IST paper\Designs\CRC_41\CRC\
					/* Split to {library_name, package_name, all} */
					String[] packageParts = value.substring(4, value.lastIndexOf(";")).trim().split("\\.");
					if (packageParts.length == 3) {
						/* USE LIBRARY_NAME.PACKAGE_NAME.ALL */
						/* Process WORK library (current working directory)
						* and skip IEEE library and others... */
						if (packageParts[0].equalsIgnoreCase("WORK")) {
							PackageParser.parse(scanner.getSourceFile(), packageParts[1], builder);
						} else continue;
					} else if (packageParts.length == 2) {
						/* USE PACKAGE_NAME.ALL; */
						PackageParser.parse(scanner.getSourceFile(), packageParts[0], builder);
					} else throw new Exception("Malformed USE PACKAGE declaration: \"" + value + "\"");
					break;
				case ENTITY_DECL:
					name = value.split("\\s")[1];
					builder.buildEntity(name);
					break;
				case COMPONENT_DECL:
					name = value.split("\\s")[1];
					builder.buildComponentDeclaration(name, scanner.getSourceFile());
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
						/* Generic TYPE and VALUE*/
						typeAndValue = PackageParser.parseType(value.substring(value.indexOf(":") + 1).trim(), builder);
						if (typeAndValue.value == null)
							throw new Exception("Generic constant " + name + " is not initialized");
						builder.buildGeneric(name, typeAndValue.type, typeAndValue.value);
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
						type = PackageParser.parseType(typeString[1].trim(), builder).type;
						/* Create new PORT */
						builder.buildPort(name, isInput, type);
					}
					break;
				case PORT_MAP:
					String[] valueParts = value.split(":|(PORT MAP)", 3);
					if (valueParts.length != 3)
						throw new Exception("Sorry, don't know how to parse this port mapping: " + value);
					name = valueParts[0].trim();
					String pm = valueParts[2].trim();
					String[] portMappings = pm.substring(pm.indexOf("(") + 1, pm.lastIndexOf(")")).split(",");
					List<Map.Entry<String, String>> portMappingEntries = new ArrayList<Map.Entry<String, String>>();
					for (String portMapping : portMappings) {
						if (!portMapping.contains("=>")) {
							//todo POSITIONAL ASSOCIATION in port map
							throw new ParseException("Implementation is missing for POSITIONAL ASSOCIATION in port map "
									+ Arrays.toString(portMappings), 0);
						}
						String[] parts = portMapping.split("=>", 2);
						if (parts.length != 2) {
							throw new ParseException("Invalid port mapping: " + portMapping, 0);
						}
						portMappingEntries.add(new AbstractMap.SimpleImmutableEntry<String, String>(parts[0].trim(), parts[1].trim()));
					}
					builder.buildComponentInstantiation(name, valueParts[1].trim(), portMappingEntries);
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
					break;
				case SIGNAL_DECL:
					tokens = splitMultipleDeclaration(token);
					for (VHDLToken aToken : tokens) {
						value = aToken.getValue();
						/* Signal NAME */
						name = value.substring(6, value.indexOf(":")).trim();
						/* Signal HIGHEST SIGNIFICANT BIT */
						typeAndValue = PackageParser.parseType(value.substring(value.indexOf(":") + 1).trim(), builder);
						/* Create new SIGNAL */
						builder.buildSignal(name, typeAndValue.type, typeAndValue.valueAsString);
					}
					break;
				case ALIAS:
					int colonIndex = value.indexOf(":");
					int isIndex = value.indexOf(" IS ");
					name = value.substring(6, colonIndex).trim();
					type = PackageParser.parseType(value.substring(colonIndex + 1, isIndex).trim(), builder).type;
					value = value.substring(isIndex + 4, value.length() - 1);
					builder.buildAlias(name, type, value);
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
						type = PackageParser.parseType(value.substring(value.indexOf(":") + 1).trim(), builder).type;
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
						for (int i = 0; i < varValueParts.length; i += 2) {
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
				case WITH:
					/* Variable NAME */
					int selectIdx = value.indexOf(" SELECT ");
					name = value.substring(5, selectIdx).trim();
					builder.buildCaseStatement(name, source); //todo: SourceLocation for WHEN
					/* CONDITIONS */
					String[] choicesAndAssigns = value.substring(selectIdx + 8, value.lastIndexOf(";")).split("( WHEN )|(,)");
					String target = null;
					for (int i = 0; i < choicesAndAssigns.length - 1; i += 2) {
						String transition = choicesAndAssigns[i].trim();
						String[] conditions = choicesAndAssigns[i + 1].split(" ?\\| ?");
						for (int j = 0; j < conditions.length; j++) conditions[j] = conditions[j].trim();
						builder.buildWhenStatement(conditions);

						if (target == null) {
							String[] targetAndValue = transition.split(" <= ?");
							target = targetAndValue[0].trim();
							builder.buildTransition(target, targetAndValue[1].trim(), source);
						} else {
							builder.buildTransition(target, transition, source);
						}
					}
					builder.buildCloseDeclaration();
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
	 * Splits multiple declaration of PORTS, VARIABLES, CONSTANTS, SIGNALS and
	 * GENERICS into an array of single declarations.
	 *
	 * @param token a token which declares N (1, 2 etc) variables/constants/signals
	 * @return an array of Tokens that contains N single variable/constant/signal declarations.
	 * @throws Exception if token to be split doesn't declare neither PORT,
	 * 					 nor VARIABLE, nor CONSTANT, nor SIGNAL and nor GENERIC
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

		/* Cut off optional "SIGNAL" modifier from PORT declaration */
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
}
