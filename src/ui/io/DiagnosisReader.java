package ui.io;

import io.QuietCloser;
import ui.base.VariableItem;

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
public class DiagnosisReader {

	private static final String SCORE1_START_TEXT = ".SCORE1_CANDIDATES";
	private static final String SCORE2_START_TEXT = ".SCORE2_CANDIDATES";
	private static final Pattern ACTUAL_MUTATION_PATTERN = Pattern.compile("^Function .* replaced by .*");

	private final File diagnosisFile;

	private Collection<VariableItem> candidates1;
	private Collection<VariableItem> candidates2;
	private VariableItem actualMutation;

	public DiagnosisReader(File diagnosisFile) {
		this.diagnosisFile = diagnosisFile;
	}

	public void read() throws IOException {

		candidates1 = new HashSet<VariableItem>();
		candidates2 = new HashSet<VariableItem>();

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(diagnosisFile));
			String line;
			Filling filling = Filling.NONE;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith(";") || line.isEmpty()) {
					continue;
				}
				if (line.startsWith(SCORE1_START_TEXT)) {
					filling = Filling.Candidates1;
					continue;
				} else if (line.startsWith(SCORE2_START_TEXT)) {
					filling = Filling.Candidates2;
					continue;
				} else if (ACTUAL_MUTATION_PATTERN.matcher(line).matches()) {
					if (!(line.contains("[") && line.contains("]"))) {
						throw new RuntimeException("Malformed DGN file (or format is changed)." +
								"\nActual mutation line is expected to contain \'Var[XXX]\'. Actual: " + line);
					}
					int start = line.indexOf("[") + 1;
					int end = line.indexOf("]");
					line = line.substring(start, end);
					actualMutation = new VariableItem(Integer.parseInt(line));
				}

				switch (filling) {
					case Candidates1:
						candidates1.add(new VariableItem(Integer.parseInt(line)));
						break;
					case Candidates2:
						candidates2.add(new VariableItem(Integer.parseInt(line)));
						break;
				}
			}

		} finally {
			QuietCloser.closeQuietly(reader);
		}

	}

	public Collection<VariableItem> getCandidates1() {
		if (candidates1 == null) {
			try {
				read();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return candidates1;
	}

	public Collection<VariableItem> getCandidates2() {
		if (candidates2 == null) {
			try {
				read();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return candidates2;
	}

	public VariableItem getActualMutation() {
		if (actualMutation == null) {
			try {
				read();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return actualMutation;
	}

	@SuppressWarnings({"EnumeratedClassNamingConvention"})
	private enum Filling {
		Candidates1, Candidates2, NONE
	}
}
