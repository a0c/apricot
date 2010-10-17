package parsers.vhdl;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.easymock.classextension.EasyMock.*;
import static org.easymock.EasyMock.expect;

import io.scan.VHDLScanner;
import io.scan.VHDLToken;
import base.vhdl.structure.Entity;
import base.vhdl.structure.Constant;
import base.vhdl.structure.Signal;

import java.util.*;

/**
 * @author Anton Chepurov
 */
public class VHDLStructureParserTest {

	@SuppressWarnings({"InstanceMethodNamingConvention"})
	@Test
	public void testTYPE_ENUM_DECL() throws Exception {
		/* Mock scanner */
		VHDLScanner mockVhdlScanner = createMock(VHDLScanner.class);
		expect(mockVhdlScanner.next()).andReturn(new VHDLToken(VHDLToken.Type.TYPE_ENUM_DECL, "TYPE STATETYPE IS ( WAITING , INIT , MULTIN , ADDIT , SHIFTING , ENDCOMP , ACCBUS ) ;"));
		expect(mockVhdlScanner.next()).andReturn(new VHDLToken(VHDLToken.Type.SIGNAL_DECL, "SIGNAL STATE, NSTATE : STATETYPE ;"));
		expect(mockVhdlScanner.next()).andReturn(null);
		replay(mockVhdlScanner);
		/* Create builder and build file base */
		VHDLStructureBuilder structureBuilder = new VHDLStructureBuilder();
		structureBuilder.buildEntity("EntityName");
		structureBuilder.buildArchitecture("ArchitectureName", "ArchitectureAffiliation");
		/* Create parser and trigger single parse step */
		VHDLStructureParser parser = new VHDLStructureParser(mockVhdlScanner, structureBuilder);
		parser.parse();

		/* ASSERT */
		Entity structure = structureBuilder.getVHDLStructure();
		Set<Constant> constantSet = structure.getArchitecture().getConstants();
		assertNotNull("Constants are not parsed", constantSet);
		assertEquals("Incorrect number of parsed constants", 7, constantSet.size());

		assertTrue("User-declared type is not saved", structureBuilder.containsType("STATETYPE"));
		assertEquals("Incorrect length for the user-declared type",
				2, structureBuilder.getType("STATETYPE").getHighestSB());

		Set<Signal> signalSet = structure.getArchitecture().getSignals();
		assertNotNull("Signals are not parsed", signalSet);
		assertEquals("Incorrect number of parsed signals", 2, signalSet.size());
		for (Signal signal : signalSet) {
			assertEquals("Incorrect register length is set for signals", 2, signal.getType().getHighestSB());
		}
	}

	@Test
	public void correctSensitivityList() {
		Object[][] sensitivityLists = {
				{"PROCESS (Clk, Reset)", new String[]{"Clk", "Reset"}},
				{"seq: PROCESS (Clk, Rst)", new String[]{"Clk", "Rst"}},
				{"some_name : PROCESS (Living, Action)", new String[]{"Living", "Action"}},
				{"some_name : PROCESS (Living)", new String[]{"Living"}},
				{"some_name : PROCESS (Living,    asdf,wewerw,wew)", new String[]{"Living", "asdf", "wewerw", "wew"}},
				{"some_name : PROCESS ()", new String[0]},
				{"some_name : PROCESS", new String[0]}
		};
		for (Object[] sensitivityList : sensitivityLists) {
			Collection<String> createdList = VHDLStructureParser.extractSensitivityList(((String) sensitivityList[0]));
			assertArrayEquals(((String[]) sensitivityList[1]), createdList.toArray(new String[createdList.size()]));
		}
	}

}
