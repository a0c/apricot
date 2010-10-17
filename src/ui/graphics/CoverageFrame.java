package ui.graphics;

import ui.base.AbstractCoverage;

import javax.swing.*;

import ui.IconAdder;

/**
 * @author Anton Chepurov
 */
public class CoverageFrame extends JFrame {
	private JPanel mainPanel;

	public CoverageFrame(AbstractCoverage nodeCoverage, AbstractCoverage edgeCoverage, AbstractCoverage toggleCoverage, String name) {
		super(name);
		/* Don't show the frame if none of the coverages is available */
		if (nodeCoverage == null && edgeCoverage == null && toggleCoverage == null) {
			dispose();
			return;
		}
		IconAdder.setFrameIcon(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		/* Create panels from input data and add them to the frame */
		createMainPanel(nodeCoverage, edgeCoverage, toggleCoverage);
		/* Add created panel to contentPane */
		getContentPane().add(mainPanel);
	}

	private void createMainPanel(AbstractCoverage nodeCoverage, AbstractCoverage edgeCoverage, AbstractCoverage toggleCoverage) {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		/* Only show those coverages that are available */
		if (nodeCoverage != null) {
			CoverageBar nodeBar = new CoverageBar(nodeCoverage);
			mainPanel.add(nodeBar);
		}
		if (edgeCoverage != null) {
			CoverageBar edgeBar = new CoverageBar(edgeCoverage);
			mainPanel.add(edgeBar);
		}
		if (toggleCoverage != null) {
			CoverageBar toggleBar = new CoverageBar(toggleCoverage);
			mainPanel.add(toggleBar);
		}

	}

	public JPanel getMainPanel() {
		return mainPanel;
	}
}
