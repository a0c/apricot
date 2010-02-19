package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 16:41:31
 */
@Deprecated
public class SimulationUI extends UIThread {

    public SimulationUI(JFrame owner) {
        super(
                owner,
                false, "Simulator",
                "Simulating model...",
                "Simulation completed",
                "Simulation done",
                true);
    }
}
