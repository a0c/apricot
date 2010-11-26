package base.hldd.structure.nodes;

import base.SourceLocation;
import org.junit.Test;

import static org.junit.Assert.*;

import base.hldd.structure.variables.Variable;
import base.hldd.structure.Flags;
import base.Range;
import base.Type;

import java.io.File;
import java.util.Collections;

/**
 * @author Anton Chepurov
 */
public class TemporalNodeTest {

	@Test
	public void builder() throws Exception {

		// Check class of the node
		Node node = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).build();
		assertTrue(node instanceof TemporalNode);
		// Window
		node = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags()))
				.window(new base.psl.structure.Range(new int[]{0, 7}, base.psl.structure.Range.TemporalModifier.ALWAYS.getSuffix())).build();
		assertTrue(node instanceof TemporalNode);
		assertEquals("someVar@[0..7]_a", node.depVarName());
		// Window placeholders
		String[] windowPlaceholders = {"ph1", "ph2"};
		node = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags()))
				.windowPlaceholders(windowPlaceholders).build();
		assertTrue(node instanceof TemporalNode);
		assertNotNull(((TemporalNode) node).getWindowPlaceholders());
		assertArrayEquals(windowPlaceholders, ((TemporalNode) node).getWindowPlaceholders());

		// Check correctness of super-class methods
		Range range = new Range(9, 2);
		SourceLocation source = new SourceLocation(new File("Buba.aqa"), Collections.singleton(1991));
		node = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags()))
				.createSuccessors(2)
				.range(range)
				.source(source).build();
		assertTrue(node instanceof TemporalNode);
		assertTrue(node.isControlNode());
		assertNotNull(node.getRange());
		assertEquals(range, node.getRange());
		assertNotNull(node.getSource());
		assertEquals(source, node.getSource());
		assertEquals("Buba.aqa 1991", node.getSource().toString());

		// Check class of the chained builder
		Node.Builder builder = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).createSuccessors(2);
		assertTrue(builder instanceof TemporalNode.Builder);
		builder = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).range(range);
		assertTrue(builder instanceof TemporalNode.Builder);
		builder = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).source(source);
		assertTrue(builder instanceof TemporalNode.Builder);

	}
}
