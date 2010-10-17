package base.hldd.structure.nodes;

import base.SourceLocation;
import org.junit.Test;

import static org.junit.Assert.*;

import base.hldd.structure.variables.Variable;
import base.hldd.structure.Flags;
import base.psl.structure.Range;
import base.Indices;
import base.Type;

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
				.window(new Range(new int[]{0, 7}, Range.TemporalModifier.ALWAYS.getSuffix())).build();
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
		Indices partedIndices = new Indices(9, 2);
		SourceLocation source = new SourceLocation(Collections.singleton(1991));
		node = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags()))
				.createSuccessors(2)
				.partedIndices(partedIndices)
				.source(source).build();
		assertTrue(node instanceof TemporalNode);
		assertTrue(node.isControlNode());
		assertNotNull(node.getPartedIndices());
		assertEquals(partedIndices, node.getPartedIndices());
		assertNotNull(node.getSource());
		assertEquals(source, node.getSource());
		assertEquals("1991", node.getSource().toString());

		// Check class of the chained builder
		Node.Builder builder = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).createSuccessors(2);
		assertTrue(builder instanceof TemporalNode.Builder);
		builder = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).partedIndices(partedIndices);
		assertTrue(builder instanceof TemporalNode.Builder);
		builder = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).source(source);
		assertTrue(builder instanceof TemporalNode.Builder);

	}
}
