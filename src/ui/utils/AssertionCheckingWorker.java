package ui.utils;

import io.ConsoleWriter;
import ui.BusinessLogicAssertionChecker;
import ui.utils.uiWithWorker.TaskSwingWorker;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Anton Chepurov
 */
public class AssertionCheckingWorker extends TaskSwingWorker {
	private final BusinessLogicAssertionChecker businessLogic;
	private final String simulationFilePath;

	public AssertionCheckingWorker(List<String> executableCommand, OutputStream errorOut,
								   BusinessLogicAssertionChecker businessLogic, String simulationFilePath,
								   ConsoleWriter consoleWriter) {
		super(executableCommand, errorOut, consoleWriter);
		this.businessLogic = businessLogic;
		this.simulationFilePath = simulationFilePath;
	}

	@Override
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
