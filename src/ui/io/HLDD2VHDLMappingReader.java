package ui.io;

import base.SourceLocation;
import io.QuietCloser;
import ui.base.HLDD2VHDLMapping;
import ui.base.NodeItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * @author Anton Chepurov
 */
public class HLDD2VHDLMappingReader {

	private final static int OFFSET = 1;
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

	private HLDD2VHDLMapping mapping = null;
	private final File mappingFile;

	public HLDD2VHDLMappingReader(File mappingFile) {
		this.mappingFile = mappingFile;
	}

	public void read() throws IOException {
		mapping = new HLDD2VHDLMapping();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(mappingFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] lineParts = line.split(":");
				if (lineParts.length != 2) {
					throw new IOException("Mapping file is malformed");
				}

				/* Parse line numbers */
				SourceLocation sourceLocation = null;
				for (String fileLines : lineParts[1].split(";")) {

					String[] fileNameAndLines = fileLines.trim().split(" ", 2);

					String fileName = fileNameAndLines[0].trim();

					if (fileNameAndLines.length < 2 || NUMBER_PATTERN.matcher(fileName).matches()) {
						throw new IOException("Mapping file is malformed (probably old format): missing VHDL file name on line:\n" + line);
					}

					String[] lineNumbersAsStrings = fileNameAndLines[1].split(",");
					Collection<Integer> lines = new HashSet<Integer>();
					for (String lineNumberAsString : lineNumbersAsStrings) {
						lines.add(Integer.parseInt(lineNumberAsString.trim()) - OFFSET);
					}

					SourceLocation nextLocation = new SourceLocation(new File(mappingFile.getParent(), fileName), lines);

					if (sourceLocation == null) {
						sourceLocation = nextLocation;
					} else {
						sourceLocation = sourceLocation.addSource(nextLocation);
					}
				}

				/* Parse indices */
				String[] indices = lineParts[0].split("\\s");
				if (indices.length == 2) {
					int graphIndex = Integer.parseInt(indices[0].trim());
					int nodeIndex = Integer.parseInt(indices[1].trim());
					mapping.addMapping(new NodeItem(graphIndex, nodeIndex), sourceLocation);
				} //todo: EdgeMappingItem, etc...
			}
		} finally {
			QuietCloser.closeQuietly(reader);
		}
	}

	public HLDD2VHDLMapping getMapping() {
		if (mapping == null) {
			try {
				read();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return mapping;
	}
}
