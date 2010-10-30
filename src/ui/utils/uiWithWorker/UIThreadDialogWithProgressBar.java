package ui.utils.uiWithWorker;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class UIThreadDialogWithProgressBar {

	private JPanel mainPanel;
	private JProgressBar progressBar;
	private JLabel label;


	public JPanel getMainPanel() {
		return mainPanel;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JLabel getLabel() {
		return label;
	}
}
