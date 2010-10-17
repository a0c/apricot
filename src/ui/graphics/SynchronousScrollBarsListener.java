package ui.graphics;

import javax.swing.*;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;

/**
 * @author Anton Chepurov
 */
public class SynchronousScrollBarsListener implements AdjustmentListener {

	private final JScrollPane mainPane;

	private final JScrollPane linkedPane;

	public SynchronousScrollBarsListener(JScrollPane mainPane, JScrollPane linkedPane) {
		this.mainPane = mainPane;
		this.linkedPane = linkedPane;
	}

	public static void linkScrollPanes(JScrollPane mainPane, JScrollPane linkedPane) {
		mainPane.getVerticalScrollBar().addAdjustmentListener(new SynchronousScrollBarsListener(mainPane, linkedPane));
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		linkedPane.getVerticalScrollBar().setValue(mainPane.getVerticalScrollBar().getValue());
	}
}