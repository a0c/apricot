package ui;

import io.ConsoleWriter;
import ui.utils.CoverageAnalyzingUI;
import ui.utils.CoverageAnalyzingWorker;
import ui.utils.uiWithWorker.UIWithWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class BusinessLogicCoverageAnalyzer {

	private ApplicationForm applicationForm;
	private ConsoleWriter consoleWriter;
	private File hlddFile;

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

	public void processAnalyze() throws ExtendedException {
		/* Check design to be selected */
		if (hlddFile == null) {
			throw new ExtendedException("HLDD model file is missing", ExtendedException.MISSING_FILE_TEXT);
		}

		int patternCount = applicationForm.getPatternCountForCoverage();
		boolean isRandom = applicationForm.isRandomCov();
		boolean isDoMeasureCoverage = applicationForm.isDoAnalyzeCoverage();
		String directive = applicationForm.getCoverageAnalyzerDirective();

		/* Collect execution string */
		List<String> commandList = new ArrayList<String>(5);
		commandList.add(ApplicationForm.LIB_DIR + (Platform.isWindows() ? "hlddsim.exe" : "hlddsim"));
		if (isDoMeasureCoverage) {
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

}
