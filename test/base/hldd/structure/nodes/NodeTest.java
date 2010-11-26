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

		// Terminal node with range
		Indices range = new Indices(7, 0);
		node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).range(range).build();
		assertTrue(node.isTerminalNode());
		assertNotNull(node.getRange());
		assertEquals(range, node.getRange());

		// Control node with range
		range = new Indices(2, 1);
		node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).createSuccessors(2).range(range).build();
		assertTrue(node.isControlNode());
		assertNotNull(node.getSuccessors());
		assertEquals(2, node.getConditionValuesCount());
		assertNotNull(node.getRange());
		assertEquals(range, node.getRange());

		// Terminal node with source
		File sourceFile = new File("SuperFile.aga");
		node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).source(new SourceLocation(sourceFile, Collections.singleton(2))).build();
		assertTrue(node.isTerminalNode());
		assertNotNull(node.getSource());
		assertEquals("SuperFile.aga 2", node.getSource().toString());

		// Control node with range and source
		range = new Indices(2, 1);
		node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).createSuccessors(2).range(range).source(new SourceLocation(sourceFile, Collections.singleton(199))).build();
		assertTrue(node.isControlNode());
		assertNotNull(node.getSuccessors());
		assertEquals(2, node.getConditionValuesCount());
		assertNotNull(node.getRange());
		assertEquals(range, node.getRange());
		assertNotNull(node.getSource());
		assertEquals("SuperFile.aga 199", node.getSource().toString());

	}

	@Test
	public void testToString() {

	}

}
