package ui.utils;

import ui.io.AssertionCheckReader;
import ui.utils.uiWithWorker.TaskSwingWorker;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.io.File;

import ui.ExtendedException;
import ui.BusinessLogicAssertionChecker;
import ui.ApplicationForm;

/**
 * @author Anton Chepurov
 */
public class AssertionLoadingWorker extends TaskSwingWorker {
	private final File simulationFile;
	private final BusinessLogicAssertionChecker businessLogicAssertionChecker;
	private AssertionCheckReader simulationReader;

	public AssertionLoadingWorker(final File simulationFile, BusinessLogicAssertionChecker businessLogicAC) {
		this.simulationFile = simulationFile;
		this.businessLogicAssertionChecker = businessLogicAC;
		executableRunnable = new Runnable() {
			public void run() {
				try {
					enableUI(false);
					simulationReader = new AssertionCheckReader(simulationFile, uiHolder, 1);
					simulationReader.readAssertions();
					isProcessFinished = true;

				} catch (Exception e) {
					businessLogicAssertionChecker.getApplicationForm().updateChkFileTextField(null);
					occurredException = new ExtendedException("IO Error occurred while reading simulation file:\n" +
							e.getMessage(), ExtendedException.IO_ERROR_TEXT);
					isProcessFinished = false;
				}
			}
		};
	}

	protected void done() {
		super.done();
		try {
			if (get()) {
				updateUI(simulationFile, simulationReader.getPatternsSize());
				/* Set simulationReader */
				businessLogicAssertionChecker.setSimulationReader(simulationReader);
			}
		} catch (InterruptedException e) {/* Do nothing. */} catch (ExecutionException e) {/* Do nothing. */}
		catch (CancellationException e) {
			/* Do nothing  */
		}

	}

	private void enableUI(boolean enable) {
		businessLogicAssertionChecker.getApplicationForm().setEnableDrawButton(enable);
	}

	private void updateUI(File simulationFile, int maxValue) {
		ApplicationForm applicationForm = businessLogicAssertionChecker.getApplicationForm();
		/* Update text field and spinner */
		if (simulationFile != null) {
			applicationForm.updateChkFileTextField(simulationFile);
			applicationForm.updateDrawSpinner(maxValue);
		} else {
			applicationForm.updateChkFileTextField(null);
		}

		/* In any case, enable UI */
		enableUI(true);
	}

	public void stopWorker() {
		super.stopWorker();
		/* Update UI and BL */
		businessLogicAssertionChecker.setSimulationFile(null);
		updateUI(null, 0);
	}
}
