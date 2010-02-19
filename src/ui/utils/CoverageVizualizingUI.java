package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 18.12.2008
 * <br>Time: 2:52:36
 */
public class CoverageVizualizingUI extends UIThread {
    public CoverageVizualizingUI(JFrame owner) {
        super(
                owner,
                false,
                "Coverage Vizualizer",
                "Mapping files",
                "Coverage vizualization completed",
                "Vizualization done",
                true);
    }
}
