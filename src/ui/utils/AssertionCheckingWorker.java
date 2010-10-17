package ui.utils;

import ui.utils.uiWithWorker.TaskSwingWorker;
import io.ConsoleWriter;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.io.OutputStream;
import java.io.File;

import ui.BusinessLogicAssertionChecker;

/**
 * @author Anton Chepurov
 */
public class AssertionCheckingWorker extends TaskSwingWorker {
	private final BusinessLogicAssertionChecker businessLogic;
	private final String simulationFilePath;

	public AssertionCheckingWorker(List<String> executableCommand, OutputStream infoOut, OutputStream errorOut,
								   BusinessLogicAssertionChecker businessLogic, String simulationFilePath,
								   ConsoleWriter consoleWriter) {
		super(executableCommand, infoOut, errorOut, consoleWriter);
		this.businessLogic = businessLogic;
		this.simulationFilePath = simulationFilePath;
	}

	protected void done() {
		super.done();
		try {
			if (get()) {
				/* Fill Simul.file automatically */
				File simulationFile = new File(simulationFilePath);
				if (simulationFile.exists()) {
					businessLogic.setSimulationFile(simulationFile);
					businessLogic.loadChkFile();
				}
			}
		} catch (InterruptedException e) {/* Do nothing. */} catch (ExecutionException e) {/* Do nothing. */}
	}
}
