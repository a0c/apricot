package parsers.tgm;

import io.scan.HLDDScanner;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static ui.FileDependencyResolver.*;

/**
 * @author Anton Chepurov
 */
public class ModelDataLoader {

	private final File patternFile;
	private HLDDScanner scanner = null;
	private FileType sourceFileType = null;
	private List<String> variableNames = null;
	private int graphCount = -1;
	private Collection<Integer> graphIndices = null;
	private Collection<Integer> inputIndices = null;

	public ModelDataLoader(File patternFile) {
		this.patternFile = patternFile;
		File modelFile = null;
		try {
			sourceFileType = FileType.parseFileType(patternFile);
			modelFile = sourceFileType.deriveModelFile(patternFile);
			if (modelFile == null || !modelFile.exists()) {
				scanner = null;
			} else {
				scanner = new HLDDScanner(modelFile);
			}
		} catch (Exception e) {
			if (modelFile != null) {
				throw new RuntimeException("Unexpected bug: cannot create " + HLDDScanner.class.getSimpleName() +
						" from modelFile " + modelFile.getAbsolutePath());
			}
		}
	}

	public void collect() {
		if (isModelFileMissing()) {
			variableNames = null;
			return;
		}
		variableNames = new LinkedList<String>();
		graphIndices = new HashSet<Integer>();
		inputIndices = new HashSet<Integer>();
		try {
			String token;
			int varIndex = -1;
			while ((token = scanner.next()) != null) {
				if (token.startsWith("VAR#") && isValidVariable(++varIndex)) {
					String varName = token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")).trim();
					if (varName.contains("@")) {
						varName = varName.substring(0, varName.indexOf("@"));
					}
					variableNames.add(varName);
				} else if (token.startsWith("STAT#")) {
					int inputCount = 0, constCount = 0, funcCount = 0;
					/* Parse statistics */
					String[] stats = token.split(",");
					for (String stat : stats) {
						if (stat.contains("GRPS")) {
							graphCount = Integer.parseInt(stat.trim().split("\\s")[0].trim());
						} else if (stat.contains("INPS")) {
							inputCount = Integer.parseInt(stat.trim().split("\\s")[0].trim());
						} else if (stat.contains("CONS")) {
							constCount = Integer.parseInt(stat.trim().split("\\s")[0].trim());
						} else if (stat.contains("FUNS")) {
							funcCount = Integer.parseInt(stat.trim().split("\\s")[0].trim());
						}
					}

					/* Collect variable indices*/
					addIndices(0, inputCount, inputIndices);
					addIndices(inputCount + constCount + funcCount, graphCount, graphIndices);
				}
			}
		} finally {
			scanner.close();
		}

	}

	public boolean isModelFileMissing() {
		return scanner == null;
	}

	public String getMissingFileMessage() {
		return sourceFileType.getMissingFileMessage(patternFile);
	}

	private boolean isValidVariable(int index) {
		switch (sourceFileType) {
			case CHK:
			case SIM:
				return graphIndices.contains(index) || inputIndices.contains(index);
			case TST:
				return inputIndices.contains(index);
			default:
				return false;
		}
	}

	private void addIndices(int start, int length, Collection<Integer> destCollection) {
		int end = start + length;
		for (int i = start; i < end; i++) {
			destCollection.add(i);
		}
	}

	public String[] getVariableNames() {
		if (variableNames == null) {
			collect();
		}
		return variableNames == null ? null : variableNames.toArray(new String[variableNames.size()]);
	}

	public int getGraphCount() {
		if (graphCount == -1) {
			collect();
		}
		return graphCount;
	}

	public Collection<Integer> getInputIndices() {
		if (inputIndices == null) {
			collect();
		}
		return inputIndices;
	}

	public Collection<Integer> getGraphIndices() {
		if (graphIndices == null) {
			collect();
		}
		return graphIndices;
	}

	public boolean isSourceCHK() {
		return sourceFileType == FileType.CHK;
	}

	public boolean isSourceSIM() {
		return sourceFileType == FileType.SIM;
	}

	@SuppressWarnings({"EnumeratedConstantNamingConvention"})
	private enum FileType {
		CHK, SIM, TST;
		private static final String AGM_EXTENSION = ".agm";
		private static final String TGM_EXTENSION = ".tgm";


		public File deriveModelFile(File patternFile) {
			switch (this) {
				case CHK:
					return deriveFileFrom(patternFile, TGM_EXTENSION);
				case SIM:
					return deriveFileFrom(patternFile, AGM_EXTENSION);
				case TST:
					return deriveFileFrom(patternFile, AGM_EXTENSION);
				default:
					return null;
			}
		}

		public String getMissingFileMessage(File patternFile) {
			switch (this) {
				case CHK:
					return "To map CHK stimuli with signal names, please, place TGM file (" +
							deriveFilePathFrom(patternFile, TGM_EXTENSION) + ") into the " +
							"following directory:\n" + patternFile.getParentFile().getAbsolutePath();
				case SIM:
					return "To map SIM stimuli with signal names, please, place AGM file (" +
							deriveFilePathFrom(patternFile, AGM_EXTENSION) + ") into the " +
							"following directory:\n" + patternFile.getParentFile().getAbsolutePath();
				case TST:
					return "To map TST stimuli with signal names, please, place AGM file (" +
							deriveFilePathFrom(patternFile, AGM_EXTENSION) + ") into the " +
							"following directory:\n" + patternFile.getParentFile().getAbsolutePath();
				default:
					return "";
			}
		}

		public static FileType parseFileType(File patternFile) {
			if (isCHK(patternFile))
				return CHK;
			else if (isSIM(patternFile))
				return SIM;
			else if (isTST(patternFile))
				return TST;
			else return null;
		}
	}
}
