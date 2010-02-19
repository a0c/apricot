package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 20:01:10
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
