package ui.utils;

import io.ConsoleWriter;
import ui.ApplicationForm;
import ui.BusinessLogicCoverageAnalyzer;
import ui.FileDependencyResolver;
import ui.utils.uiWithWorker.TaskSwingWorker;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Anton Chepurov
 */
public class CoverageAnalyzingWorker extends TaskSwingWorker {

	private final BusinessLogicCoverageAnalyzer businessLogic;

	public CoverageAnalyzingWorker(List<String> executableCommand, OutputStream errorOut,
								   BusinessLogicCoverageAnalyzer businessLogic, ConsoleWriter consoleWriter) {
		super(executableCommand, errorOut, consoleWriter);
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
				showVHDLCoverage();
			}
		} catch (InterruptedException e) {/* Do nothing. */} catch (ExecutionException e) {/* Do nothing. */}

	}

	private void setEnableUI(boolean enable) {
		ApplicationForm applicationForm = businessLogic.getApplicationForm();
		applicationForm.enableCoverageAnalyzer(enable);
	}

	private void showVHDLCoverage() {
		/* Automatically load COV and VHDL files, if available and if asked/possible */
		ApplicationForm applicationForm = businessLogic.getApplicationForm();
		if (!applicationForm.isDoAnalyzeCoverage()) {
			return;
		}
		File hlddFile = businessLogic.getHlddFile();
		File covFile = FileDependencyResolver.deriveCovFile(hlddFile);
		if (covFile != null) {
			applicationForm.setCovFile(covFile);
		}

	}
}
