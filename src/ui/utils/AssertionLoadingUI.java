package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 20:48:00
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
