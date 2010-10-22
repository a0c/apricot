package base.hldd.structure.nodes;

import base.SourceLocation;
import org.junit.Test;

import static org.junit.Assert.*;

import base.hldd.structure.variables.Variable;
import base.hldd.structure.Flags;
import base.Indices;
import base.Type;

import java.io.File;
import java.util.Collections;

/**
 * @author Anton Chepurov
 */
public class NodeTest {

	@Test
	public void builder() {

		// Terminal node
		Node node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).build();
		assertTrue(node.isTerminalNode());
		assertEquals("someVar", node.depVarName());

		// Control node
		node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).createSuccessors(2).build();
		assertTrue(node.isControlNode());
		assertEquals("someVar", node.depVarName());
		assertNotNull(node.getSuccessors());
		assertEquals(2, node.getConditionValuesCount());

		// Terminal node with parted indices
		Indices partedIndices = new Indices(7, 0);
		node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).partedIndices(partedIndices).build();
		assertTrue(node.isTerminalNode());
		assertNotNull(node.getPartedIndices());
		assertEquals(partedIndices, node.getPartedIndices());

		// Control node with parted indices
		partedIndices = new Indices(2, 1);
		node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).createSuccessors(2).partedIndices(partedIndices).build();
		assertTrue(node.isControlNode());
		assertNotNull(node.getSuccessors());
		assertEquals(2, node.getConditionValuesCount());
		assertNotNull(node.getPartedIndices());
		assertEquals(partedIndices, node.getPartedIndices());

		// Terminal node with source
		File sourceFile = new File("SuperFile.aga");
		node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).source(new SourceLocation(sourceFile, Collections.singleton(2))).build();
		assertTrue(node.isTerminalNode());
		assertNotNull(node.getSource());
		assertEquals("SuperFile.aga 2", node.getSource().toString());

		// Control node with parted indices and source
		partedIndices = new Indices(2, 1);
		node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).createSuccessors(2).partedIndices(partedIndices).source(new SourceLocation(sourceFile, Collections.singleton(199))).build();
		assertTrue(node.isControlNode());
		assertNotNull(node.getSuccessors());
		assertEquals(2, node.getConditionValuesCount());
		assertNotNull(node.getPartedIndices());
		assertEquals(partedIndices, node.getPartedIndices());
		assertNotNull(node.getSource());
		assertEquals("SuperFile.aga 199", node.getSource().toString());

	}

	@Test
	public void testToString() {

	}

}
