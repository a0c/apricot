package parsers.psl;

import io.scan.PSLScanner;
import io.scan.LexemeType;
import base.psl.structure.VerificationDirective;

/**
 * @author Anton Chepurov
 */
public class StructureParser {
	private static final String PROPERTY_DIRECTIVE_START = " : ";
	private static final String PROPERTY_NAME_REGEXP = "^" + LexemeType.IDENTIFIER.getRegexp() + PROPERTY_DIRECTIVE_START + ".*";
	static final String MISSING_NAME_TEXT = "Property NAME is not specified for the following property: ";
	static final String MISSING_DIRECTIVE_TEXT = "Property VERIFICATION DIRECTIVE is not specified for the following property: ";
	static final String INCORRECT_DIRECTIVE_1_TEXT = "UNKNOWN VERIFICATION DIRECTIVE (";
	static final String INCORRECT_DIRECTIVE_2_TEXT = ") is specified for the following property: ";
	static final String EMPTY_BODY_TEXT = "EMPTY BODY extracted for the following property: ";
	static final String PROPERTY_BODY_END = ";";

	private PSLScanner pslScanner;
	private StructureBuilder structureBuilder;


	public StructureParser(PSLScanner pslScanner, StructureBuilder structureBuilder) {
		this.pslScanner = pslScanner;
		this.structureBuilder = structureBuilder;
	}

	public void parse() throws Exception {
		String token;

		try {
			while ((token = pslScanner.next()) != null) {

				if (true/* todo: token is a property */) {

					structureBuilder.buildProperty(extractPropertyName(token), extractVerificationDirective(token), extractPropertyBody(token), token);

				} else if (false/* todo: token is a configuration*/) {
					/* todo: Extract the configuration name ... */
//                    pslStructureBuilder.buildConfiguration(token);
				}
			}


		} catch (Exception e) {
			pslScanner.close();
			throw e;
		}
	}

	static String extractPropertyBody(String propertyLine) throws Exception {
		String propertyBody = propertyLine.substring(getDirectiveEndIndex(propertyLine, getDirectiveStartIndex(propertyLine))).trim();
		if (propertyBody.endsWith(PROPERTY_BODY_END)) {
			propertyBody = propertyBody.substring(0, propertyBody.length() - 1).trim();
		}
		if (propertyBody.length() > 0) {
			return propertyBody;
		} else throw new Exception(EMPTY_BODY_TEXT + propertyLine);
	}

	static VerificationDirective extractVerificationDirective(String propertyLine) throws Exception {
		int directiveStartIndex = getDirectiveStartIndex(propertyLine);
		String potentialDirective = propertyLine.substring(directiveStartIndex, getDirectiveEndIndex(propertyLine, directiveStartIndex));
		for (VerificationDirective directive : VerificationDirective.values()) {
			if (potentialDirective.equalsIgnoreCase(directive.name())) {
				return directive;
			}
		}
		throw new Exception(INCORRECT_DIRECTIVE_1_TEXT + potentialDirective + INCORRECT_DIRECTIVE_2_TEXT + propertyLine);
	}

	private static int getDirectiveEndIndex(String propertyLine, int directiveStartIndex) throws Exception {
		int directiveEndIndex = propertyLine.indexOf(" ", directiveStartIndex);
		if (directiveEndIndex == -1) {
			throw new Exception(MISSING_DIRECTIVE_TEXT + propertyLine);
		}
		return directiveEndIndex;
	}

	private static int getDirectiveStartIndex(String propertyLine) throws Exception {
		checkForPropertyName(propertyLine);
		return propertyLine.indexOf(PROPERTY_DIRECTIVE_START) + PROPERTY_DIRECTIVE_START.length();
	}

	static String extractPropertyName(String propertyLine) throws Exception {
		checkForPropertyName(propertyLine);
		return propertyLine.substring(0, propertyLine.indexOf(PROPERTY_DIRECTIVE_START));
	}

	static void checkForPropertyName(String propertyLine) throws Exception {
		if (!propertyLine.matches(PROPERTY_NAME_REGEXP)) throw new Exception(MISSING_NAME_TEXT + propertyLine);
	}

}
