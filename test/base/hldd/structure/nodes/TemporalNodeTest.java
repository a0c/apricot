package base.hldd.structure.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import base.hldd.structure.variables.Variable;
import base.hldd.structure.Flags;
import base.psl.structure.Range;
import base.Indices;
import base.Type;

import java.util.Collections;
import java.util.Set;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 01.11.2009
 * <br>Time: 23:15:54
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

        // Check correctness of superclass methods
        Indices partedIndices = new Indices(9, 2);
        Set<Integer> vhdlLines = Collections.singleton(1991);
        node = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags()))
                .createSuccessors(2)
                .partedIndices(partedIndices)
                .vhdlLines(vhdlLines).build();
        assertTrue(node instanceof TemporalNode);
        assertTrue(node.isControlNode());
        assertNotNull(node.getPartedIndices());
        assertEquals(partedIndices, node.getPartedIndices());
        assertNotNull(node.getVhdlLines());
        assertEquals(vhdlLines, node.getVhdlLines());
        assertTrue(node.getVhdlLines().contains(new Integer(1991)));

        // Check class of the chained builder
        Node.Builder builder = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).createSuccessors(2);
        assertTrue(builder instanceof TemporalNode.Builder);
        builder = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).partedIndices(partedIndices);
        assertTrue(builder instanceof TemporalNode.Builder);
        builder = new TemporalNode.Builder(new Variable("someVar", Type.BIT_TYPE, new Flags())).vhdlLines(vhdlLines);
        assertTrue(builder instanceof TemporalNode.Builder);

    }
}
