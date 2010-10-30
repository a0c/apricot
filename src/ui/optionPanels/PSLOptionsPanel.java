package ui.optionPanels;

import ui.ApplicationForm.FileDropHandler;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class PSLOptionsPanel {
	private JButton ppgLibButton;
	private JTextField ppgLibTextField;
	private JPanel mainPanel;

	public PSLOptionsPanel(FileDropHandler fileDropHandler) {
		ppgLibTextField.setOpaque(false);
		ppgLibTextField.setFocusable(false);

		ppgLibButton.addKeyListener(fileDropHandler);
	}

	public JButton getPpgLibButton() {
		return ppgLibButton;
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public JTextField getPpgLibTextField() {
		return ppgLibTextField;
	}
}
