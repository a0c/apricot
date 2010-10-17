package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class CoverageVizualizingUI extends UIThread {
	public CoverageVizualizingUI(JFrame owner) {
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
