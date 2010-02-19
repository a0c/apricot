package base.vhdl.structure.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import base.hldd.structure.variables.Variable;
import base.hldd.structure.variables.PartedVariable;
import base.hldd.structure.Flags;
import base.vhdl.structure.Transition;
import base.vhdl.structure.OperandImpl;
import base.Indices;
import base.Type;
import parsers.ExpressionBuilder;
import parsers.OperandValueCalculator;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 12.10.2008
 * <br>Time: 23:38:26
 */
public class TransitionNodeTest {

    @Test
    public void correctIsTransitionOf() throws Exception {
        /* Build variable to compare with */
        Variable comparedVariable = new Variable("V_OUT", new Type(new Indices(3, 0)), new Flags());
        Variable differentVariable = new Variable("BALOON", new Type(new Indices(3, 0)), new Flags());
        PartedVariable comparedVariable2 = new PartedVariable("V_OUT", new Type(new Indices(7, 0)), new Indices(2, 2));
        PartedVariable comparedVariable3 = new PartedVariable("V_OUT", new Type(new Indices(7, 0)), new Indices(7, 0)); 
        PartedVariable comparedVariable4 = new PartedVariable("V_OUT", new Type(new Indices(7, 0)), new Indices(6, 0));
        PartedVariable comparedVariable5 = new PartedVariable("V_OUT", new Type(new Indices(7, 0)), new Indices(7, 1));
        /* Build and test Transition Nodes */
        TransitionNode transNodeNull = buildTransitionNode(null);
        TransitionNode transNodeFull = buildTransitionNode("V_OUT");
        TransitionNode transNodePart1 = buildTransitionNode("V_OUT ( 2 )");
        TransitionNode transNodePart2 = buildTransitionNode("V_OUT ( 7 DOWNTO 0 )");

        /* NULL transition */
        assertTrue(notRecognizedMessage(comparedVariable, transNodeNull), transNodeNull.isTransitionOf(comparedVariable, true));
        assertTrue(notRecognizedMessage(comparedVariable, transNodeNull), transNodeNull.isTransitionOf(comparedVariable2, true));
        assertTrue(notRecognizedMessage(comparedVariable, transNodeNull), transNodeNull.isTransitionOf(comparedVariable3, true));
        assertTrue(notRecognizedMessage(differentVariable, transNodeNull), transNodeNull.isTransitionOf(differentVariable, true));
        assertFalse(notRecognizedMessage(comparedVariable, transNodeNull), transNodeNull.isTransitionOf(comparedVariable, false));
        assertFalse(notRecognizedMessage(comparedVariable, transNodeNull), transNodeNull.isTransitionOf(comparedVariable2, false));
        assertFalse(notRecognizedMessage(comparedVariable, transNodeNull), transNodeNull.isTransitionOf(comparedVariable3, false));
        assertFalse(notRecognizedMessage(differentVariable, transNodeNull), transNodeNull.isTransitionOf(differentVariable, false));

        /* Compared with "DIFFERENTVARIABLE" */
        assertFalse(recognizedMessage(differentVariable, transNodePart2), transNodePart2.isTransitionOf(differentVariable, true));
        /* Same name, non of them is parted */
        assertTrue(notRecognizedMessage(comparedVariable, transNodeFull), transNodeFull.isTransitionOf(comparedVariable, true));
        /* Same name, var is parted, node is not */
        assertTrue(notRecognizedMessage(comparedVariable2, transNodeFull), transNodeFull.isTransitionOf(comparedVariable2, true));
        assertTrue(notRecognizedMessage(comparedVariable3, transNodeFull), transNodeFull.isTransitionOf(comparedVariable3, true));
        /* Same name, var is parted, node is too */
        assertTrue(notRecognizedMessage(comparedVariable2, transNodePart1), transNodePart1.isTransitionOf(comparedVariable2, true));
        assertTrue(notRecognizedMessage(comparedVariable2, transNodePart2), transNodePart2.isTransitionOf(comparedVariable2, true));
        assertTrue(notRecognizedMessage(comparedVariable4, transNodePart2), transNodePart2.isTransitionOf(comparedVariable4, true));
        assertTrue(notRecognizedMessage(comparedVariable5, transNodePart2), transNodePart2.isTransitionOf(comparedVariable5, true));

        assertTrue(notRecognizedMessage(comparedVariable3, transNodePart2), transNodePart2.isTransitionOf(comparedVariable3, true));
        assertFalse(recognizedMessage(comparedVariable3, transNodePart1), transNodePart1.isTransitionOf(comparedVariable3, true));

    }

    private static String recognizedMessage(Variable comparedVariable3, TransitionNode transNodePart1) {
        return "Transition Node \"" + transNodePart1 + "\" is recognized as transition of \"" + comparedVariable3 + "\"";
    }

    private static String notRecognizedMessage(Variable comparedVariable, TransitionNode transNodeFull) {
        return "Transition Node \"" + transNodeFull + "\" is not recognized as transition of \"" + comparedVariable + "\"";
    }

    private static TransitionNode buildTransitionNode(String varOperandString) throws Exception {
        ExpressionBuilder builder = new ExpressionBuilder(new OperandValueCalculator(), java.util.Collections.singleton("V_OUT"));
        return varOperandString == null
                ? new TransitionNode(new Transition()) 
                : new TransitionNode(new Transition(((OperandImpl) builder.buildExpression(varOperandString)), builder.buildExpression("valueExpression")));
    }
}
