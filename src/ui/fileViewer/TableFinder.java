package ui.fileViewer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anton Chepurov
 */
public class TableFinder {

	private final JTabbedPane tabbedPane;

	public TableFinder(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

	public JTable find() {
		return findTable(tabbedPane.getSelectedComponent());
	}

	private JTable findTable(Component component) {
		if (component instanceof JTable) {
			return (JTable) component;
		} else if (component instanceof JPanel) {
			JTable table = findTableIn(((JPanel) component).getComponents());
			if (table != null) {
				return table;
			}
		} else if (component instanceof JScrollPane) {
			JTable table = findTableIn(((JScrollPane) component).getViewport().getComponents());
			if (table != null) {
				return table;
			}
		}
		return null;
	}

	private JTable findTableIn(Component... components) {
		if (components == null) {
			return null;
		}
		for (Component component : components) {
			JTable table = findTable(component);
			if (table != null) {
				return table;
			}
		}
		return null;
	}

}
