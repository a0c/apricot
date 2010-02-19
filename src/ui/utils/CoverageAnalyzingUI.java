package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 22:49:51
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
