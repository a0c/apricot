package ui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;

import static ui.BusinessLogic.HLDDRepresentationType.*;

/**
 * @author Anton Chepurov
 */
public class OutputFileGenerator implements ChangeListener, DocumentListener {

	private final ApplicationForm applicationForm;
	private JButton outputFileButton;

	public OutputFileGenerator(ApplicationForm applicationForm, JButton outputFileButton) {
		this.applicationForm = applicationForm;
		this.outputFileButton = outputFileButton;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		react();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		react();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		react();
	}

	private void react() {
		File outputFile = generate();

		if (outputFile != null) {

			applicationForm.setDestFile(outputFile);

			applicationForm.updateTextFieldFor(outputFileButton, outputFile);
		}
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

		if (hlddType == FULL_TREE) {
			name.append("_F");
		} else if (hlddType == REDUCED) {
			name.append("_R");
		} else if (hlddType == MINIMIZED) {
			name.append("_M");
		} else {
			throw new RuntimeException("OutputFileGenerator.generate(): unknown HLDDRepresentationType: " + hlddType);
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
