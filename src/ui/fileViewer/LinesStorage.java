package ui.fileViewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Anton Chepurov
 */
public class LinesStorage {

	private final Collection<Integer> mutationLines;
	private final Collection<Integer> nodesLines;
	private final Collection<Integer> edgesLines;
	private final Collection<Integer> candidates1Lines;
	private final Collection<Integer> candidates2Lines;
	private ArrayList<Integer> nodeLinesArray;
	private ArrayList<Integer> edgeLinesArray;
	private ArrayList<Integer> candidates1LinesArray;
	private ArrayList<Integer> candidates2LinesArray;
	private ArrayList<Integer> mutationLinesArray;

	private int offset = 0;
	private TableForm tableForm;

	public static LinesStorage emptyStorage() {
		return new LinesStorage.Builder().build();
	}

	private LinesStorage(Builder builder) {
		nodesLines = builder.nodesLines;
		edgesLines = builder.edgesLines;
		candidates1Lines = builder.candidates1Lines;
		candidates2Lines = builder.candidates2Lines;
		mutationLines = builder.mutationLines;

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
		if (mutationLines != null) {
			mutationLinesArray = new ArrayList<Integer>(mutationLines);
			Collections.sort(mutationLinesArray);
		}
	}

	public boolean hasMutation() {
		return mutationLines != null && !mutationLines.isEmpty();
	}

	public boolean hasMutationLine(int line) {
		return hasMutation() && mutationLines.contains(line + offset);
	}

	public int getFirstMutationLine() {
		if (!hasMutation()) {
			return -1;
		}
		return mutationLinesArray.get(0);
	}

	public boolean hasNodes() {
		return nodesLines != null && !nodesLines.isEmpty();
	}

	public boolean hasEdges() {
		return edgesLines != null && !edgesLines.isEmpty();
	}

	public boolean hasCandidates1() {
		return candidates1Lines != null && !candidates1Lines.isEmpty();
	}

	public boolean hasCandidates2() {
		return candidates2Lines != null && !candidates2Lines.isEmpty();
	}

	public boolean hasNodeLine(int line) {
		return hasNodes() && nodesLines.contains(line + offset);
	}

	public boolean hasEdgeLine(int line) {
		return hasEdges() && edgesLines.contains(line + offset);
	}

	public boolean hasCandidate1Line(int line) {
		return hasCandidates1() && candidates1Lines.contains(line + offset);
	}

	public boolean hasCandidate2Line(int line) {
		return hasCandidates2() && candidates2Lines.contains(line + offset);
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

	public String generateNodesStat() {
		if (!hasNodes()) {
			return null;
		}
		return "Uncovered nodes" + generateNodesInfo();
	}

	public String generateNodesInfo() {
		if (!hasNodes()) {
			return "";
		}
		return statFor(nodeLinesArray);
	}

	public String generateEdgesStat() {
		if (!hasEdges()) {
			return null;
		}
		return "Uncovered edges" + statFor(edgeLinesArray);
	}

	public String generateCandidates1Stat() {
		if (!hasCandidates1()) {
			return null;
		}
		return "Candidates 1" + generateCandidates1Info();
	}

	public String generateCandidates1Info() {
		if (!hasCandidates1()) {
			return "";
		}
		return statFor(candidates1LinesArray);
	}

	public String generateCandidates2Stat() {
		if (!hasCandidates2()) {
			return null;
		}
		return "Candidates 2" + generateCandidates2Info();
	}

	public String generateCandidates2Info() {
		if (!hasCandidates2()) {
			return "";
		}
		return statFor(candidates2LinesArray);
	}

	private String statFor(ArrayList<Integer> linesArray) {
		return " (" + linesArray.size() + ")";
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int findPrevLine(int row) {
		row += offset;
		int prevLine = -1;
		prevLine = Math.max(prevLine, findPrevLineIn(row, nodeLinesArray, tableForm.isNodesSelected()));
		prevLine = Math.max(prevLine, findPrevLineIn(row, edgeLinesArray, tableForm.isEdgesSelected()));
		prevLine = Math.max(prevLine, findPrevLineIn(row, candidates1LinesArray, tableForm.isCandidates1Selected()));
		prevLine = Math.max(prevLine, findPrevLineIn(row, candidates2LinesArray, tableForm.isCandidates2Selected()));
		prevLine = Math.max(prevLine, findPrevLineIn(row, mutationLinesArray, tableForm.isShowingMutation()));
		if (prevLine != -1) {
			prevLine -= offset;
		}
		return prevLine;
	}

	private int findPrevLineIn(int row, ArrayList<Integer> linesArray, boolean areLinesSelected) {
		int prevLine = -1;
		if (linesArray == null || !areLinesSelected) {
			return prevLine;
		}
		for (Integer line : linesArray) {
			if (line < row) {
				prevLine = line;
			} else {
				break;
			}
		}
		return prevLine;
	}

	public int findNextLine(int row) {
		row += offset;
		int nextLine = Integer.MAX_VALUE;
		nextLine = Math.min(nextLine, findNextLineIn(row, nodeLinesArray, tableForm.isNodesSelected()));
		nextLine = Math.min(nextLine, findNextLineIn(row, edgeLinesArray, tableForm.isEdgesSelected()));
		nextLine = Math.min(nextLine, findNextLineIn(row, candidates1LinesArray, tableForm.isCandidates1Selected()));
		nextLine = Math.min(nextLine, findNextLineIn(row, candidates2LinesArray, tableForm.isCandidates2Selected()));
		nextLine = Math.min(nextLine, findNextLineIn(row, mutationLinesArray, tableForm.isShowingMutation()));
		if (nextLine != Integer.MAX_VALUE) {
			nextLine -= offset;
		}
		return nextLine;
	}

	private int findNextLineIn(int row, ArrayList<Integer> linesArray, boolean areLinesSelected) {
		int nextLine = Integer.MAX_VALUE;
		if (linesArray == null || !areLinesSelected) {
			return nextLine;
		}
		for (Integer line : linesArray) {
			if (line > row) {
				nextLine = line;
				break;
			}
		}
		return nextLine;
	}

	public void setTableForm(TableForm tableForm) {
		this.tableForm = tableForm;
	}

	public static class Builder {
		private Collection<Integer> nodesLines = null;
		private Collection<Integer> edgesLines = null;
		private Collection<Integer> candidates1Lines = null;
		private Collection<Integer> candidates2Lines = null;
		private Collection<Integer> mutationLines = null;

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

		public Builder mutation(Collection<Integer> mutationLines) {
			this.mutationLines = mutationLines;
			return this;
		}

		public LinesStorage build() {
			return new LinesStorage(this);
		}
	}
}
