package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 19.12.2008
 * <br>Time: 22:09:34
 */
public class AssertionLoadingWaiterUI extends UIThread {
    public AssertionLoadingWaiterUI(JFrame owner, boolean visible) {
        super(
                owner,
                false,
                "Drawing",
                "Drawing stimuli",
                null,
                null,
                visible);
    }
}
