package ui.buttonPanels;

import ui.ApplicationForm;
import ui.OutputFileGenerator;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author Anton Chepurov
 */
public class BehButtons {
	private JButton hlddButton;
	private JTextField hlddTextField;
	private JButton vhdlButton;
	private JTextField vhdlTextField;
	private JPanel mainPanel;
	private JCheckBox smartNameCheckbox;

	public BehButtons(ApplicationForm.FileDropHandler fileDropHandler) {
		smartNameCheckbox.setMnemonic(KeyEvent.VK_N);

		vhdlButton.addKeyListener(fileDropHandler);
		hlddButton.addKeyListener(fileDropHandler);
		smartNameCheckbox.addKeyListener(fileDropHandler);
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public JButton getHlddButton() {
		return hlddButton;
	}

	public JButton getVhdlButton() {
		return vhdlButton;
	}

	public JTextField getHlddTextField() {
		return hlddTextField;
	}

	public JTextField getVhdlTextField() {
		return vhdlTextField;
	}

	public boolean areSmartNamesAllowed() {
		return smartNameCheckbox.isSelected();
	}

	public void addFileGenerator(OutputFileGenerator outputFileGenerator) {
		vhdlTextField.getDocument().addDocumentListener(outputFileGenerator);
		smartNameCheckbox.addChangeListener(outputFileGenerator);
	}

	public void triggerSmartNames() {
		if (areSmartNamesAllowed()) {
			smartNameCheckbox.doClick();
			smartNameCheckbox.doClick();
		}
	}
}
