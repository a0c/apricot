package ui;

import ui.utils.uiWithWorker.UIWithWorker;
import ui.utils.SimulationUI;
import ui.utils.SimulationWorker;
import io.ConsoleWriter;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Anton Chepurov
 */
public class BusinessLogicSimulation {
	/* Link to form */
	private final ApplicationForm applicationForm;
	private final ConsoleWriter consoleWriter;

	private File hlddFile = null;

	public BusinessLogicSimulation(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
		this.applicationForm = applicationForm;
		this.consoleWriter = consoleWriter;
	}

	public void setHlddFile(File hlddFile) {
		this.hlddFile = hlddFile;
	}

	public String getProposedFileName() {
		return hlddFile != null ? hlddFile.getAbsolutePath() : null;
	}

	public void processSimulate() throws ExtendedException {

		/* Check design to be selected */
		if (hlddFile == null) {
			throw new ExtendedException("HLDD model file is missing", ExtendedException.MISSING_FILE_TEXT);
		}
		/* Receive data from form */
//        int patternCount = applicationForm.getSimulatePatternCount();

		/* Collect execution string */
		List<String> commandList = new ArrayList<String>(5);
		commandList.add(ApplicationForm.LIB_DIR + "beh_simul");
//        if (patternCount > 0) {
//            commandList.add("-random");
//            commandList.add("" + patternCount);
//        }
		commandList.add(hlddFile.getAbsolutePath().replace(".agm", ""));

		/* Execute command */
		UIWithWorker.runUIWithWorker(
				new SimulationUI(applicationForm.getFrame()),
				new SimulationWorker(commandList, System.out, System.err, applicationForm, consoleWriter)
		);

	}

}
