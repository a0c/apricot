package ui.buttonPanels;

import ui.ApplicationForm;

import javax.swing.*;
import javax.swing.event.DocumentListener;

/**
 * @author Anton Chepurov
 */
public class PSLButtons {
	private JButton pslButton;
	private JTextField pslTextField;
	private JButton baseModelButton;
	private JTextField baseModelTextField;
	private JPanel mainPanel;

	public PSLButtons(ApplicationForm.FileDropHandler fileDropHandler) {
		baseModelButton.addKeyListener(fileDropHandler);
		pslButton.addKeyListener(fileDropHandler);
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public JButton getPslButton() {
		return pslButton;
	}

	public JButton getBaseModelButton() {
		return baseModelButton;
	}

	public JTextField getPslTextField() {
		return pslTextField;
	}

	public JTextField getBaseModelTextField() {
		return baseModelTextField;
	}

	public void addFileGenerator(DocumentListener outputFileGenerator) {
		baseModelTextField.getDocument().addDocumentListener(outputFileGenerator);
	}
}