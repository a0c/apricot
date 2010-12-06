package ui.fileViewer;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Anton Chepurov
 */
public class TabMover extends KeyAdapter {

	private final JTabbedPane tabbedPane;

	public TabMover(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

	@Override
	public void keyReleased(KeyEvent e) {

		if (e.isAltDown() && e.isShiftDown()) {

			if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT) {

				moveTab();
			}
		}
	}

	private void moveTab() {

		int index = tabbedPane.getSelectedIndex();
		TabComponent tabComponent = (TabComponent) tabbedPane.getTabComponentAt(index);

		for (MouseListener mouseListener : tabComponent.getMouseListeners()) {
			mouseListener.mouseClicked(new MouseEvent(tabComponent, 0, 0, 0, 0, 0, 2, false));
		}
	}

}
