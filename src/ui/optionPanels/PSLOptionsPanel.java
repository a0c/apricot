package ui.optionPanels;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class PSLOptionsPanel {
	private JButton ppgLibButton;
	private JTextField ppgLibTextField;
	private JPanel mainPanel;

	public PSLOptionsPanel() {
		ppgLibTextField.setOpaque(false);
		ppgLibTextField.setFocusable(false);
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
