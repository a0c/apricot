package ui.buttonPanels;

import ui.ApplicationForm;

import javax.swing.*;
import javax.swing.event.DocumentListener;

/**
 * @author Anton Chepurov
 */
public class BehDDButtons {
	private JButton hlddButton;
	private JTextField hlddTextField;
	private JButton vhdlButton;
	private JTextField vhdlTextField;
	private JPanel mainPanel;

	public BehDDButtons(ApplicationForm.FileDropHandler fileDropHandler) {
		vhdlButton.addKeyListener(fileDropHandler);
		hlddButton.addKeyListener(fileDropHandler);
	}


	public JButton getHlddButton() {
		return hlddButton;
	}

	public JTextField getHlddTextField() {
		return hlddTextField;
	}

	public JButton getVhdlButton() {
		return vhdlButton;
	}

	public JTextField getVhdlTextField() {
		return vhdlTextField;
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public void addFileGenerator(DocumentListener outputFileGenerator) {
		vhdlTextField.getDocument().addDocumentListener(outputFileGenerator);
	}
}
