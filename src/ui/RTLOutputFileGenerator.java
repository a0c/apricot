package ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 02.06.2010
 * <br>Time: 17:38:51
 */
public class RTLOutputFileGenerator implements DocumentListener {
	//todo: possibly generify this class to OutputFileGenerator...
	private final ApplicationForm applicationForm;
	private JButton outputFileButton;

	public RTLOutputFileGenerator(ApplicationForm applicationForm, JButton outputFileButton) {
		this.applicationForm = applicationForm;
		this.outputFileButton = outputFileButton;
	}

	public File generate() {
		File sourceFile = applicationForm.getSourceFile();
		if (sourceFile == null) {
			return null;
		}

		StringBuilder name = new StringBuilder(sourceFile.getName().replaceAll(".agm$", ""));
		name.append("_RTL.agm");

		return new File(sourceFile.getParent(), name.toString());
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		react();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {}

	@Override
	public void changedUpdate(DocumentEvent e) {
		react();
	}

	void react() {
		BusinessLogic.ParserID parserId = applicationForm.getSelectedParserId();
		if (parserId == BusinessLogic.ParserID.HlddBeh2HlddRtl) {

			File file = generate();

			applicationForm.setDestFile(file);
			applicationForm.updateTextFieldFor(outputFileButton, file);

		}
	}
}
