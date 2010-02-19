package parsers.psl;

import org.junit.Test;
import org.junit.Before;
import base.psl.structure.*;
import static junit.framework.Assert.*;
import helpers.PSLProperties;

import java.util.List;
import java.util.LinkedList;

import parsers.psl.PSLStructureBuilder;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 24.09.2008
 * <br>Time: 13:39:36
 */
public class PSLStructureBuilderTest {
    private final Collection[] sourceProps = {
            new Collection("p1", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[0][0], "property source line 1"),
            new Collection("p2", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[1][0], "property source line 2"),
            new Collection("p3", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[2][0], "property source line 3"),
            new Collection("p4", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[3][0], "property source line 4"),
            new Collection("p5", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[4][0], "property source line 5"),
            new Collection("p6", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[5][0], "property source line 6"),
            new Collection("p7", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[6][0], "property source line 7"),
            new Collection("p8", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[7][0], "property source line 8"),
            new Collection("p9", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[8][0], "property source line 9"),
            new Collection("p10", VerificationDirective.ASSERT, PSLProperties.exampleOperatorArray[9][0], "property source line 10")
    };

    private PSLStructureBuilder builder;

    @Before
    public void initBuilder() throws Exception {
        builder = new PSLStructureBuilder(PPGLibraryTest.createLibrary());
    }

    @Test
    public void numberOfPropsAndStartingExpr() throws Exception {
        /* BUILD properties */
        Property[] properties = buildPropertiesFrom(sourceProps);

        /* ASSERT */
        assertEquals("Number of built properties is wrong", sourceProps.length, properties.length);
        for (int i = 0; i < sourceProps.length; i++) {
            Collection sourceProp = sourceProps[i];
            assertEquals(sourceProp.sourceLine, properties[i].getComment());
            assertEquals(sourceProp.name, properties[i].getName());
            assertTrue(properties[i].getStartExpression() instanceof ExpressionImpl);
            assertEquals(PSLProperties.exampleOperatorArray[i][1], ((ExpressionImpl) properties[i].getStartExpression()).getPslOperator().getName());
        }
    }

    @Test
    public void correctlyParsedFirstExpression() throws Exception {
        /* BUILD properties */
        Property property = buildPropertiesFrom(sourceProps[0])[0];
        /* Collect operators */
        LinkedList<PSLOperator> operatorList = new LinkedList<PSLOperator>();
        collectOperators(property.getStartExpression(), operatorList);
        /* Check (ASSERT) collected operators */
        assertEquals("Number of build expressions is wrong", 5, operatorList.size());
        assertEquals(PSLProperties.exampleOperatorArray[0][1], operatorList.get(0).getName()); // ALWAYS
        assertEquals(PSLProperties.exampleOperatorArray[3][1], operatorList.get(1).getName()); // -->
        assertEquals(PSLProperties.exampleOperatorArray[12][1], operatorList.get(2).getName()); // and
        assertEquals(PSLProperties.exampleOperatorArray[13][1], operatorList.get(3).getName()); // not
        assertEquals(PSLProperties.exampleOperatorArray[8][1], operatorList.get(4).getName()); // next_e[start to end]
    }

    /* Helper method */
    private void collectOperators(AbstractExpression startExpression, List<PSLOperator> operatorsList) {
        if (startExpression instanceof ExpressionImpl) {
            operatorsList.add(((ExpressionImpl) startExpression).getPslOperator());
            for (AbstractExpression operand : ((ExpressionImpl) startExpression).getOperands()) {
                collectOperators(operand, operatorsList);
            }
        }
    }

    /* Helper method */
    private Property[] buildPropertiesFrom(Collection... sourceProps) throws Exception {
        /* BUILD */
        for (Collection sourceProp : sourceProps) {
            builder.buildProperty(sourceProp.name, sourceProp.directive, sourceProp.body, sourceProp.sourceLine);
        }
        /* OBTAIN and RETURN*/
        return builder.getProperties();
    }

    /**
     * Helper class
     */
    class Collection {
        final String name;
        final VerificationDirective directive;
        final String body;
        final String sourceLine;

        public Collection(String name, VerificationDirective directive, String body, String sourceLine) {
            this.name = name;
            this.directive = directive;
            this.body = body;
            this.sourceLine = sourceLine;
        }
    }
}
