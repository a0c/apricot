package base;

import java.io.File;
import java.util.*;

/**
 * @author Anton Chepurov
 */
public class SourceLocation {

	private final Map<File, TreeSet<Integer>> linesByFile;

	public SourceLocation(File sourceFile, Collection<Integer> sourceVhdlLines) {

		if (sourceFile == null || sourceVhdlLines == null) {
			throw new IllegalArgumentException("SourceLocation: NULL parameter passed to constructor");
		}
		if (sourceVhdlLines.isEmpty()) {
			throw new IllegalArgumentException("SourceLocation: EMPTY COLLECTION parameter passed to constructor");
		}

		TreeSet<Integer> vhdlLines = new TreeSet<Integer>(sourceVhdlLines);

		if (vhdlLines.first() < 0) {
			throw new IllegalArgumentException("SourceLocation: NEGATIVE SOURCE VHDL LINE passed to constructor");
		}

		linesByFile = new TreeMap<File, TreeSet<Integer>>();

		linesByFile.put(sourceFile, vhdlLines);

	}

	private SourceLocation(Map<File, TreeSet<Integer>> linesByFile) {
		this.linesByFile = linesByFile;
	}

	public SourceLocation addSource(SourceLocation source) {
		if (source == null) {
			return this;
		}
		return createFrom(Arrays.asList(this, source));
	}

	public Integer getFirstLine() throws HLDDException {
		if (linesByFile.size() != 1) {
			throw new HLDDException("SourceLocation: obtaining first line from source with multiple files: " + toString());
		}
		return linesByFile.values().iterator().next().first();
	}

	public Collection<File> getFiles() {
		return linesByFile.keySet();
	}

	public Collection<Integer> getLinesForFile(File file) {
		return linesByFile.get(file);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		for (Map.Entry<File, TreeSet<Integer>> entry : linesByFile.entrySet()) {

			sb.append(entry.getKey().getName()).append(" ");

			TreeSet<Integer> vhdlLines = entry.getValue();

			for (int vhdlLine : vhdlLines)
				sb.append(vhdlLine).append(", ");

			int length = sb.length();
			sb.delete(length - 2, length);

			sb.append("; ");
		}

		return sb.substring(0, sb.length() - 2);
	}

	public static SourceLocation createFrom(Collection<SourceLocation> sources) {

		Map<File, TreeSet<Integer>> linesByFile = new TreeMap<File, TreeSet<Integer>>();

		for (SourceLocation source : sources) {

			if (source == null) {
				continue;
			}

			for (Map.Entry<File, TreeSet<Integer>> entry : source.linesByFile.entrySet()) {

				File newFile = entry.getKey();
				TreeSet<Integer> newLines = entry.getValue();

				if (linesByFile.containsKey(newFile)) {

					TreeSet<Integer> existingLines = linesByFile.get(newFile);

					existingLines.addAll(newLines);

				} else {

					linesByFile.put(newFile, new TreeSet<Integer>(newLines));

				}
			}
		}

		return new SourceLocation(linesByFile);
	}
}
