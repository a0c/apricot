package parsers.vhdl;

import org.junit.Test;
import static org.junit.Assert.*;
import base.vhdl.structure.*;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.AbstractNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.Type;
import base.Indices;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
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


	/*
	* EntityFinder
	* */
	@Test public void correctListOfFiles() throws URISyntaxException {
		VHDLStructureBuilder.ArchitectureFileFinder finder = initAdderFinder();
		List<File> fileList = finder.getListOfFiles();
		assertNotNull(fileList);
		assertEquals(2, fileList.size());
		java.util.Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getPath().compareTo(o2.getPath());
			}
		});
		assertEquals("ha.vhdl", fileList.get(0).getName()); 
		assertEquals("va.vhdl", fileList.get(1).getName());
	}
	@Test public void architectureDetectedInStrings() throws URISyntaxException {
		String entityName = "full_adder";
		
		String input = "end;\n" +
				"\n" +
				"architecture behavioral of full_adder is\n" +
				"begin";
		ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes());
		assertTrue("Architecture not detected in standard string",
				new VHDLStructureBuilder.ArchitectureFileFinder.Detector(is, entityName).isDetected());

		input = "end;\n" +
				"\n" +
				"architecture behavioral\n" +
				"of full_adder is\n" +
				"begin";
		is = new ByteArrayInputStream(input.getBytes());
		assertTrue("Architecture not detected in string over 2 lines",
				new VHDLStructureBuilder.ArchitectureFileFinder.Detector(is, entityName).isDetected());

		input = "end;\n" +
				"\n" +
				"architecture behavioral\n" +
				"      of  full_adder is\n" +
				"begin";
		is = new ByteArrayInputStream(input.getBytes());
		assertTrue("Architecture not detected in string over 2 lines with tabulation",
				new VHDLStructureBuilder.ArchitectureFileFinder.Detector(is, entityName).isDetected());

		input = "end;\n" +
				"\n" +
				"architecture behavioral\n" +
				"      of  blabla is\n" +
				"begin";
		is = new ByteArrayInputStream(input.getBytes());
		assertFalse("False architecture detected in string over 2 lines with tabulation",
				new VHDLStructureBuilder.ArchitectureFileFinder.Detector(is, entityName).isDetected());

		input = "--end;\n" +
				"--\n" +
				"--architecture behavioral\n" +
				"--       of full_adder is\n" +
				"--begin";
		is = new ByteArrayInputStream(input.getBytes());
		assertFalse("Architecture detected in comments over 2 lines",
				new VHDLStructureBuilder.ArchitectureFileFinder.Detector(is, entityName).isDetected());

		input = "--end;\n" +
				"--\n" +
				"--architecture behavioral of full_adder is\n" +
				"--begin";
		is = new ByteArrayInputStream(input.getBytes());
		assertFalse("Architecture detected in a single line comment", 
				new VHDLStructureBuilder.ArchitectureFileFinder.Detector(is, entityName).isDetected());

	}
	@Test public void architectureDetectedInFiles() throws URISyntaxException, FileNotFoundException {
		VHDLStructureBuilder.ArchitectureFileFinder finder = initAdderFinder();
		String entityName = "full_adder";
		File falseFile = new File(finder.getSourceFile().getParent(), "ha.vhdl");
		File correctFile = new File(finder.getSourceFile().getParent(), "va.vhdl");
		assertFalse("Architecture detected in incorrect file",
				new VHDLStructureBuilder.ArchitectureFileFinder.Detector(new FileInputStream(falseFile), entityName).isDetected());
		assertTrue("Architecture not detected in correct file",
				new VHDLStructureBuilder.ArchitectureFileFinder.Detector(new FileInputStream(correctFile), entityName).isDetected());
	}
	@Test public void entityFoundForComponent() throws URISyntaxException {
		VHDLStructureBuilder.ArchitectureFileFinder finder = initAdderFinder();
		File entityFile = finder.findArchitectureFileForEntity("full_adder");
		assertNotNull("File with architecture not found for component", entityFile);
		assertTrue(entityFile.exists());
		assertTrue("Incorrect file found as a component architecture file", entityFile.getName().equals("va.vhdl"));
	}

	private VHDLStructureBuilder.ArchitectureFileFinder initAdderFinder() throws URISyntaxException {
		URI uri = VHDLStructureBuilderTest.class.getResource("./../../designs/add4/add4.vhdl").toURI();
		File sourceFile = new File(uri);
		return new VHDLStructureBuilder.ArchitectureFileFinder(sourceFile);
	}
}
