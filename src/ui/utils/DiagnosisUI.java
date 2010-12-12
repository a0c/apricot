package ui.utils;

import ui.utils.uiWithWorker.UIThread;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class DiagnosisUI extends UIThread {
	public DiagnosisUI(JFrame frame) {
		super(
				frame,
				false,
				"Diagnosis",
				"Diagnosing design...",
				true
		);
	}
}
