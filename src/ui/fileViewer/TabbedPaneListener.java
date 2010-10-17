package ui.fileViewer;

import ui.ApplicationForm;
import ui.SingleFileSelector;
import ui.ExtendedException;
import ui.TooltipShowingFocusAdapter;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;

/**
 * @author Anton Chepurov
 */
public class TabbedPaneListener extends MouseSelectionAdapter {

	private final ApplicationForm applicationForm;

	private final JPanel clickMePanel;

	public TabbedPaneListener(ApplicationForm applicationForm, JTabbedPane tabbedPane, JPanel clickMePanel) {
		super(tabbedPane);
		this.applicationForm = applicationForm;
		this.clickMePanel = clickMePanel;
		addMouseListenerToTab();
	}

	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		if (isClickMePanel(source)) {
			String[] extensions;
			String title;
			String invalidFileMessage;
			if (e.getButton() == MouseEvent.BUTTON1) {
				extensions = new String[]{"vhd", "vhdl"};
				title = "Open VHDL file";
				invalidFileMessage = "Selected file is not a VHDL file";
			} else {
				extensions = null;
				title = "Open file";
				invalidFileMessage = null;
			}
			SingleFileSelector fileSelector = SingleFileSelector.getInstance(SingleFileSelector.DialogType.OPEN,
					extensions, null, title, invalidFileMessage);
			if (fileSelector.isFileSelected()) {
				try {
					fileSelector.validateFile();
				} catch (ExtendedException e1) {
					throw new RuntimeException(e1);
				}
				File selectedFile = fileSelector.getRestrictedSelectedFile();
				if (!selectedFile.exists()) {
					String message = "The specified file is not found:\n" + selectedFile.getAbsolutePath();
					//noinspection ThrowableInstanceNeverThrown
					applicationForm.showErrorMessage(new ExtendedException(message, ExtendedException.FILE_NOT_FOUND_TEXT));
					return;
				}
				applicationForm.addFileViewerTabFromFile(selectedFile, null, null, tabbedPane);
			}
		} else {
			super.mouseClicked(e);
		}
	}

	private void addMouseListenerToTab() {
		final JLabel clickMeLabel = new JLabel("Load file");
		tabbedPane.setTabComponentAt(clickMePanel == null ? 0 : tabbedPane.indexOfComponent(clickMePanel), clickMeLabel);
		clickMeLabel.addMouseListener(this);
		tabbedPane.addFocusListener(new TooltipShowingFocusAdapter());
		tabbedPane.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER) {
					mouseClicked(new MouseEvent(clickMeLabel, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, 1, false,
							e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1));
				} else if (keyCode == KeyEvent.VK_DELETE) {
					Component activeTabComponent = tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex());
					if (!isClickMePanel(activeTabComponent)) {
						((TabComponent) activeTabComponent).getButton().doClick();
					}
				}
			}
		});
	}

	private boolean isClickMePanel(Object source) {
		return source != null && (source == clickMePanel
				|| source == tabbedPane.getTabComponentAt(tabbedPane.indexOfComponent(clickMePanel)));
	}
}
