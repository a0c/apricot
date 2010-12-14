package ui.fileViewer;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class TableFinder {

	private final JTabbedPane tabbedPane;

	private List<JTable> tables;

	public TableFinder(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

	public List<JTable> findAll() {
		clearTables();
		findTablesIn(tabbedPane.getComponents());
		return tables;
	}

	public JTable find() {
		clearTables();
		findTables(tabbedPane.getSelectedComponent());
		return tables.isEmpty() ? null : tables.get(0);
	}

	private void findTables(Component component) {
		if (component instanceof JTable) {
			tables.add((JTable) component);
		} else if (component instanceof JPanel) {
			findTablesIn(((JPanel) component).getComponents());
		} else if (component instanceof JScrollPane) {
			findTablesIn(((JScrollPane) component).getViewport().getComponents());
		}
	}

	private void findTablesIn(Component... components) {
		if (components == null) {
			return;
		}
		for (Component component : components) {
			findTables(component);
		}
	}

	private void clearTables() {
		tables = new LinkedList<JTable>();
	}
}
