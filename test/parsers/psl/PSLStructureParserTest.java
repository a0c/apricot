package parsers.psl;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import base.psl.structure.VerificationDirective;
import helpers.ThrowableFlag;
import helpers.PSLProperties;
import parsers.psl.PSLStructureParser;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 17.09.2008
 * <br>Time: 13:02:20
 */
public class PSLStructureParserTest {

    @Test (expected = ThrowableFlag.class)
    public void checkForPropertyNameFails() throws ThrowableFlag {
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
            throw new ThrowableFlag();
        }
    }

    @Test (expected = ThrowableFlag.class)
    public void checkForPropertyNamePasses() throws ThrowableFlag {
        try {
            PSLStructureParser.checkForPropertyName("something_special : ");
            PSLStructureParser.checkForPropertyName("something_special : and more;");
        } catch (Exception e) {return;}
        throw new ThrowableFlag();
    }

    @Test
    public void correctNameExtracted() throws Exception {
        assertEquals("p7", PSLStructureParser.extractPropertyName(PSLProperties.formattedPropertyArray[6]));
    }

    @Test (expected = ThrowableFlag.class)
    public void incorrectNameRejected() throws ThrowableFlag {
        try {
            assertEquals("p1", PSLStructureParser.extractPropertyName(PSLProperties.unformattedPropertyArray[0]));
        } catch (Exception e) {
            if (e.getMessage().startsWith(PSLStructureParser.MISSING_NAME_TEXT)) {
                throw new ThrowableFlag();
            }
        }
    }

    @Test (expected = ThrowableFlag.class)
    public void missingDirectiveRejected() throws ThrowableFlag {
        try {
            PSLStructureParser.extractVerificationDirective("m : issing_Directive;");
        } catch (Exception e) {
            if (e.getMessage().startsWith(PSLStructureParser.MISSING_DIRECTIVE_TEXT)) {
                throw new ThrowableFlag();
            }
        }
    }

    @Test (expected = ThrowableFlag.class)
    public void invalidDirectiveRejected() throws ThrowableFlag {
        try {
            PSLStructureParser.extractVerificationDirective("in : correct_Directive or_unknown_directive;");
        } catch (Exception e) {
            if (e.getMessage().startsWith(PSLStructureParser.INCORRECT_DIRECTIVE_1_TEXT)) {
                throw new ThrowableFlag();
            }
        }
    }

    @Test
    public void correctDirectiveExtracted() throws Exception {
        for (int i = 0; i < PSLProperties.formattedPropertyArray.length; i++) {
            assertEquals(VerificationDirective.ASSERT, PSLStructureParser.extractVerificationDirective(PSLProperties.formattedPropertyArray[i]));
        }
    }

    @Test
    public void correctBodyExtracted() throws Exception {
        assertEquals("( a -> next b )", PSLStructureParser.extractPropertyBody(PSLProperties.formattedPropertyArray[5]));
        assertEquals("always ( a -> next[ 2 ] b )", PSLStructureParser.extractPropertyBody(PSLProperties.formattedPropertyArray[6]));
    }

    @Test (expected = ThrowableFlag.class)
    public void exceptionForMissingBody() throws ThrowableFlag {
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
                    throw new ThrowableFlag();
                }
            }
        }
    }


}
