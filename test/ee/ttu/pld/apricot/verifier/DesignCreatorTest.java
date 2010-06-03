package ee.ttu.pld.apricot.verifier;

import org.junit.Before;
import org.junit.Test;
import ui.ConverterSettings;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.05.2010
 * <br>Time: 11:39:50
 */
public class DesignCreatorTest {
	private Statistics statistics;

	@Before public void createStatistics() {
		statistics = Statistics.createByteArrayStatistics();
	}

	@Test public void skipMissingFile() {
		Collection<Design> designs = DesignCreator.create(Collections.singletonList("m:\\someNonExistentFile.buba"), statistics);
		assertNotNull("DesignCreator.create(): should return an empty collection for missing file. Actual: null.", designs);
		assertEquals("DesignCreator.create(): should create an empty design collection from a missing file.", 0, designs.size());
		String message = statistics.getMessage();
		assertTrue("DesignCreator.create(): should write to OutputStream about missing file.\nActual: " + message,
				message.startsWith(Statistics.SKIPPING_NON_EXISTENT_FILE));
	}
	@Test public void skipMissingVHDLFile() {
		Collection<Design> designs = DesignCreator.create(Collections.singletonList("D:\\WORKSPACE\\tr\\b13_M_EX.agm"), statistics);
		assertNotNull("DesignCreator.create(): should return an empty collection for missing VHDL file.\nActual: null.", designs);
		assertEquals("DesignCreator.create(): should create an empty design collection from a file with missing VHDL file.", 0, designs.size());
		String message = statistics.getMessage();
		assertTrue("DesignCreator.create(): should write to OutputStream about missing VHDL file.\nActual: " + message,
				message.startsWith(Statistics.SKIPPING_WITHOUT_VHDL));
	}
	@Test public void skipWhenParseFails() {
		Collection<Design> designs = DesignCreator.create(
				Collections.singletonList("D:\\Programming\\Apricot\\DESIGNS\\branch\\ITC99\\orig\\b11\\b11_F.agm"), statistics);
		assertNotNull("DesignCreator.create(): should return an empty collection when settings' parse fails. Actual: null.", designs);
		assertEquals("DesignCreator.create(): should create an empty design collection when settings' parse fails.", 0, designs.size());
		String message = statistics.getMessage();
		assertTrue("DesignCreator.create(): should write to OutputStream about failed parse.\nActual: " + message,
				message.startsWith(ConverterSettings.PARSE_FAILED_FOR));
	}
	@Test public void createCorrectFiles() {
		Collection<Design> designs = DesignCreator.create(Arrays.asList(
				"D:\\Programming\\Apricot\\DESIGNS\\branch\\ITC99\\orig\\b13\\tr_E\\b13_M_FU.agm",
				"D:\\Programming\\Apricot\\DESIGNS\\branch\\ITC99\\orig\\b13\\tr_E\\b13_M_GR.agm"), null);
		assertNotNull("DesignCreator.create(): should return collection for correct files.\nActual: null.", designs);
		assertEquals("DesignCreator.create(): should create collection with size 2 for correct files.", 2, designs.size());
	}
	@Test public void loadCorrectNumberOfTestDesigns() {
		Collection<String> designPaths = DesignCreator.loadTestDesignsPaths();
		assertNotNull("DesignCreator.loadTestDesignsPaths(): non-null designs expected. Actual: null", designPaths);
		assertEquals(81, designPaths.size());
	}
	@Test public void createDefaultFiles() {
		int defaultSize = DesignCreator.loadTestDesignsPaths().size();
		Collection<Design> designs = DesignCreator.create(null);
		assertNotNull("DesignCreator.create(): should return collection for default files.\nActual: null.", designs);
		assertEquals("DesignCreator.create(): should create collection with size " + defaultSize + " for default files.",
				defaultSize, designs.size());
	}
}
