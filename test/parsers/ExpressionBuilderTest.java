package parsers;

import org.junit.Test;

import static org.junit.Assert.*;
import static junit.framework.Assert.assertEquals;

import base.vhdl.structure.*;
import base.Indices;

import java.util.List;
import java.util.Collections;
import java.math.BigInteger;

/**
 * @author Anton Chepurov
 */
public class ExpressionBuilderTest {

	private static final String[] TRIM_SOURCE = {
			"( something )",
			"( something () )",
			"( ()   ()  )",
			"()     ()",
			"( something",
			"something )  ",
			"something",
			"((() smth() (())))",
			"( ( ( ) smth ( ) ( ( ) ) ) )"
	};
	private static final String[] TRIM_DESTINATION = {
			"something",
			"something ()",
			"()   ()",
			"()     ()",
			"( something",
			"something )",
			"something",
			"() smth() (())",
			"( ) smth ( ) ( ( ) )"
	};

	private static final String[] EXPLICIT_TYPE_CONVERSIONS = {
			"std_logic_vector ( unsigned ( accumulation ) + unsigned ( \"0\" & operandB ) )",
			"unSigned ( accumulation )",
			"conv_integer ( ips_addr ( MOD_EN_BITS -1 downto 4 ) )"
	};
	private static final String[] NON_EXPLICIT_TYPE_CONVERSIONS = {
			"unsigned ( accumulation ) + unsigned ( accumulation )",
			"operand",
			"operand ( 3 )",
			"unSigne ( accumulation )"
	};

	private static final String[] CLOCK_EXPRESSIONS = {
			"CLOCK'EVENT AND CLOCK='1'",
			"CLK='0'",
			"CLK'EVENT AND CLK='1'"
	};

	private static final String[] SOURCE_INDICES = {
			"NoIndices",
			"SingleIndex ( 92093 )",
			"DoubleIndex ( 123 DOWNTO 2 )",
			"ComplexIndex ( CONSTANT - 123 DOWNTO ANOTHER_GENERIC + 2 )", //assume that CONSTANT = 130 and ANOTHER_GENERIC = 3
			"127 DOWNTO ( -128 )",  // not to mix ExpressionBuilder.singleBitPattern and ExpressionBuilder.pureBitRangePattern

			"D_IN ( 8 DOWNTO 1 )",
			"D_IN ( 1 TO 8 )",

			"D_IN ( 0 )",

			"32767 DOWNTO -32768",
			"0 TO 3",
			"(PROCESSOR_WIDTH -1) DOWNTO -1" //assume that Processor_width = 19
	};

	private static final Indices[] DESTINATION_INDICES = {
			null,
			new Indices(92093, 92093),
			new Indices(123, 2),
			new Indices(7, 5),
			new Indices(127, -128),

			new Indices(8, 1),
			new Indices(8, 1, false),

			new Indices(0, 0),

			new Indices(32767, -32768),
			new Indices(3, 0, false),
			new Indices(18, -1),
	};

	@Test
	public void testTrimEnclosingBrackets() {

		for (int i = 0; i < TRIM_SOURCE.length; i++) {
			assertEquals(TRIM_DESTINATION[i], ExpressionBuilder.trimEnclosingBrackets(TRIM_SOURCE[i]));
		}

	}

	@Test
	public void explicitTypeConversionDetected() throws Exception {
		/* ACCEPT */
		for (String line : EXPLICIT_TYPE_CONVERSIONS) {
			assertTrue(
					"Explicit type conversion discarded: " + line,
					ExpressionBuilder.isExplicitTypeConversion(line));
		}
		/* DISCARD */
		for (String line : NON_EXPLICIT_TYPE_CONVERSIONS) {
			assertTrue(
					"Non-Explicit type conversion discarded: " + line,
					!ExpressionBuilder.isExplicitTypeConversion(line));
		}

	}

	@Test
	public void correctExpressionBuiltFromETC() throws Exception { // Explicit Type Conversion
		ExpressionBuilder builder = new ExpressionBuilder();
		AbstractOperand expression = builder.buildExpression(EXPLICIT_TYPE_CONVERSIONS[0]);
		assertNotNull("Operand was not built", expression);
		assertTrue(OperandImpl.class.getSimpleName() + " is built instead of " + Expression.class.getSimpleName() +
				" for expression: " + EXPLICIT_TYPE_CONVERSIONS[0],
				expression instanceof Expression);
		List<AbstractOperand> operands = ((Expression) expression).getOperands();
		assertEquals("Incorrect number of operand for the following expression: " + expression, 2, operands.size());
		assertTrue("Incorrect 1st operand", operands.get(0).toString().equalsIgnoreCase("accumulation"));
		assertTrue(OperandImpl.class.getSimpleName() + " is built instead of " + Expression.class.getSimpleName() +
				" for expression: " + "\"0\" & operandB", operands.get(1) instanceof Expression);
	}

	@Test
	public void correctExpressionBuiltForClock() throws Exception {
		ExpressionBuilder builder = new ExpressionBuilder();
		AbstractOperand abstractOperand = builder.buildExpression(CLOCK_EXPRESSIONS[0]);
		assertTrue(OperandImpl.class.getSimpleName() + " is built instead of " + Expression.class.getSimpleName() +
				" for expression: " + CLOCK_EXPRESSIONS[0], abstractOperand instanceof Expression);
		Expression clockExpression = (Expression) abstractOperand;
		assertTrue("AND operand was not created for expression: " + CLOCK_EXPRESSIONS[0],
				clockExpression.getOperator() == Operator.AND);
		assertTrue("Incorrect number of operand for the following expression: " + CLOCK_EXPRESSIONS[0],
				clockExpression.getOperands().size() == 2);
		assertEquals("CLOCK'EVENT", clockExpression.getOperands().get(0).toString());
		assertTrue(OperandImpl.class.getSimpleName() + " is built instead of " + Expression.class.getSimpleName() +
				" for expression: " + "CLOCK='1'", clockExpression.getOperands().get(1) instanceof Expression);

	}

	@Test
	public void correctUserDefinedFunctionBuilt() {
		System.out.println("someInteger ( constant )");

	}

	@Test
	public void correctIndicesBuilt() throws Exception {
		OperandValueCalculator calculator = new OperandValueCalculator();
		calculator.addConstant(new Constant("CONSTANT", null, BigInteger.valueOf(130)));
		calculator.addConstant(new Constant("ANOTHER_GENERIC", null, BigInteger.valueOf(3)));
		calculator.addConstant(new Constant("PROCESSOR_WIDTH", null, BigInteger.valueOf(19)));

		ExpressionBuilder builder = new ExpressionBuilder(calculator, Collections.<String>emptySet());
		for (int i = 0; i < SOURCE_INDICES.length; i++) {
			Indices indices = builder.buildRange(SOURCE_INDICES[i]);
			assertEquals("Incorrect indices built from \"" + SOURCE_INDICES[i] + "\"",
					DESTINATION_INDICES[i], indices);
		}
	}

}