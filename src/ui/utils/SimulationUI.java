package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * @author Anton Chepurov
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
