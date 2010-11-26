package base.vhdl.visitors;

import base.Indices;
import base.Type;
import base.TypeResolver;
import base.hldd.structure.Flags;
import base.hldd.structure.variables.RangeVariable;
import base.hldd.structure.variables.Variable;
import base.vhdl.structure.OperandImpl;
import base.vhdl.structure.Transition;
import base.vhdl.structure.nodes.TransitionNode;
import org.junit.Test;
import parsers.ExpressionBuilder;
import parsers.OperandValueCalculator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Anton Chepurov
 */
public class GraphGeneratorTest {


	@Test
	public void someTest() {
/*
        ### /// ### /// ### /// ### /// ### /// ###
        Here remember that ":=" is a Blocking assignment,
        and "<=" is a Non-Blocking assignment.
        The result of the Blocking one is observable AT ONCE.
        The result of the Non-Blocking one is observable only AT THE END OF THE PROCESS.
        ### /// ### /// ### /// ### /// ### /// ###

        ########### 1 ###########
        conta_tmp := conta_tmp+1;
		if conta_tmp = 8 then
			conta_tmp := 0;
        end if;

        ########### 2 ###########
        conta_tmp := conta_tmp+1;
		if conta_tmp = 8 then
			conta_tmp(2) := 0;
		end if;

        ########### 3 ###########
        cts   <= '1'; //init

        if rtr = '1' then
            cts   <= '1';
        end if ;
        if rtr = '0' then
            cts <= '0' ;
        end if ;

        cts   <= '0'; //smth else

        ########### 4 ###########
        IF ((tre = '0') OR (dsr = '0')) THEN
            error <= '1';
            error <= '1';
        ELSIF (NOT ((tre = '0') OR (dsr = '0'))) THEN
            error <= '1';
            error <= '0';
        END IF;

        ########### 5 ###########
        crc_d8_start_1        <= '0';
        

*/
	}

	@Test
	public void correctIsVariableSetIn() throws Exception {
		/* Build variable to compare with */
		Variable comparedVariable = new Variable("V_OUT", new Type(new Indices(3, 0)), new Flags());
		Variable differentVariable = new Variable("BALOON", new Type(new Indices(3, 0)), new Flags());
		RangeVariable comparedVariable2 = new RangeVariable("V_OUT", new Type(new Indices(7, 0)), new Indices(2, 2));
		RangeVariable comparedVariable3 = new RangeVariable("V_OUT", new Type(new Indices(7, 0)), new Indices(7, 0));
		RangeVariable comparedVariable4 = new RangeVariable("V_OUT", new Type(new Indices(7, 0)), new Indices(6, 0));
		RangeVariable comparedVariable5 = new RangeVariable("V_OUT", new Type(new Indices(7, 0)), new Indices(7, 1));
		/* Build and test Transition Nodes */
		TransitionNode transNodeNull = buildTransitionNode(null);
		TransitionNode transNodeFull = buildTransitionNode("V_OUT");
		TransitionNode transNodePart1 = buildTransitionNode("V_OUT ( 2 )");
		TransitionNode transNodePart2 = buildTransitionNode("V_OUT ( 7 DOWNTO 0 )");

		/* NULL transition */
		assertTrue(notRecognizedMessage(comparedVariable, transNodeNull), GraphGenerator.isVariableSetIn(transNodeNull, comparedVariable, true, null));
		assertTrue(notRecognizedMessage(comparedVariable, transNodeNull), GraphGenerator.isVariableSetIn(transNodeNull, comparedVariable2, true, null));
		assertTrue(notRecognizedMessage(comparedVariable, transNodeNull), GraphGenerator.isVariableSetIn(transNodeNull, comparedVariable3, true, null));
		assertTrue(notRecognizedMessage(differentVariable, transNodeNull), GraphGenerator.isVariableSetIn(transNodeNull, differentVariable, true, null));
		assertFalse(notRecognizedMessage(comparedVariable, transNodeNull), GraphGenerator.isVariableSetIn(transNodeNull, comparedVariable, false, null));
		assertFalse(notRecognizedMessage(comparedVariable, transNodeNull), GraphGenerator.isVariableSetIn(transNodeNull, comparedVariable2, false, null));
		assertFalse(notRecognizedMessage(comparedVariable, transNodeNull), GraphGenerator.isVariableSetIn(transNodeNull, comparedVariable3, false, null));
		assertFalse(notRecognizedMessage(differentVariable, transNodeNull), GraphGenerator.isVariableSetIn(transNodeNull, differentVariable, false, null));

		TypeResolver typeResolver = mock(TypeResolver.class);

		/* Compared with "DIFFERENT_VARIABLE" */
		assertFalse(recognizedMessage(differentVariable, transNodePart2), GraphGenerator.isVariableSetIn(transNodePart2, differentVariable, true, typeResolver));
		/* Same name, non of them is range */
		when(typeResolver.resolveType("V_OUT")).thenReturn(new Type(new Indices(3, 0)));
		assertTrue(notRecognizedMessage(comparedVariable, transNodeFull), GraphGenerator.isVariableSetIn(transNodeFull, comparedVariable, true, typeResolver));
		/* Same name, var is range, node is not */
		when(typeResolver.resolveType("V_OUT")).thenReturn(new Type(new Indices(7, 0)));
		assertTrue(notRecognizedMessage(comparedVariable2, transNodeFull), GraphGenerator.isVariableSetIn(transNodeFull, comparedVariable2, true, typeResolver));
		assertTrue(notRecognizedMessage(comparedVariable3, transNodeFull), GraphGenerator.isVariableSetIn(transNodeFull, comparedVariable3, true, typeResolver));
		/* Same name, var is range, node is too */
		assertTrue(notRecognizedMessage(comparedVariable2, transNodePart1), GraphGenerator.isVariableSetIn(transNodePart1, comparedVariable2, true, typeResolver));
		assertTrue(notRecognizedMessage(comparedVariable2, transNodePart2), GraphGenerator.isVariableSetIn(transNodePart1, comparedVariable2, true, typeResolver));
		assertTrue(notRecognizedMessage(comparedVariable4, transNodePart2), GraphGenerator.isVariableSetIn(transNodePart2, comparedVariable4, true, typeResolver));
		assertTrue(notRecognizedMessage(comparedVariable5, transNodePart2), GraphGenerator.isVariableSetIn(transNodePart2, comparedVariable5, true, typeResolver));

		assertTrue(notRecognizedMessage(comparedVariable3, transNodePart2), GraphGenerator.isVariableSetIn(transNodePart2, comparedVariable3, true, typeResolver));
		assertFalse(recognizedMessage(comparedVariable3, transNodePart1), GraphGenerator.isVariableSetIn(transNodePart1, comparedVariable3, true, typeResolver));

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
