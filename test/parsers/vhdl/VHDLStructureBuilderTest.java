package parsers.vhdl;

import org.junit.Test;
import static org.junit.Assert.*;
import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.AbstractNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.Type;
import base.Indices;

import java.util.Set;
import java.util.List;
import java.util.LinkedList;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 07.10.2008
 * <br>Time: 12:18:26
 */
public class VHDLStructureBuilderTest {

    @Test
    public void othersTransitionCreatedCorrectly() throws Exception {

        Entity structure = insertTransitionIntoEntity("(OTHERS => '0')");
        AbstractNode rootNode = getRootNodeAndCheck(structure);

        assertTrue("Incorrectly parsed OTHERS transition",
                ((TransitionNode) rootNode).getValueOperand().toString().equals("\"00000000\""));        

    }

    @Test
    public void nullTransitionCreatedCorrectly() throws Exception {

        Entity structure = insertTransitionIntoEntity("NULL");
        AbstractNode rootNode = getRootNodeAndCheck(structure);

        assertTrue("Incorrectly parsed NULL transition", ((TransitionNode) rootNode).isNull());
    }

    private static AbstractNode getRootNodeAndCheck(Entity structure) {
        Set<Process> processes = structure.getArchitecture().getProcesses();
        assertNotNull("Process is not parsed", processes);
        assertEquals("Incorrect number of parsed processes", 1, processes.size());
        Process process = processes.iterator().next();
        List<AbstractNode> children = process.getRootNode().getChildren();
        assertTrue("The root node of process is empty", children.size() > 0);
        AbstractNode rootNode = children.get(0);
        assertTrue(rootNode instanceof TransitionNode);
        return rootNode;
    }

    private static Entity insertTransitionIntoEntity(String transitionValue) throws Exception {
        String portName = "varName";
        int portHighestSB = 7;
        VHDLStructureBuilder builder = new VHDLStructureBuilder();
        builder.buildEntity("EntityName");
        builder.buildPort(portName, false, new Type(new Indices(portHighestSB, 0)));
        builder.buildArchitecture("ArchitectureName", "ArchitectureAffiliation");
        builder.buildProcess("ProcessName", new LinkedList<String>());
        builder.buildTransition(portName, transitionValue, null);
        return builder.getVHDLStructure();
    }
}
