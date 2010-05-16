package base.hldd.structure.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import base.hldd.structure.variables.Variable;
import base.hldd.structure.Flags;
import base.Indices;
import base.Type;

import java.util.Collections;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 01.11.2009
 * <br>Time: 21:36:01
 */
public class NodeTest {

    @Test
    public void builder () {

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

        // Terminal node with vhdl lines
        node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).vhdlLines(Collections.singleton(2)).build();
        assertTrue(node.isTerminalNode());
        assertNotNull(node.getVhdlLines());
        assertEquals(1, node.getVhdlLines().size());
        assertTrue(node.getVhdlLines().contains(new Integer(2)));

        // Control node with parted indices and vhdl lines
        partedIndices = new Indices(2, 1);
        node = new Node.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).createSuccessors(2).partedIndices(partedIndices).vhdlLines(Collections.singleton(199)).build();
        assertTrue(node.isControlNode());
        assertNotNull(node.getSuccessors());
        assertEquals(2, node.getConditionValuesCount());
        assertNotNull(node.getPartedIndices());
        assertEquals(partedIndices, node.getPartedIndices());
        assertNotNull(node.getVhdlLines());
        assertEquals(1, node.getVhdlLines().size());
        assertTrue(node.getVhdlLines().contains(new Integer(199)));
        
    }

    @Test
    public void testToString() {
        
    }

}
