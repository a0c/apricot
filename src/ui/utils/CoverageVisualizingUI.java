package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class CoverageVisualizingUI extends UIThread {
	public CoverageVisualizingUI(JFrame owner) {
		super(
				owner,
				false,
				"Coverage Visualizer",
				"Mapping files",
				"Coverage visualization completed",
				"Visualization done",
				true);
	}
}
