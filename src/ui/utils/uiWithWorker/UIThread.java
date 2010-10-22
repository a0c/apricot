package ui.utils.uiWithWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Anton Chepurov
 */
public abstract class UIThread extends Thread implements UIInterface {

	private JDialog dialog = null;
	private TaskSwingWorker worker = null;
	private JProgressBar progressBar;

	private final boolean withProgressBar;
	private final String completeMessage;
	private final String completeTitle;
	private static final String ERROR_MESSAGE = "Error occurred:\n";
	private static final String ERROR_TITLE = "Error";

	protected UIThread(JFrame owner, boolean withProgressBar, String title, String label, String completeMessage, String completeTitle, boolean visible) {
		this.withProgressBar = withProgressBar;
		this.completeMessage = completeMessage;
		this.completeTitle = completeTitle;

		/* Initialize dialog */
		dialog = new JDialog(owner, title, false);
		UIThreadDialogWithProgressBar dialogWithProgressBar = new UIThreadDialogWithProgressBar();
		dialogWithProgressBar.getLabel().setText(label);
		progressBar = dialogWithProgressBar.getProgressBar();
		if (!withProgressBar) {
			progressBar.setVisible(false);
		}
		/* Borders */
		dialog.setLayout(new BorderLayout());
		dialog.add(Box.createVerticalStrut(10), BorderLayout.PAGE_START);
		dialog.add(Box.createVerticalStrut(10), BorderLayout.PAGE_END);
		dialog.add(Box.createHorizontalStrut(10), BorderLayout.LINE_START);
		dialog.add(Box.createHorizontalStrut(10), BorderLayout.LINE_END);
		/* Message */
		dialog.add(dialogWithProgressBar.getMainPanel());
//		dialog.add(new JLabel(label, JLabel.CENTER));
		/* CLOSE button */
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				/* If algorithm has already been started, stop it */
				if (worker != null) {
					worker.stopWorker();
				}
				/* Dispose the Dialog */
				super.windowClosing(e);
			}
		});
		/* Location */
		dialog.pack();
//		dialog.setSize(200, 100);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(visible);
	}

	public void updateProgressBar(int value, int maxValue) {
		if (withProgressBar) {
			progressBar.setValue(value * progressBar.getMaximum() / maxValue);
		}
	}

	public void hideDialog() {
		dialog.setVisible(false);
		dialog.dispose();
	}

	public void setVisible(boolean visible) {
		dialog.setVisible(visible);
	}

	public void showSuccessDialog() {
		if (completeMessage == null || completeTitle == null) {
			return;
		}
		JOptionPane.showMessageDialog(dialog.getOwner(), completeMessage, completeTitle, JOptionPane.INFORMATION_MESSAGE);
	}

	public void showErrorDialog(String consoleOutput) {
		JOptionPane.showMessageDialog(dialog.getOwner(), ERROR_MESSAGE + consoleOutput, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
	}

	public void setWorker(TaskSwingWorker worker) {
		this.worker = worker;
	}

}
