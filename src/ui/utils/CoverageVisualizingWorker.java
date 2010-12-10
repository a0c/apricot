package ui.utils;

import base.SourceLocation;
import io.ConsoleWriter;
import ui.ApplicationForm;
import ui.ExtendedException;
import ui.SimpleLock;
import ui.base.HLDD2VHDLMapping;
import ui.base.NodeItem;
import ui.base.SplitCoverage;
import ui.base.VariableItem;
import ui.fileViewer.LinesStorage;
import ui.graphics.CoveragePanel;
import ui.io.CoverageReader;
import ui.io.DiagnosisReader;
import ui.io.HLDD2VHDLMappingReader;
import ui.utils.uiWithWorker.TaskSwingWorker;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class CoverageVisualizingWorker extends TaskSwingWorker {

	private final File vhdlFile;
	private final File covFile;
	private final File dgnFile;
	private final File mappingFile;
	private final ApplicationForm applicationForm;
	private final ConsoleWriter consoleWriter;
	private final SimpleLock simpleLock;

	public CoverageVisualizingWorker(File vhdlFile, File covFile, File dgnFile, File mappingFile, ApplicationForm applicationForm, ConsoleWriter consoleWriter, SimpleLock simpleLock) {
		this.vhdlFile = vhdlFile;
		this.covFile = covFile;
		this.dgnFile = dgnFile;
		this.mappingFile = mappingFile;
		this.applicationForm = applicationForm;
		this.consoleWriter = consoleWriter;
		this.simpleLock = simpleLock;
		executableRunnable = createRunnable();
	}

	private Runnable createRunnable() {
		return new Runnable() {
			public void run() {
				try {
					/* Read HLDD-2-VHDL mapping */
					consoleWriter.write("Mapping HLDD to VHDL...");
					HLDD2VHDLMapping hldd2VHDLMapping = new HLDD2VHDLMappingReader(mappingFile).getMapping();
					consoleWriter.done();

					/* Read COV file */
					CoverageReader coverageReader = null;
					Collection<NodeItem> uncoveredNodeItems = null;
					if (covFile != null) {
						consoleWriter.write("Reading coverage file...");
						coverageReader = new CoverageReader(covFile);
						uncoveredNodeItems = coverageReader.getUncoveredNodeItems();
						consoleWriter.done();
					}

					/* Read DGN file */
					SourceLocation actualMutationSources = null;
					SourceLocation candidates1Sources = null;
					SourceLocation candidates2Sources = null;
					if (dgnFile != null) {
						consoleWriter.write("Reading diagnosis file...");
						DiagnosisReader diagnosisReader = new DiagnosisReader(dgnFile);
						Collection<VariableItem> candidates1 = diagnosisReader.getCandidates1();
						Collection<VariableItem> candidates2 = diagnosisReader.getCandidates2();
						candidates1Sources = hldd2VHDLMapping.getSourceFor(candidates1);
						candidates2Sources = hldd2VHDLMapping.getSourceFor(candidates2);
						VariableItem actualMutation = diagnosisReader.getActualMutation();
						actualMutationSources = hldd2VHDLMapping.getSourceFor(actualMutation);
						consoleWriter.done();
					}

					/* Extract lines for uncovered nodes */
					SourceLocation allSources = hldd2VHDLMapping.getAllSources();
					SourceLocation uncoveredSources = uncoveredNodeItems != null
							? hldd2VHDLMapping.getSourceFor(uncoveredNodeItems) : null;

					LinkedList<File> allSourceFiles = new LinkedList<File>(allSources.getFiles());
					if (!allSourceFiles.contains(vhdlFile)) {
						allSourceFiles.add(vhdlFile); // just in case it is not in allSources list (no transitions in it, only component instantiations)
					}
					java.util.Collections.sort(allSourceFiles);
					LinkedList<SplitCoverage> vhdlNodeCoverages = new LinkedList<SplitCoverage>();
					for (File sourceFile : allSourceFiles) {
						/* Add tab to the FileViewer */
						if (hasNodeCoverage(coverageReader) || dgnFile != null) {
							LinesStorage linesStorage = buildLinesStorage(sourceFile, uncoveredSources,
									candidates1Sources, candidates2Sources, actualMutationSources);
							applicationForm.addFileViewerTabFromFile(sourceFile, linesStorage, null);
						}
						/* Add VHDL coverage bar */
						if (hasNodeCoverage(coverageReader)) {
							int total = 0;
							if (allSources.hasFile(sourceFile)) {
								Collection<Integer> lines = allSources.getLinesForFile(sourceFile);
								total = lines != null ? lines.size() : 0;
							}
							int uncovered = uncoveredSources != null && uncoveredSources.hasFile(sourceFile) ?
									uncoveredSources.getLinesForFile(sourceFile).size() : 0;
							vhdlNodeCoverages.add(new SplitCoverage(total - uncovered, total, sourceFile.getName(), null));
						}
					}

					/* Add coverage */
					if (covFile != null) {
						if (hasNodeCoverage(coverageReader)) {
							int total = allSources.getTotalLinesNum();
							int uncovered = uncoveredSources == null ? 0 : uncoveredSources.getTotalLinesNum();
							vhdlNodeCoverages.addFirst(new SplitCoverage(total - uncovered, total, SplitCoverage.STATEMENT_COVERAGE,
									"Coverage for top level: " + vhdlFile.getPath()));
						}
						CoveragePanel coveragePanel = new CoveragePanel(
								coverageReader.getNodeCoverage(),
								coverageReader.getEdgeCoverage(),
								coverageReader.getToggleCoverage(),
								coverageReader.getConditionCoverage(),
								vhdlNodeCoverages);
						applicationForm.addCoverage(generateTabTitle(covFile), covFile.getAbsolutePath(), coveragePanel);
					}

					isProcessFinished = true;
				} catch (Exception e) {
					occurredException = ExtendedException.create(e);
					isProcessFinished = false;
				}

			}
		};
	}

	private boolean hasNodeCoverage(CoverageReader coverageReader) {
		return coverageReader != null && coverageReader.hasNodeCoverage();
	}

	private LinesStorage buildLinesStorage(File sourceFile,
										   SourceLocation uncoveredSources,
										   SourceLocation candidates1Sources,
										   SourceLocation candidates2Sources,
										   SourceLocation mutationSources) {
		LinesStorage.Builder builder = new LinesStorage.Builder();
		if (uncoveredSources != null) {
			builder.nodes(uncoveredSources.getLinesForFile(sourceFile));
		}
		if (candidates1Sources != null && candidates1Sources.hasFile(sourceFile)) {
			builder.candidates1(candidates1Sources.getLinesForFile(sourceFile));
		}
		if (candidates2Sources != null && candidates2Sources.hasFile(sourceFile)) {
			builder.candidates2(candidates2Sources.getLinesForFile(sourceFile));
		}
		if (mutationSources != null) {
			builder.mutation(mutationSources.getLinesForFile(sourceFile));
		}
		return builder.build();
	}

	private static String generateTabTitle(File aFile) {
		StringBuilder sb = new StringBuilder(aFile.getName());
		sb.delete(sb.lastIndexOf("."), sb.length());
		sb.append(":Coverage");
		return sb.toString();
	}

	protected Boolean doInBackground() {
		/* Disable UI */
		enableUI(false);

		return super.doInBackground();
	}

	protected void done() {
		/* Enable UI */
		enableUI(true);

		super.done();

		simpleLock.unlock();
	}

	private void enableUI(boolean enable) {
		applicationForm.enableCoverageHighlighter(enable);
	}
}
