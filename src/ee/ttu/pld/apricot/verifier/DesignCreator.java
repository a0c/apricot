package ee.ttu.pld.apricot.verifier;

import io.QuietCloser;
import ui.ConverterSettings;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.05.2010
 * <br>Time: 11:41:07
 */
public class DesignCreator {

	private static final File TEST_DESIGNS_FILE;
	static {
		File file;
		try {
			URI uri = DesignCreator.class.getResource("TestDesignsList.txt").toURI();
			file = new File(uri);
		} catch (URISyntaxException e) {
			file = null;
		}
		TEST_DESIGNS_FILE = file;
	}

	private DesignCreator() {}

	public static Collection<Design> create(Collection<String> hlddPaths, Statistics statistics) {

		LinkedList<Design> designs = new LinkedList<Design>();

		for (String hlddPath : hlddPaths) {

			if (hlddPath == null) {
				continue;
			}

			File hlddFile = new File(hlddPath);

			if (hlddFile.exists()) {

				try {
					ConverterSettings settings = ConverterSettings.parse(hlddPath);

					File sourceFile = settings.getSourceFile();

					if (!sourceFile.exists()) {
						statistics.skippedWithoutVHDL(hlddPath);
					} else
						designs.add(new Design(hlddFile, settings));

				} catch (ConverterSettings.ConverterSettingsParseException e) {
					statistics.skipped(e.getMessage()); // parse failed
				}
			} else {
				statistics.skippedNonExistent(hlddPath);
			}
		}

		return designs;
	}

	public static Collection<Design> create(Statistics statistics) {
		if (statistics == null) {
			statistics = Statistics.createConsoleStatistics();
		}
		return create(loadTestDesignsPaths(), statistics);
	}

	static Collection<String> loadTestDesignsPaths() {

		LinkedList<String> designPaths = new LinkedList<String>();

		if (TEST_DESIGNS_FILE == null || !TEST_DESIGNS_FILE.exists()) {
			return designPaths;
		}

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(TEST_DESIGNS_FILE)));
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("####") && !line.isEmpty()) {
					designPaths.add(line);
				}
			}
			QuietCloser.closeQuietly(br);

		} catch (FileNotFoundException e) {
			throw new RuntimeException("Cannot create FileInputStream from existing test file " + TEST_DESIGNS_FILE.getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException("Could not read from test file " + TEST_DESIGNS_FILE.getAbsolutePath());
		}
		return designPaths;
	}
}
