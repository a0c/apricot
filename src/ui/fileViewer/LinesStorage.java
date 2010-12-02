package ui.fileViewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Anton Chepurov
 */
public class LinesStorage {

	private final Collection<Integer> nodesLines;
	private final Collection<Integer> edgesLines;
	private final Collection<Integer> candidates1Lines;
	private final Collection<Integer> candidates2Lines;
	private ArrayList<Integer> nodeLinesArray;
	private ArrayList<Integer> edgeLinesArray;
	private ArrayList<Integer> candidates1LinesArray;
	private ArrayList<Integer> candidates2LinesArray;

	private int offset = 0;

	public static LinesStorage emptyStorage() {
		return new LinesStorage.Builder().build();
	}

	private LinesStorage(Builder builder) {
		nodesLines = builder.nodesLines;
		edgesLines = builder.edgesLines;
		candidates1Lines = builder.candidates1Lines;
		candidates2Lines = builder.candidates2Lines;

		if (nodesLines != null) {
			nodeLinesArray = new ArrayList<Integer>(nodesLines);
			Collections.sort(nodeLinesArray);
		}
		if (edgesLines != null) {
			edgeLinesArray = new ArrayList<Integer>(edgesLines);
			Collections.sort(edgeLinesArray);
		}
		if (candidates1Lines != null) {
			candidates1LinesArray = new ArrayList<Integer>(candidates1Lines);
			Collections.sort(candidates1LinesArray);
		}
		if (candidates2Lines != null) {
			candidates2LinesArray = new ArrayList<Integer>(candidates2Lines);
			Collections.sort(candidates2LinesArray);
		}
	}

	public boolean isEmpty() {
		return nodesLines == null || nodesLines.isEmpty();
	}

	public boolean hasNodeLine(int line) {
		return nodesLines != null && nodesLines.contains(line + offset);
	}

	public boolean hasEdgeLine(int line) {
		return edgesLines != null && edgesLines.contains(line + offset);
	}

	public boolean hasCandidate1Line(int line) {
		return candidates1Lines != null && candidates1Lines.contains(line + offset);
	}

	public boolean hasCandidate2Line(int line) {
		return candidates2Lines != null && candidates2Lines.contains(line + offset);
	}

	public String generateNodeStat(int line) {
		if (!hasNodeLine(line)) {
			return null;
		}
		return "Uncovered node " + statFor(line, nodeLinesArray);
	}

	public String generateEdgeStat(int line) {
		if (!hasEdgeLine(line)) {
			return null;
		}
		return "Uncovered edge " + statFor(line, edgeLinesArray);
	}

	public String generateCandidate1Stat(int line) {
		if (!hasCandidate1Line(line)) {
			return null;
		}
		return "Candidate 1 " + statFor(line, candidates1LinesArray);
	}

	public String generateCandidate2Stat(int line) {
		if (!hasCandidate2Line(line)) {
			return null;
		}
		return "Candidate 2 " + statFor(line, candidates2LinesArray);
	}

	private String statFor(int line, ArrayList<Integer> linesArray) {
		return "(" + (linesArray.indexOf(line + offset) + 1) + "/" + linesArray.size() + ")";
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public static class Builder {
		private Collection<Integer> nodesLines = null;
		private Collection<Integer> edgesLines = null;
		private Collection<Integer> candidates1Lines = null;
		private Collection<Integer> candidates2Lines = null;

		public Builder nodes(Collection<Integer> nodesLines) {
			this.nodesLines = nodesLines;
			return this;
		}

		public Builder edges(Collection<Integer> edgesLines) {
			this.edgesLines = edgesLines;
			return this;
		}

		public Builder candidates1(Collection<Integer> candidates1Lines) {
			this.candidates1Lines = candidates1Lines;
			return this;
		}

		public Builder candidates2(Collection<Integer> candidates2Lines) {
			this.candidates2Lines = candidates2Lines;
			return this;
		}

		public LinesStorage build() {
			return new LinesStorage(this);
		}
	}
}
