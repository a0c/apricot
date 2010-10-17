package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class AssertionLoadingUI extends UIThread {
	public AssertionLoadingUI(JFrame owner) {
		super(
				owner,
				true,
				"Simulation stimuli loader",
				"Loading simulation stimuli",
				null,
				null,
				true);
	}
}
