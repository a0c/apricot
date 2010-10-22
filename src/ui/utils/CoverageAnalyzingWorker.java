package ui.utils;

import ui.FileDependencyResolver;
import ui.utils.uiWithWorker.TaskSwingWorker;
import io.ConsoleWriter;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.io.OutputStream;
import java.io.File;

import ui.ApplicationForm;
import ui.BusinessLogicCoverageAnalyzer;

/**
 * @author Anton Chepurov
 */
public class CoverageAnalyzingWorker extends TaskSwingWorker {

	private final BusinessLogicCoverageAnalyzer businessLogic;

	public CoverageAnalyzingWorker(List<String> executableCommand, OutputStream infoOut, OutputStream errorOut,
								   BusinessLogicCoverageAnalyzer businessLogic, ConsoleWriter consoleWriter) {
		super(executableCommand, infoOut, errorOut, consoleWriter);
		this.businessLogic = businessLogic;
	}

	protected Boolean doInBackground() {
		/* Disable HLDD BUTTON and ANALYZE BUTTON */
		setEnableUI(false);
		/* Start worker */
		return super.doInBackground();
	}

	protected void done() {
		/* Enable HLDD BUTTON and ANALYZE BUTTON */
		setEnableUI(true);

		super.done();
		try {
			if (get()) {
				/* Display coverage */
				businessLogic.displayCoverage();
				showVHDLCoverage();
			}
		} catch (InterruptedException e) {/* Do nothing. */} catch (ExecutionException e) {/* Do nothing. */}

	}

	private void setEnableUI(boolean enable) {
		ApplicationForm applicationForm = businessLogic.getApplicationForm();
		applicationForm.setEnableHlddCoverageButton(enable);
		applicationForm.setEnableAnalyzeButton(enable);
	}

	private void showVHDLCoverage() {
		/* Automatically load COV and VHDL files, if available */
		ApplicationForm applicationForm = businessLogic.getApplicationForm();
		File hlddFile = businessLogic.getHlddFile();
		File covFile = FileDependencyResolver.deriveCovFile(hlddFile);
		if (covFile != null) {
			applicationForm.updateCovTextField(covFile);
			businessLogic.setCovFile(covFile);
		}

		File vhdlFile = FileDependencyResolver.deriveVhdlFile(hlddFile);
		if (vhdlFile != null) {
			applicationForm.updateVhdlCovTextField(vhdlFile);
			businessLogic.setVhdlFile(vhdlFile);
		}
		/* Automatically click Show button, if both files are set */
		if (vhdlFile != null && covFile != null) {
			applicationForm.doClickShowButton();
		}
	}
}
