package parsers.psl;

import ee.ttu.pld.apricot.DetectionException;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import base.psl.structure.VerificationDirective;
import helpers.PSLProperties;

/**
 * @author Anton Chepurov
 */
public class PSLStructureParserTest {

	@Test(expected = DetectionException.class)
	public void checkForPropertyNameFails() throws DetectionException {
		boolean first = false, second = false, third = false;
		try {
			PSLStructureParser.checkForPropertyName("m:issing_name;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(PSLStructureParser.MISSING_NAME_TEXT)) {
				first = true;
			}
		}
		try {
			PSLStructureParser.checkForPropertyName("m: issing_name;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(PSLStructureParser.MISSING_NAME_TEXT)) {
				second = true;
			}
		}
		try {
			PSLStructureParser.checkForPropertyName("m :issing_name;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(PSLStructureParser.MISSING_NAME_TEXT)) {
				third = true;
			}
		}
		if (first && second && third) {
			throw new DetectionException();
		}
	}

	@Test(expected = DetectionException.class)
	public void checkForPropertyNamePasses() throws DetectionException {
		try {
			PSLStructureParser.checkForPropertyName("something_special : ");
			PSLStructureParser.checkForPropertyName("something_special : and more;");
		} catch (Exception e) {
			return;
		}
		throw new DetectionException();
	}

	@Test
	public void correctNameExtracted() throws Exception {
		assertEquals("p7", PSLStructureParser.extractPropertyName(PSLProperties.FORMATTED_PROPERTY_ARRAY[6]));
	}

	@Test(expected = DetectionException.class)
	public void incorrectNameRejected() throws DetectionException {
		try {
			assertEquals("p1", PSLStructureParser.extractPropertyName(PSLProperties.UNFORMATTED_PROPERTY_ARRAY[0]));
		} catch (Exception e) {
			if (e.getMessage().startsWith(PSLStructureParser.MISSING_NAME_TEXT)) {
				throw new DetectionException();
			}
		}
	}

	@Test(expected = DetectionException.class)
	public void missingDirectiveRejected() throws DetectionException {
		try {
			PSLStructureParser.extractVerificationDirective("m : issing_Directive;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(PSLStructureParser.MISSING_DIRECTIVE_TEXT)) {
				throw new DetectionException();
			}
		}
	}

	@Test(expected = DetectionException.class)
	public void invalidDirectiveRejected() throws DetectionException {
		try {
			PSLStructureParser.extractVerificationDirective("in : correct_Directive or_unknown_directive;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(PSLStructureParser.INCORRECT_DIRECTIVE_1_TEXT)) {
				throw new DetectionException();
			}
		}
	}

	@Test
	public void correctDirectiveExtracted() throws Exception {
		for (int i = 0; i < PSLProperties.FORMATTED_PROPERTY_ARRAY.length; i++) {
			assertEquals(VerificationDirective.ASSERT, PSLStructureParser.extractVerificationDirective(PSLProperties.FORMATTED_PROPERTY_ARRAY[i]));
		}
	}

	@Test
	public void correctBodyExtracted() throws Exception {
		assertEquals("( a -> next b )", PSLStructureParser.extractPropertyBody(PSLProperties.FORMATTED_PROPERTY_ARRAY[5]));
		assertEquals("always ( a -> next[ 2 ] b )", PSLStructureParser.extractPropertyBody(PSLProperties.FORMATTED_PROPERTY_ARRAY[6]));
	}

	@Test(expected = DetectionException.class)
	public void exceptionForMissingBody() throws DetectionException {
		boolean first = false;
		try {
			PSLStructureParser.extractPropertyBody("m : issing_Body; ");
		} catch (Exception e) {
			if (e.getMessage().startsWith(PSLStructureParser.EMPTY_BODY_TEXT)) {
				first = true;
			}
		}
		try {
			PSLStructureParser.extractPropertyBody("m : issing_Body ;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(PSLStructureParser.EMPTY_BODY_TEXT)) {
				if (first) {
					throw new DetectionException();
				}
			}
		}
	}


}
