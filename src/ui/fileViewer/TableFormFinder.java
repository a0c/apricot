package ui.fileViewer;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class TableFormFinder {

	private final JTabbedPane tabbedPane;

	public TableFormFinder(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

	public Collection<TableForm> find() {

		Collection<TableForm> forms = new LinkedList<TableForm>();

		for (Component component : tabbedPane.getComponents()) {

			if (component instanceof TableForm.TableFormPanel) {

				forms.add(((TableForm.TableFormPanel) component).getTableForm());

			}
		}

		return forms;
	}
}
