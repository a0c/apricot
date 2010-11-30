package ui;

import io.ConsoleWriter;
import ui.utils.CoverageAnalyzingUI;
import ui.utils.CoverageAnalyzingWorker;
import ui.utils.CoverageVisualizingUI;
import ui.utils.CoverageVisualizingWorker;
import ui.utils.uiWithWorker.UIWithWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class BusinessLogicCoverageAnalyzer implements Lockable {

	private final ApplicationForm applicationForm;
	private final ConsoleWriter consoleWriter;
	private File hlddFile;
	private File vhdlFile;
	private File covFile;
	private File mappingFile;

	private final SimpleLock simpleLock = new SimpleLock();

	public BusinessLogicCoverageAnalyzer(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
		this.applicationForm = applicationForm;
		this.consoleWriter = consoleWriter;
	}

	public File getHlddFile() {
		return hlddFile;
	}

	public void setHlddFile(File hlddFile) {
		this.hlddFile = hlddFile;
	}

	public void setVhdlFile(File vhdlFile) {
		this.vhdlFile = vhdlFile;
	}

	public void setCovFile(File covFile) {
		this.covFile = covFile;
		/* Clear mapping file (this file is implicit for user, so must be manipulated carefully) */
		setMappingFile(null);
	}

	private void setMappingFile(File mappingFile) {
		this.mappingFile = mappingFile;
	}

	public void processAnalyze() throws ExtendedException {
		/* Check design to be selected */
		if (hlddFile == null) {
			throw new ExtendedException("HLDD model file is missing", ExtendedException.MISSING_FILE_TEXT);
		}

		int patternCount = applicationForm.getPatternCountForCoverage();
		boolean isRandom = applicationForm.isRandomCov();
		boolean isDoAssert = applicationForm.isDoAnalyzeCoverage();
		String directive = applicationForm.getCoverageAnalyzerDirective();

		/* Collect execution string */
		List<String> commandList = new ArrayList<String>(5);
		commandList.add(ApplicationForm.LIB_DIR + (Platform.isWindows() ? "hlddsim.exe" : "hlddsim"));
		if (isDoAssert) {
			commandList.add("-coverage");
			commandList.add(directive);
		}
		if (isRandom) {
			commandList.add("-random");
			commandList.add("" + patternCount);
		}
		commandList.add(hlddFile.getAbsolutePath().replace(".agm", ""));

		/* Execute command */
		UIWithWorker.runUIWithWorker(
				new CoverageAnalyzingUI(applicationForm.getFrame()),
				new CoverageAnalyzingWorker(
						commandList,
						System.err,
						this,
						consoleWriter
				)
		);

		/* showVHDLCoverage() is invoked in CoverageAnalyzingWorker after it ("assert") has completed its work */
	}

	public ApplicationForm getApplicationForm() {
		return applicationForm;
	}

	public void processShow() throws ExtendedException {
		/* Check all files to be selected */
		String fileDescription = null;
		if (vhdlFile == null) {
			fileDescription = "VHDL";
		} else if (covFile == null) {
			fileDescription = "Coverage";
		} else if (mappingFile == null) {
			fileDescription = "Mapping";
			//todo: temporary fix
			mappingFile = FileDependencyResolver.deriveMapFile(covFile);
			fileDescription = mappingFile == null ? "Mapping" : null;
			//todo: temporary fix
		}
		if (fileDescription != null) {
			throw new ExtendedException(fileDescription + " file is missing", ExtendedException.MISSING_FILE_TEXT);
		}

		/*  */
		UIWithWorker.runUIWithWorker(new CoverageVisualizingUI(applicationForm.getFrame()),
				new CoverageVisualizingWorker(vhdlFile, covFile, mappingFile, applicationForm, consoleWriter, simpleLock));

	}

	@Override
	public boolean isLocked() {
		return simpleLock.isLocked();
	}

	@Override
	public void lock() {
		simpleLock.lock();
	}

	@Override
	public void unlock() {
		simpleLock.unlock();
	}
}
