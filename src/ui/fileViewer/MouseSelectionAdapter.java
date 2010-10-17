package ui.fileViewer;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * @author Anton Chepurov
 */
public class MouseSelectionAdapter extends MouseAdapter {

	protected JTabbedPane tabbedPane;

	public MouseSelectionAdapter(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		if (source instanceof Component) {
			tabbedPane.setSelectedIndex(tabbedPane.indexOfTabComponent((Component) source));
		}
	}
}
