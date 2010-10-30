package ui.utils;

import base.SourceLocation;
import io.ConsoleWriter;
import ui.ApplicationForm;
import ui.ExtendedException;
import ui.SimpleLock;
import ui.base.HLDD2VHDLMapping;
import ui.base.NodeItem;
import ui.base.SplitCoverage;
import ui.graphics.CoveragePanel;
import ui.io.CoverageReader;
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
	private final File mappingFile;
	private final ApplicationForm applicationForm;
	private final ConsoleWriter consoleWriter;
	private final SimpleLock simpleLock;

	public CoverageVisualizingWorker(File vhdlFile, File covFile, File mappingFile, ApplicationForm applicationForm, ConsoleWriter consoleWriter, SimpleLock simpleLock) {
		this.vhdlFile = vhdlFile;
		this.covFile = covFile;
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
					consoleWriter.write("Reading coverage file...");
					CoverageReader coverageReader = new CoverageReader(covFile);
					Collection<NodeItem> uncoveredNodeItems = coverageReader.getUncoveredNodeItems();
					consoleWriter.done();

					/* Extract lines for uncovered nodes */
					SourceLocation allSources = hldd2VHDLMapping.getAllSources();
					SourceLocation uncoveredSources = hldd2VHDLMapping.getSourceFor(uncoveredNodeItems);

					/* Add tab to the FileViewer */
					LinkedList<File> allSourceFiles = new LinkedList<File>(allSources.getFiles());
					if (!allSourceFiles.contains(vhdlFile)) {
						allSourceFiles.add(vhdlFile); // just in case it is not in allSources list (no transitions in it, only component instantiations)
					}
					java.util.Collections.sort(allSourceFiles);
					LinkedList<SplitCoverage> vhdlNodeCoverages = new LinkedList<SplitCoverage>();
					for (File sourceFile : allSourceFiles) {
						Collection<Integer> highlightedLines = uncoveredSources == null ? null : uncoveredSources.getLinesForFile(sourceFile);
						applicationForm.addFileViewerTabFromFile(sourceFile, highlightedLines, null, null);	
						/* Add coverage */
						int total = 0;
						if (allSources.hasFile(sourceFile)) {
							Collection<Integer> lines = allSources.getLinesForFile(sourceFile);
							total = lines != null ? lines.size() : 0;
						}
						int uncovered = uncoveredSources != null && uncoveredSources.hasFile(sourceFile) ?
								uncoveredSources.getLinesForFile(sourceFile).size() : 0;
						vhdlNodeCoverages.add(new SplitCoverage(total - uncovered, total, sourceFile.getName(), null));
					}

					/* Add coverage */
					int total = allSources.getTotalLinesNum();
					int uncovered = uncoveredSources == null ? 0 : uncoveredSources.getTotalLinesNum();
					vhdlNodeCoverages.addFirst(new SplitCoverage(total - uncovered, total, SplitCoverage.STATEMENT_COVERAGE,
							"Coverage for top level: " + vhdlFile.getPath()));
					CoveragePanel coveragePanel = new CoveragePanel(
							coverageReader.getNodeCoverage(),
							coverageReader.getEdgeCoverage(),
							coverageReader.getToggleCoverage(),
							vhdlNodeCoverages);
					applicationForm.addCoverage(generateTabTitle(covFile), covFile.getAbsolutePath(), coveragePanel);

					isProcessFinished = true;
				} catch (Exception e) {
					occurredException = ExtendedException.create(e);
					isProcessFinished = false;
				}

			}
		};
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
