package ui;

import ee.ttu.pld.apricot.cli.CoverageRequest;
import ee.ttu.pld.apricot.cli.Request;
import io.ConsoleWriter;
import ui.utils.CoverageAnalyzingUI;
import ui.utils.CoverageAnalyzingWorker;
import ui.utils.CoverageVisualizingUI;
import ui.utils.CoverageVisualizingWorker;
import ui.utils.uiWithWorker.UIWithWorker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class BusinessLogicCoverageAnalyzer implements Lockable {

	private ApplicationForm applicationForm;
	private ConsoleWriter consoleWriter;
	private File hlddFile;
	private File vhdlFile;
	private File covFile;
	private File mappingFile;

	private final SimpleLock simpleLock = new SimpleLock();

	public BusinessLogicCoverageAnalyzer(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
		this.applicationForm = applicationForm;
		this.consoleWriter = consoleWriter;
	}

	public BusinessLogicCoverageAnalyzer(Collection<Request> requests, String libPath) {
		for (Request request : requests) {
			if (request instanceof CoverageRequest) {
				CoverageRequest coverageRequest = (CoverageRequest) request;
				if (coverageRequest.isBroken()) {
					coverageRequest.printError();
					continue;
				}
				processRequest(coverageRequest, libPath);
			}
		}
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

	private void processRequest(CoverageRequest coverageRequest, String libPath) {

		if (libPath == null) {
			libPath = "../lib/";
		}

		List<String> cmd = new ArrayList<String>(5);
		cmd.add(libPath + (Platform.isWindows() ? "hlddsim.exe" : "hlddsim"));
		cmd.add("-coverage");
		cmd.add(coverageRequest.getDirective());
		cmd.add(coverageRequest.getHlddFile().getAbsolutePath().replace(".agm", ""));

		try {
			Process process = Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]));

			boolean success = waitForProcessToComplete(process);

			if (success) {
				coverageRequest.markSuccessful();
			}

		} catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
	private boolean waitForProcessToComplete(Process process) {
		InputStream inputStream = process.getInputStream();
		InputStream errorStream = process.getErrorStream();
		boolean isProcessFinished = false;
		boolean success = false;
		try {
			while (!isProcessFinished && !Thread.interrupted()) {
				/* Read OUTPUT */
				int byteCount = inputStream.available();
				if (byteCount > 0) {
					for (int i = 0; i < byteCount; i++) {
						//todo: read N at once. + use buffered reader??
						System.out.write(String.valueOf((char) inputStream.read()).getBytes());
					}
				}
				/* Read ERROR */
				int errorBytesAvailable = errorStream.available();
				if (errorBytesAvailable > 0) {
					for (int i = 0; i < errorBytesAvailable; i++) {
						System.err.write(errorStream.read());
					}
				}

				try {
					int exitValue = process.exitValue();
					isProcessFinished = true;
					success = exitValue == 0;
					if (!success) {
						System.out.println("ERROR: Coverage Analyzer failed with error " + exitValue);
					}
				} catch (IllegalThreadStateException e) {
					// indicates that process.exitValue() cannot return any value yet
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						break;
					}
				}
			}
		} catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		return success;
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
