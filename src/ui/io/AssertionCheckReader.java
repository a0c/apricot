package ui.io;

import ui.utils.uiWithWorker.UIInterface;
import io.QuietCloser;

import java.io.*;
import java.util.*;

import parsers.tgm.ModelDataLoader;

/**
 * @author Anton Chepurov
 */
public class AssertionCheckReader {

	private BufferedReader reader;
	private ModelDataLoader modelDataLoader;
	private int patternsSize = -1;
	private long[][] variablePatterns = null;
	private char[][] assertionPatterns = null;
	private List<HashSet<Long>> differentValuesByVariableIndex = null;
	private static final String DEFAULT_COMMENT = ";";
	private final File inputFile;
	private UIInterface uiHolder;
	private int uiUpdateFreqCount;
	private static final long X_VALUE = Long.MIN_VALUE;
	private Collection<Integer> validVarIndices;

	/**
	 * @param inputFile where to read vectors with assertion results from
	 * @param uiHolder UI to update while reading vectors
	 * @param uiUpdateFreqCount update UI with each <code>uiUpdateFreqCount</code>-th read vector
	 */
	public AssertionCheckReader(File inputFile, UIInterface uiHolder, int uiUpdateFreqCount) {
		this.inputFile = inputFile;
		this.modelDataLoader = new ModelDataLoader(inputFile);
		this.uiHolder = uiHolder;
		this.uiUpdateFreqCount = uiUpdateFreqCount;
	}

	public void readAssertions() throws IOException {
		String line;
		int lineCount = 0;
		String[] valueLines;

		try {
			reader = new BufferedReader(new FileReader(inputFile));

			/* Read number of patterns */
			patternsSize = readPatternsSize();

			/* Skip to patterns and Init variablePatterns and assertionPatterns */
			while (!nextLine().equalsIgnoreCase(".PATTERNS")) {
			}
			initPatternsStorage(patternsSize);

			/* Read the patterns */
			while ((line = nextLine()) != null && !Thread.interrupted()) {
				if (lineCount == patternsSize) break;
				valueLines = line.split("\\s");
				int assertionIndexBound = valueLines.length - assertionPatterns.length - 1;
				for (int signalIndex = 0, variableIndex = 0, assertionIndex = 0; signalIndex < valueLines.length; signalIndex++) {
					/* Skip discarded signals */
					if (!validVarIndices.contains(signalIndex)) continue;
					if (signalIndex <= assertionIndexBound) {
						/* Is variable signal index */
						addVariablePatternValue(lineCount, valueLines[signalIndex], variableIndex++);
					} else {
						/* Is assertion signal index */
						assertionPatterns[assertionIndex++][lineCount] = valueLines[signalIndex].toCharArray()[0];
					}
				}
				/* Update UIHolder if it exists */
				if (uiHolder != null && isUpdatingValue(lineCount, patternsSize)) {
					uiHolder.updateProgressBar(lineCount, patternsSize);
				}

				lineCount++;
			}
			if (Thread.interrupted()) {
				System.out.println("");
			}
		} finally {
			QuietCloser.closeQuietly(reader);
		}
	}

	private void addVariablePatternValue(int lineCount, String valueLine, int variableIndex) throws IOException {
		try {
			/* Number value */
			long valueLong = Long.parseLong(valueLine);
			variablePatterns[variableIndex][lineCount] = valueLong;
			if (differentValuesByVariableIndex.get(variableIndex).size() < 3) { // if 3 or more, then already a non-boolean
				differentValuesByVariableIndex.get(variableIndex).add(valueLong);
			}
		} catch (NumberFormatException e) {
			/* X value */
			if (valueLine.equalsIgnoreCase("X")) {
				variablePatterns[variableIndex][lineCount] = X_VALUE;
			} else {
				throw new IOException("Unsupported variable value is read: " + valueLine);
			}
		}
	}

	private boolean isUpdatingValue(int lineCount, int patternsSize) {
		int itemChunk = patternsSize / (100 / uiUpdateFreqCount);
		return itemChunk == 0 || lineCount % itemChunk == 0;
	}

	private void initPatternsStorage(int patternsSize) throws IOException {

		/* Obtain numbers of PATTERNS and ASSERTIONS */
		int patternsCount, assertionsCount;
		if (modelDataLoader.isModelFileMissing()) {
			throw new IOException(modelDataLoader.getMissingFileMessage());
		}
		ModelDataLoader.FileType sourceFileType = modelDataLoader.getSourceFileType();
		if (sourceFileType == ModelDataLoader.FileType.CHKfile) {
			/* File contains PATTERNS and ASSERTIONS. */
			validVarIndices = modelDataLoader.getInputIndices();
			validVarIndices.addAll(modelDataLoader.getGraphIndices());

			assertionsCount = modelDataLoader.getGraphCount();
			patternsCount = validVarIndices.size() - assertionsCount;

		} else {
			/* File contains PATTERNS only. */
			validVarIndices = modelDataLoader.getInputIndices();
			if (sourceFileType == ModelDataLoader.FileType.SIMfile) {
				validVarIndices.addAll(modelDataLoader.getGraphIndices());
			}

			patternsCount = validVarIndices.size();
			assertionsCount = 0;

		}
		/* Init storage */
		variablePatterns = new long[patternsCount][];
		assertionPatterns = new char[assertionsCount][];
		differentValuesByVariableIndex = new ArrayList<HashSet<Long>>(patternsCount);
		for (int i = 0; i < patternsCount; i++) {
			variablePatterns[i] = new long[patternsSize];
			differentValuesByVariableIndex.add(i, new HashSet<Long>());
		}
		for (int i = 0; i < assertionsCount; i++) {
			assertionPatterns[i] = new char[patternsSize];
		}
	}

	private int readPatternsSize() throws IOException {
		String line;
		int patternsSize = -1;
		while ((line = nextLine()) != null) {
			if (line.startsWith(".VECTORS")) {
				patternsSize = Integer.parseInt(line.substring(8).trim());
				break;
			}
		}
		if (patternsSize == -1) {
			throw new IOException("Missing number of patterns.");
		} else return patternsSize;
	}

	private String nextLine() throws IOException {
		String line;

		while (true) {
			line = reader.readLine();

			/* EOF reached */
			if (line == null) break;
			/* Trim line */
			line = line.trim();
			/* Empty line */
			if (line.isEmpty()) continue;
			/* Comment line */
			if (line.startsWith(DEFAULT_COMMENT)) continue;
			/* Exit cycle */
			break;
		}

		return line;
	}

	public long[][] getVariablePatterns() {
		if (variablePatterns == null) {
			readSilently();
		}
		return variablePatterns;
	}

	public char[][] getAssertionPatterns() {
		if (assertionPatterns == null) {
			readSilently();
		}
		return assertionPatterns;
	}

	public Collection getBooleanIndices() {
		TreeSet<Integer> booleanIndices = new TreeSet<Integer>();
		NEXT_VARIABLE:
		for (int varIndex = 0; varIndex < differentValuesByVariableIndex.size(); varIndex++) {
			HashSet<Long> differentValuesSet = differentValuesByVariableIndex.get(varIndex);
			/* Skip those variables, that have more than 2 different values */
			if (differentValuesSet.size() > 2) {
				continue;
			}
			/* If some of the values is not a boolean one, then skip this variable */
			for (Long value : differentValuesSet) {
				if (value != null && !isAllowed(value)) {
					continue NEXT_VARIABLE;
				}
			}
			/* The variable contains only boolean values, so add it's index to booleanIndices */
			booleanIndices.add(varIndex);
		}

		return booleanIndices;
	}

	private boolean isAllowed(Long integer) {
		return integer == 0 || integer == 1;
	}

	public int getPatternsSize() {
		if (patternsSize == -1) {
			readSilently();
		}
		return patternsSize;
	}

	private void readSilently() {
		try {
			readAssertions();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
