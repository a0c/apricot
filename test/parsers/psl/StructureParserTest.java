package parsers.psl;

import ee.ttu.pld.apricot.DetectionException;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import base.psl.structure.VerificationDirective;
import helpers.PSLProperties;

/**
 * @author Anton Chepurov
 */
public class StructureParserTest {

	@Test(expected = DetectionException.class)
	public void checkForPropertyNameFails() throws DetectionException {
		boolean first = false, second = false, third = false;
		try {
			StructureParser.checkForPropertyName("m:issing_name;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(StructureParser.MISSING_NAME_TEXT)) {
				first = true;
			}
		}
		try {
			StructureParser.checkForPropertyName("m: issing_name;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(StructureParser.MISSING_NAME_TEXT)) {
				second = true;
			}
		}
		try {
			StructureParser.checkForPropertyName("m :issing_name;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(StructureParser.MISSING_NAME_TEXT)) {
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
			StructureParser.checkForPropertyName("something_special : ");
			StructureParser.checkForPropertyName("something_special : and more;");
		} catch (Exception e) {
			return;
		}
		throw new DetectionException();
	}

	@Test
	public void correctNameExtracted() throws Exception {
		assertEquals("p7", StructureParser.extractPropertyName(PSLProperties.FORMATTED_PROPERTY_ARRAY[6]));
	}

	@Test(expected = DetectionException.class)
	public void incorrectNameRejected() throws DetectionException {
		try {
			assertEquals("p1", StructureParser.extractPropertyName(PSLProperties.UNFORMATTED_PROPERTY_ARRAY[0]));
		} catch (Exception e) {
			if (e.getMessage().startsWith(StructureParser.MISSING_NAME_TEXT)) {
				throw new DetectionException();
			}
		}
	}

	@Test(expected = DetectionException.class)
	public void missingDirectiveRejected() throws DetectionException {
		try {
			StructureParser.extractVerificationDirective("m : issing_Directive;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(StructureParser.MISSING_DIRECTIVE_TEXT)) {
				throw new DetectionException();
			}
		}
	}

	@Test(expected = DetectionException.class)
	public void invalidDirectiveRejected() throws DetectionException {
		try {
			StructureParser.extractVerificationDirective("in : correct_Directive or_unknown_directive;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(StructureParser.INCORRECT_DIRECTIVE_1_TEXT)) {
				throw new DetectionException();
			}
		}
	}

	@Test
	public void correctDirectiveExtracted() throws Exception {
		for (int i = 0; i < PSLProperties.FORMATTED_PROPERTY_ARRAY.length; i++) {
			assertEquals(VerificationDirective.ASSERT, StructureParser.extractVerificationDirective(PSLProperties.FORMATTED_PROPERTY_ARRAY[i]));
		}
	}

	@Test
	public void correctBodyExtracted() throws Exception {
		assertEquals("( a -> next b )", StructureParser.extractPropertyBody(PSLProperties.FORMATTED_PROPERTY_ARRAY[5]));
		assertEquals("always ( a -> next[ 2 ] b )", StructureParser.extractPropertyBody(PSLProperties.FORMATTED_PROPERTY_ARRAY[6]));
	}

	@Test(expected = DetectionException.class)
	public void exceptionForMissingBody() throws DetectionException {
		boolean first = false;
		try {
			StructureParser.extractPropertyBody("m : issing_Body; ");
		} catch (Exception e) {
			if (e.getMessage().startsWith(StructureParser.EMPTY_BODY_TEXT)) {
				first = true;
			}
		}
		try {
			StructureParser.extractPropertyBody("m : issing_Body ;");
		} catch (Exception e) {
			if (e.getMessage().startsWith(StructureParser.EMPTY_BODY_TEXT)) {
				if (first) {
					throw new DetectionException();
				}
			}
		}
	}


}
