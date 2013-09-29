package ui;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;
import static ui.BusinessLogic.ParserID.*;
import static ui.BusinessLogic.HLDDRepresentationType.*;

/**
 * @author Anton Chepurov
 */
public class ConverterSettingsTest {

	@Test
	public void correctHasAmongstParents() {
		File behDDFile = new File("/ITC99/trees/b00/b00.vhd");
		File behFile = new File("/ITC99/orig/b13/tr_E/b13.vhd");

		assertTrue("ConverterSettings.hasAmongstParents(): should detect 'trees'-named parent in " +
				behDDFile.getAbsolutePath(), ConverterSettings.hasAmongstParents("trees", behDDFile));
		assertFalse("ConverterSettings.hasAmongstParents(): should not detect 'trees'-named parent in " +
				behFile.getAbsolutePath(), ConverterSettings.hasAmongstParents("trees", behFile));

	}
	/*
	* Check VALIDATION failures
	* */

	@Test(expected = ExtendedException.class)
	public void validateParserId() throws ExtendedException {
		try {
			new ConverterSettings.Builder(null, null, null).build();
		} catch (ExtendedException e) {
			assertEquals("ConverterSettings: validate(): validation should fail directly for uninitialised parserId.",
					ConverterSettings.PARSER_ID_IS_NULL, e.getMessage());
			throw e;
		}
	}

	@Test(expected = ExtendedException.class)
	public void validateSourceFile() throws ExtendedException {
		try {
			new ConverterSettings.Builder(VhdlBeh2HlddBeh, null, null).build();
		} catch (ExtendedException e) {
			assertEquals("ConverterSettings: validate(): validation should fail directly for missing source file.",
					ConverterSettings.MISSING_SOURCE_FILE, e.getMessage());
			throw e;
		}
	}

	@Test(expected = ExtendedException.class)
	public void validatePslFile() throws ExtendedException {
		try {
			new ConverterSettings.Builder(VhdlBeh2HlddBeh, new File(""), null).build();
		} catch (ExtendedException e) {
			assertEquals("ConverterSettings: validate(): validation should fail directly for missing PSL/destination file.",
					ConverterSettings.MISSING_DESTINATION_FILE, e.getMessage());
			throw e;
		}
	}

	@Test(expected = ExtendedException.class)
	public void validateBaseModelFile() throws ExtendedException {
		try {
			new ConverterSettings.Builder(PSL2THLDD, new File(""), new File("")).build();
		} catch (ExtendedException e) {
			assertEquals("ConverterSettings: validate(): validation should fail directly for missing base file.",
					ConverterSettings.MISSING_BASE_HLDD_MODEL_FILE, e.getMessage());
			throw e;
		}
	}

	@Test(expected = ExtendedException.class)
	public void validateHlddType() throws ExtendedException {
		try {
			new ConverterSettings.Builder(VhdlBeh2HlddBeh, new File(""), new File("")).build();
		} catch (ExtendedException e) {
			assertEquals("ConverterSettings: validate(): validation should fail directly for uninitialised hlddType.",
					ConverterSettings.HLDD_TYPE_IS_NULL, e.getMessage());
			throw e;
		}
	}

	@Test(expected = ExtendedException.class)
	public void validateHlddTypeForDD() throws ExtendedException {
		try {
			new ConverterSettings.Builder(VhdlBehDd2HlddBeh, new File(""), new File("")).build();
		} catch (ExtendedException e) {
			assertEquals("ConverterSettings: validate(): validation should fail directly for uninitialised hlddType.",
					ConverterSettings.HLDD_TYPE_IS_NULL, e.getMessage());
			throw e;
		}
	}
	/*
	* Check PARSE failures
	* */

	@Test(expected = ConverterSettings.ConverterSettingsParseException.class)
	public void rejectIllegalExtension() throws ConverterSettings.ConverterSettingsParseException {
		ConverterSettings.parse("b03_F_FU.VHD");
	}

	@Test(expected = ConverterSettings.ConverterSettingsParseException.class)
	public void rejectIllegalCSMode() throws ConverterSettings.ConverterSettingsParseException {
		ConverterSettings.parse("B03.agm");
	}

	@Test(expected = ConverterSettings.ConverterSettingsParseException.class)
	public void rejectIllegalCompactnessMode() throws ConverterSettings.ConverterSettingsParseException {
		ConverterSettings.parse("B03_FU.agm");
	}
	/*
	* Check CORRECT PARSES
	* */

	@Test
	public void testParse() throws ConverterSettings.ConverterSettingsParseException, ExtendedException {

		BusinessLogic.ParserID parserId;
		ConverterSettings correctSettings;
		ConverterSettings settings;
		String destinationFilePath;

		/* VhdlBeh2HlddBeh and VhdlBehDd2HlddBeh */
		for (int i = 0; i < 2; i++) {
			File sourceFile;
			String parent;
			if (i == 0) {
				parserId = VhdlBeh2HlddBeh;
				parent = "/tr/";
				sourceFile = new File(parent + "b03.vhd");
			} else {
				parserId = VhdlBehDd2HlddBeh;
				parent = "/tr/trees/";
				sourceFile = new File(parent + "b03.vhd");
			}

			// FULL
			destinationFilePath = parent + "b03_F_FU.agm";
			correctSettings = build(parserId, sourceFile, new File(destinationFilePath))
					.setHlddType(FULL_TREE).build();
			settings = ConverterSettings.parse(destinationFilePath);
			assertEquals("Settings parsed incorrectly for " + parserId + " (" + destinationFilePath + ")", correctSettings, settings);

			destinationFilePath = parent + "b03_F_GR.agm";
			correctSettings = build(parserId, sourceFile, new File(destinationFilePath))
					.setHlddType(FULL_TREE)
					.setDoCreateCSGraphs(true).build();
			settings = ConverterSettings.parse(destinationFilePath);
			assertEquals("Settings parsed incorrectly for " + parserId + " (" + destinationFilePath + ")", correctSettings, settings);

			destinationFilePath = parent + "b03_F_EX.agm";
			correctSettings = build(parserId, sourceFile, new File(destinationFilePath))
					.setHlddType(FULL_TREE)
					.setDoCreateExtraCSGraphs(true).build();
			settings = ConverterSettings.parse(destinationFilePath);
			assertEquals("Settings parsed incorrectly for " + parserId + " (" + destinationFilePath + ")", correctSettings, settings);

			destinationFilePath = parent + "b03_F_FL.agm";
			correctSettings = build(parserId, sourceFile, new File(destinationFilePath))
					.setHlddType(FULL_TREE)
					.setDoFlattenConditions(true).build();
			settings = ConverterSettings.parse(destinationFilePath);
			assertEquals("Settings parsed incorrectly for " + parserId + " (" + destinationFilePath + ")", correctSettings, settings);
			// FULL 4 RTL
			destinationFilePath = parent + "b03_F4_EX.agm";
			correctSettings = build(parserId, sourceFile, new File(destinationFilePath))
					.setHlddType(FULL_TREE_4_RTL)
					.setDoCreateExtraCSGraphs(true).build();
			settings = ConverterSettings.parse(destinationFilePath);
			assertEquals("Settings parsed incorrectly for " + parserId + " (" + destinationFilePath + ")", correctSettings, settings);
			// MIN
			destinationFilePath = parent + "b03_M_FL.agm";
			correctSettings = build(parserId, sourceFile, new File(destinationFilePath))
					.setHlddType(MINIMIZED)
					.setDoFlattenConditions(true).build();
			settings = ConverterSettings.parse(destinationFilePath);
			assertEquals("Settings parsed incorrectly for " + parserId + " (" + destinationFilePath + ")", correctSettings, settings);
			// RED
			destinationFilePath = parent + "b03_R_GR.agm";
			correctSettings = build(parserId, sourceFile, new File(destinationFilePath))
					.setHlddType(REDUCED)
					.setDoCreateCSGraphs(true).build();
			settings = ConverterSettings.parse(destinationFilePath);
			assertEquals("Settings parsed incorrectly for " + parserId + " (" + destinationFilePath + ")", correctSettings, settings);
		}

		/* HlddBeh2HlddRtl */
		parserId = HlddBeh2HlddRtl;
		destinationFilePath = "/tr/b13_M_EX_RTL.agm";
		correctSettings = new ConverterSettings.Builder(parserId, new File("/tr/b13_M_EX.agm"),
				new File(destinationFilePath))
				.setHlddType(MINIMIZED)
				.setDoCreateExtraCSGraphs(true).build();
		settings = ConverterSettings.parse(destinationFilePath);
		assertEquals("Settings parsed incorrectly for " + parserId + " (" + destinationFilePath + ")", correctSettings, settings);

		/* PSL2THLDD */
		parserId = PSL2THLDD;
		destinationFilePath = "/tr/b13_M_EX.tgm";
		correctSettings = new ConverterSettings.Builder(parserId, new File("/tr/b13_M_EX.psl"),
				new File(destinationFilePath))
				.setBaseModelFile(new File("/tr/b13_M_EX.agm")).build();
		settings = ConverterSettings.parse(destinationFilePath);
		assertEquals("Settings parsed incorrectly for " + parserId + " (" + destinationFilePath + ")", correctSettings, settings);

	}

	private ConverterSettings.Builder build(BusinessLogic.ParserID parserId, File sourceFile, File pslFile) {
		return new ConverterSettings.Builder(parserId, sourceFile, pslFile);
	}
}
