package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class CoverageAnalyzingUI extends UIThread {
	public CoverageAnalyzingUI(JFrame frame) {
		super(
				frame,
				false,
				"Coverage analyzer",
				"Analyzing coverage...",
				"Coverage analysis completed",
				"Analysis done",
				true);
	}
}
