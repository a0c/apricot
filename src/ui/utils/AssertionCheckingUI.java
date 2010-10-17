package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class AssertionCheckingUI extends UIThread {
	public AssertionCheckingUI(JFrame owner) {
		super(
				owner,
				false, "Assertion checker",
				"Checking assertions...",
				"Assertions checking completed",
				"Checking done",
				true);
	}
}
