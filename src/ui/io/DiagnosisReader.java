package ui.io;

import io.QuietCloser;
import ui.base.VariableItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Anton Chepurov
 */
public class DiagnosisReader {

	private static final String SCORE1_START_TEXT = ".SCORE1_CANDIDATES";
	private static final String SCORE2_START_TEXT = ".SCORE2_CANDIDATES";
	private static final String SCORE1_END_TEXT = ";.SCORE1_DETECTED";
	private static final String SCORE2_END_TEXT = ";.SCORE2_DETECTED";

	private final File diagnosisFile;

	private Collection<VariableItem> candidates1;
	private Collection<VariableItem> candidates2;

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
				} else if (line.startsWith(SCORE1_END_TEXT) || line.startsWith(SCORE2_END_TEXT)) {
					filling = Filling.NONE;
					continue;
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

	@SuppressWarnings({"EnumeratedClassNamingConvention"})
	private enum Filling {
		Candidates1, Candidates2, NONE
	}
}
