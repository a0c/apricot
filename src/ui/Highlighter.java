package ui;

import io.ConsoleWriter;
import ui.utils.CoverageVisualizingUI;
import ui.utils.CoverageVisualizingWorker;
import ui.utils.uiWithWorker.UIWithWorker;

import java.io.File;

/**
 * @author Anton Chepurov
 */
public class Highlighter implements Lockable {

	private final ApplicationForm applicationForm;
	private final ConsoleWriter consoleWriter;

	private File vhdlFile;
	private File covFile;
	private File dgnFile;

	public Highlighter(ApplicationForm applicationForm, ConsoleWriter consoleWriter) {
		this.applicationForm = applicationForm;
		this.consoleWriter = consoleWriter;
	}

	public void setVhdlFile(File vhdlFile) {
		this.vhdlFile = vhdlFile;
	}

	public void setCovFile(File covFile) {
		this.covFile = covFile;
	}

	public void setDgnFile(File dgnFile) {
		this.dgnFile = dgnFile;
	}

	public void highlight() throws ExtendedException {
		File mappingFile = null;
		/* Check all files to be selected */
		String fileDescription = null;
		if (vhdlFile == null) {
			fileDescription = "VHDL";
		} else if (covFile == null && dgnFile == null) {
			fileDescription = "Coverage/Diagnosis";
		} else {
			File someExistingFile = covFile != null ? covFile : dgnFile;
			mappingFile = FileDependencyResolver.deriveMapFile(someExistingFile);
			if (mappingFile == null) {
				fileDescription = "Mapping";
			}
		}
		if (fileDescription != null) {
			throw new ExtendedException(fileDescription + " file is missing", ExtendedException.MISSING_FILE_TEXT);
		}

		UIWithWorker.runUIWithWorker(new CoverageVisualizingUI(applicationForm.getFrame()),
				new CoverageVisualizingWorker(vhdlFile, covFile, dgnFile, mappingFile, applicationForm, consoleWriter, simpleLock));

	}

	/* Lockable */
	private final SimpleLock simpleLock = new SimpleLock();

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
