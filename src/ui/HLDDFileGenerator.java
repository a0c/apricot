package ui;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;

import static ui.BusinessLogic.HLDDRepresentationType.*;

/**
 * @author Anton Chepurov
 */
public class HLDDFileGenerator implements ChangeListener, DocumentListener {

	private final ApplicationForm applicationForm;

	public HLDDFileGenerator(ApplicationForm applicationForm) {
		this.applicationForm = applicationForm;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		react(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		react(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		react(e);
	}

	private void react(Object event) {

		if (!isAcceptedParser()) {
			return;
		}

		if (!applicationForm.areSmartNamesAllowed()) {
			if (isVhdlChanged(event)) {
				applicationForm.setBehHlddFile(null);
			}
			return;
		}

		File outputFile = generate();

		if (outputFile != null) {

			applicationForm.setBehHlddFile(outputFile);
		}
	}

	private boolean isVhdlChanged(Object event) {
		if (event instanceof ChangeEvent) {
			/* selecting radio button => user action (manual) */
			return false;
		} else if (event instanceof DocumentEvent) {
			/* VHDL file changes => auto action */
			return true;
		} else {
			throw new RuntimeException("Class of specified event object is not accepted for detecting VHDL file change: " + event.getClass().getSimpleName());
		}
	}

	private boolean isAcceptedParser() {
		return applicationForm.getSelectedParserId() == BusinessLogic.ParserID.VhdlBeh2HlddBeh;
	}

	public File generate() {

		File sourceFile = applicationForm.getSourceFile();
		BusinessLogic.HLDDRepresentationType hlddType = applicationForm.getHlddRepresentationType();

		if (sourceFile == null || hlddType == null) {
			return null;
		}

		boolean doCreateCSGraphs = applicationForm.shouldCreateCSGraphs();
		boolean doCreateExCSGraphs = applicationForm.shouldCreateExtraCSGraphs();
		boolean doFlattenCS = applicationForm.shouldFlattenCS();


		StringBuilder name = new StringBuilder(sourceFile.getParent()).append(File.separator);

		name.append(sourceFile.getName().replaceAll(".(vhd|vhdl)$", ""));

		if (hlddType == FULL_TREE_4_RTL) {
			name.append("_F4");
		} else if (hlddType == FULL_TREE) {
			name.append("_F");
		} else if (hlddType == REDUCED) {
			name.append("_R");
		} else if (hlddType == MINIMIZED) {
			name.append("_M");
		} else {
			throw new RuntimeException("HLDDFileGenerator.generate(): unknown HLDDRepresentationType: " + hlddType);
		}

		if (doCreateCSGraphs) {
			name.append("_GR");
		} else if (doFlattenCS) {
			name.append("_FL");
		} else if (doCreateExCSGraphs) {
			name.append("_EX");
		} else {
			name.append("_FU");
		}

		name.append(".agm");

		return new File(name.toString());
	}
}
