package ui.graphics;

import ui.base.AbstractCoverage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;


/**
 * @author Anton Chepurov
 */
public class CoveragePanel extends JPanel {

	private JPanel hlddPanel, vhdlPanel;
	private AbstractCoverage vhdlNodeCoverage;
	private AbstractCoverage vhdlEdgeCoverage;
	private AbstractCoverage vhdlToggleCoverage;

	private CoveragePanel() {
		setLayout(new GridLayout(1, 2));

		hlddPanel = new JPanel();
		hlddPanel.setLayout(new BoxLayout(hlddPanel, BoxLayout.PAGE_AXIS));
		Border hlddBorder = BorderFactory.createCompoundBorder();
		hlddBorder = BorderFactory.createTitledBorder(hlddBorder, "HLDD", TitledBorder.CENTER, TitledBorder.ABOVE_TOP);
		hlddPanel.setBorder(hlddBorder);

		vhdlPanel = new JPanel();
		vhdlPanel.setLayout(new GridLayout(3, 1));
		Border vhdlBorder = BorderFactory.createCompoundBorder();
		vhdlBorder = BorderFactory.createTitledBorder(vhdlBorder, "VHDL", TitledBorder.CENTER, TitledBorder.ABOVE_TOP);
		vhdlPanel.setBorder(vhdlBorder);

		add(hlddPanel);
		add(vhdlPanel);
	}

	public CoveragePanel(AbstractCoverage hlddNodeCoverage, AbstractCoverage hlddEdgeCoverage, AbstractCoverage hlddToggleCoverage) {
		this();

		/* Don't show the frame if none of the coverages is available */ //todo...
		if (hlddNodeCoverage == null && hlddEdgeCoverage == null && hlddToggleCoverage == null) {
			return;
		}

		/* Create panels from input data and add them to the HLDD panel */
		addHLDDCoverage(hlddNodeCoverage);
		addHLDDCoverage(hlddEdgeCoverage);
		addHLDDCoverage(hlddToggleCoverage);

	}

	public CoveragePanel(AbstractCoverage vhdlNodeCoverage) {
		this();

		this.vhdlNodeCoverage = vhdlNodeCoverage;

		addVHDLCoverage(vhdlNodeCoverage);

	}

	public void addVHDLCoverage(AbstractCoverage vhdlCoverage) {
		addCoverage(vhdlCoverage, vhdlPanel);
	}

	public void addHLDDCoverage(AbstractCoverage hlddCoverage) {
		addCoverage(hlddCoverage, hlddPanel);
	}

	private void addCoverage(AbstractCoverage coverage, JPanel destinationPanel) {
		/* Only show those coverages that are available */
		if (coverage != null) {
			CoverageBar coverageBar = new CoverageBar(coverage);
			destinationPanel.add(coverageBar);
		}
	}

	public void addVHDLCoverageFrom(CoveragePanel vhdlCoveragePanel) {

		vhdlPanel.removeAll();

		AbstractCoverage vhdlCov;

		if ((vhdlCov = vhdlCoveragePanel.vhdlNodeCoverage) != null) {
			vhdlNodeCoverage = vhdlCov;
			addVHDLCoverage(vhdlCov);
		}
		if ((vhdlCov = vhdlCoveragePanel.vhdlEdgeCoverage) != null) {
			vhdlEdgeCoverage = vhdlCov;
			addVHDLCoverage(vhdlCov);
		}
		if ((vhdlCov = vhdlCoveragePanel.vhdlToggleCoverage) != null) {
			vhdlToggleCoverage = vhdlCov;
			addVHDLCoverage(vhdlCov);
		}

		vhdlPanel.validate();
	}
}
