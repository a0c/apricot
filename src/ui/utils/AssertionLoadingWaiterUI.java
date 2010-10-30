package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class AssertionLoadingWaiterUI extends UIThread {
	public AssertionLoadingWaiterUI(JFrame owner, boolean visible) {
		super(
				owner,
				false,
				"Drawing",
				"Drawing stimuli...",
				visible);
	}
}
