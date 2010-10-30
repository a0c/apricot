package ui.fileViewer;

import ui.ApplicationForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Anton Chepurov
 */
public class MouseSelectionAdapter extends MouseAdapter {

	protected JTabbedPane tabbedPane;

	protected JTabbedPane otherTabbedPane;

	public MouseSelectionAdapter(JTabbedPane tabbedPane) {
		this(tabbedPane, null);
	}

	public MouseSelectionAdapter(JTabbedPane tabbedPane, JTabbedPane otherTabbedPane) {
		this.tabbedPane = tabbedPane;
		this.otherTabbedPane = otherTabbedPane;
	}

	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		if (source instanceof Component) {
			tabbedPane.setSelectedIndex(tabbedPane.indexOfTabComponent((Component) source));
			if (otherTabbedPane != null) {
				if (!(source instanceof TabComponent)) {
					return;
				}
				int index = ApplicationForm.findSameFileInOtherTabbedPane(((TabComponent) source).getToolTipText(), otherTabbedPane);
				if (index != -1) {
					otherTabbedPane.setSelectedIndex(index);
				}
			}
		}
	}
}
