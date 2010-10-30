package ui.io;

import io.QuietCloser;
import ui.base.NodeItem;
import ui.base.SplitCoverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Anton Chepurov
 */
public class CoverageReader {

	private static final String NODE_START_TEXT = ".NODE_COVERAGE";
	private static final String EDGE_START_TEXT = ".EDGE_COVERAGE";
	private static final String TOGGLE_START_TEXT = ".TOGGLE_COVERAGE";
	private static final String DEFAULT_COMMENT = ";";

	private final File covFile;
	private Collection<NodeItem> uncoveredNodeItems;
	private SplitCoverage nodeCoverage;
	private SplitCoverage edgeCoverage;
	private SplitCoverage toggleCoverage;

	public CoverageReader(File covFile) {
		this.covFile = covFile;
	}

	public void read() throws IOException {
		uncoveredNodeItems = new HashSet<NodeItem>();
		BufferedReader reader = null;
		boolean hasSomethingToFind = true;
		try {
			reader = new BufferedReader(new FileReader(covFile));
			String line;
			while ((line = reader.readLine()) != null && hasSomethingToFind) {
				line = line.trim();
				/* Skip comments and empty lines */
				if (line.startsWith(DEFAULT_COMMENT) || line.length() == 0) continue;

				if (line.startsWith(NODE_START_TEXT)) {

					nodeCoverage = parseCoverage(line, NODE_START_TEXT, SplitCoverage.NODE_COVERAGE);

					while ((line = reader.readLine()) != null && !line.startsWith(EDGE_START_TEXT)) {
						line = line.trim();
						/* Skip comments and empty lines */
						if (line.startsWith(DEFAULT_COMMENT) || line.length() == 0) continue;

						/* Parse indices */
						String[] indicesAsStrings = line.split("\\s");
						if (indicesAsStrings.length != 2) {
							throw new IOException("Coverage file is malformed");
						}
						int graphIndex = Integer.parseInt(indicesAsStrings[0].trim());
						int nodeIndex = Integer.parseInt(indicesAsStrings[1].trim());
						uncoveredNodeItems.add(new NodeItem(graphIndex, nodeIndex));
					}
				}
				//todo: do like in ConverterSettings.loadSmartComment(): use isReadingSmth flag...
				if (line.startsWith(EDGE_START_TEXT)) {
					edgeCoverage = parseCoverage(line, EDGE_START_TEXT, SplitCoverage.EDGE_COVERAGE);
				}

				if (line.startsWith(TOGGLE_START_TEXT)) {
					toggleCoverage = parseCoverage(line, TOGGLE_START_TEXT, SplitCoverage.TOGGLE_COVERAGE);
					hasSomethingToFind = false;
				}

			}

		} finally {
			QuietCloser.closeQuietly(reader);
		}
	}

	private SplitCoverage parseCoverage(String line, String lineStartText, String title) {
		String[] numHolder = line.substring(lineStartText.length(), line.indexOf("(")).trim().split("/");
		int covered = Integer.parseInt(numHolder[0].trim());
		int total = Integer.parseInt(numHolder[1].trim());
		return new SplitCoverage(covered, total, title, null);
	}

	public SplitCoverage getNodeCoverage() {
		return nodeCoverage;
	}

	public SplitCoverage getEdgeCoverage() {
		return edgeCoverage;
	}

	public SplitCoverage getToggleCoverage() {
		return toggleCoverage;
	}

	public Collection<NodeItem> getUncoveredNodeItems() {
		if (uncoveredNodeItems == null) {
			try {
				read();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return uncoveredNodeItems;
	}
}
