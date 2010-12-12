package ui.utils;

import io.ConsoleWriter;
import ui.ApplicationForm;
import ui.FileDependencyResolver;
import ui.utils.uiWithWorker.TaskSwingWorker;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Anton Chepurov
 */
public class DiagnosisWorker extends TaskSwingWorker {

	private final File hlddFile;
	private final ApplicationForm applicationForm;

	public DiagnosisWorker(List<String> cmd, PrintStream errorOut, File hlddFile, ApplicationForm applicationForm,
						   ConsoleWriter consoleWriter) {
		super(cmd, errorOut, consoleWriter);
		this.hlddFile = hlddFile;
		this.applicationForm = applicationForm;
	}

	@Override
	protected Boolean doInBackground() {
		/* Disable HLDD BUTTON and SIMULATE BUTTON */
		setEnableUI(false);
		/* Start worker */
		return super.doInBackground();
	}

	@Override
	protected void done() {
		/* Enable HLDD BUTTON and SIMULATE BUTTON */
		setEnableUI(true);

		super.done();
		try {
			if (get()) {
				/* Display candidates */
				showCandidates();
			}
		} catch (InterruptedException e) {/* do nothing */} catch (ExecutionException e) {/* do nothing */}
	}

	private void showCandidates() {
		if (!applicationForm.isDoAnalyzeCoverage()) {
			return;
		}
		File dgnFile = FileDependencyResolver.deriveDgnFile(hlddFile);
		if (dgnFile != null) {
			applicationForm.setDgnFile(dgnFile);
		}
	}

	private void setEnableUI(boolean enable) {
		applicationForm.enableDiagnosis(enable);
	}

}
