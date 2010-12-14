package ui;

import io.ConsoleWriter;
import ui.utils.DiagnosisUI;
import ui.utils.DiagnosisWorker;
import ui.utils.uiWithWorker.UIWithWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class Diagnosis {

	private final ApplicationForm applicationForm;
	private final ConsoleWriter consoleWriter;

	private File hlddFile;

	public Diagnosis(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
		this.applicationForm = applicationForm;
		this.consoleWriter = consoleWriter;
	}

	public void setHlddFile(File hlddFile) {
		this.hlddFile = hlddFile;
	}


	public void diagnose() throws ExtendedException {

		if (hlddFile == null) {
			throw new ExtendedException("HLDD model file is missing", ExtendedException.MISSING_FILE_TEXT);
		}

		boolean isRandom = applicationForm.isRandomDiag();
		int patternCount = applicationForm.getPatternCountForDiag();
		boolean diagnose = applicationForm.isDoDiagnose();
		boolean optimize = applicationForm.isDoDiagOptimize();
		boolean potential = applicationForm.isDoDiagPotential();
		boolean scoreByFailed = applicationForm.isDoDiagScoreByFailed();
		boolean scoreByRatio = applicationForm.isDoDiagScoreByRatio();
		String operators = applicationForm.getDiagnosisOperatorDirective();

		List<String> cmd = new ArrayList<String>(10);
		cmd.add(ApplicationForm.LIB_DIR + (Platform.isWindows() ? "hlddsim.exe" : "hlddsim"));
		if (diagnose) {
			cmd.add("-diagnosis");
			if (optimize) {
				cmd.add("optimize");
			}
			if (potential) {
				cmd.add("potential");
			}
			if (scoreByFailed) {
				/* this is the default option (used automatically by hlddsim) */
//				cmd.add("scorebyfailed");
			}
			if (scoreByRatio) {
				cmd.add("scorebyratio");
			}
			cmd.add("operator");
			cmd.add(operators);
		}
		if (isRandom) {
			cmd.add("-random");
			cmd.add("" + patternCount);
		}
		cmd.add(hlddFile.getAbsolutePath().replace(".agm", ""));

		UIWithWorker.runUIWithWorker(
				new DiagnosisUI(applicationForm.getFrame()),
				new DiagnosisWorker(cmd, System.err, hlddFile, applicationForm, consoleWriter)
		);
	}
}
