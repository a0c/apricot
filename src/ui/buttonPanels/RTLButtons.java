package ui.buttonPanels;

import ui.ApplicationForm;

import javax.swing.*;
import javax.swing.event.DocumentListener;

/**
 * @author Anton Chepurov
 */
public class RTLButtons {
	private JButton rtlButton;
	private JTextField rtlTextField;
	private JButton behButton;
	private JTextField behTextField;
	private JPanel mainPanel;

	public RTLButtons(ApplicationForm.FileDropHandler fileDropHandler) {
		behButton.addKeyListener(fileDropHandler);
		rtlButton.addKeyListener(fileDropHandler);
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public JButton getRtlButton() {
		return rtlButton;
	}

	public JButton getBehButton() {
		return behButton;
	}

	public JTextField getRtlTextField() {
		return rtlTextField;
	}

	public JTextField getBehTextField() {
		return behTextField;
	}

	public void addFileGenerator(DocumentListener outputFileGenerator) {
		behTextField.getDocument().addDocumentListener(outputFileGenerator);
	}
}