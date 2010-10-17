package base.vhdl.structure;

import org.junit.Test;
import org.junit.Ignore;

import static org.junit.Assert.assertTrue;

import parsers.ExpressionBuilder;

/**
 * @author Anton Chepurov
 */
public class TransitionTest {

	@Test
	@Ignore
	public void someTest() throws Exception {
		Transition nullTransition = new Transition();
		Transition regTransition = new Transition(((OperandImpl) new ExpressionBuilder().buildExpression("VariableName")), new OperandImpl("operand"));
		assertTrue("Transitions are not equal", nullTransition.isIdenticalTo(regTransition));
		//todo: deliberately failing transition (made to fail when all tests are run, so that I attend to this problem):
		//todo: suppose VHDL Beh DD, comparing NULL to a REG <= REG,
		//todo: where both are essentially value retaining transitions.
	}

}
