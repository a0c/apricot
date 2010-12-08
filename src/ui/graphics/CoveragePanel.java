package ui.graphics;

import ui.base.AbstractCoverage;
import ui.base.SplitCoverage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Collection;


/**
 * @author Anton Chepurov
 */
public class CoveragePanel extends JPanel {

	private JPanel hlddInternalPanel;
	private JPanel vhdlInternalPanel;

	private CoveragePanel() {
		setLayout(new GridLayout(1, 2));

		JPanel hlddPanel = new JPanel();
		hlddPanel.setLayout(new BoxLayout(hlddPanel, BoxLayout.PAGE_AXIS));
		Border hlddBorder = BorderFactory.createEtchedBorder();
		hlddBorder = BorderFactory.createTitledBorder(hlddBorder, "HLDD", TitledBorder.CENTER, TitledBorder.TOP);
		hlddPanel.setBorder(hlddBorder);
		hlddInternalPanel = new JPanel();
		hlddInternalPanel.setLayout(new BoxLayout(hlddInternalPanel, BoxLayout.PAGE_AXIS));
		JScrollPane hlddScrollPane = new JScrollPane(hlddInternalPanel);
		hlddScrollPane.setBorder(null);
		hlddPanel.add(hlddScrollPane);

		JPanel vhdlPanel = new JPanel();
		vhdlPanel.setLayout(new BoxLayout(vhdlPanel, BoxLayout.PAGE_AXIS));
		Border vhdlBorder = BorderFactory.createEtchedBorder();
		vhdlBorder = BorderFactory.createTitledBorder(vhdlBorder, "VHDL", TitledBorder.CENTER, TitledBorder.TOP);
		vhdlPanel.setBorder(vhdlBorder);
		vhdlInternalPanel = new JPanel();
		vhdlInternalPanel.setLayout(new BoxLayout(vhdlInternalPanel, BoxLayout.PAGE_AXIS));
		JScrollPane vhdlScrollPane = new JScrollPane(vhdlInternalPanel);
		vhdlScrollPane.setBorder(null);
		vhdlPanel.add(vhdlScrollPane);

		add(hlddPanel);
		add(vhdlPanel);
	}

	public CoveragePanel(AbstractCoverage hlddNodeCoverage, AbstractCoverage hlddEdgeCoverage,
						 AbstractCoverage hlddToggleCoverage, SplitCoverage hlddCondCoverage,
						 Collection<? extends AbstractCoverage> vhdlNodeCoverages) {
		this();

		/* Don't show the frame if none of the coverages is available */ //todo...
		if (hlddNodeCoverage == null && hlddEdgeCoverage == null && hlddToggleCoverage == null && hlddCondCoverage == null
				&& (vhdlNodeCoverages == null || vhdlNodeCoverages.isEmpty())) {
			return;
		}

		/* Create panels from input data and add them to the HLDD panel */
		addHLDDCoverage(hlddNodeCoverage);
		addHLDDCoverage(hlddEdgeCoverage);
		addHLDDCoverage(hlddToggleCoverage);
		addHLDDCoverage(hlddCondCoverage);

		if (vhdlNodeCoverages != null) {
			for (AbstractCoverage vhdlNodeCoverage : vhdlNodeCoverages) {
				addVHDLCoverage(vhdlNodeCoverage, vhdlNodeCoverage.getTooltip());
			}
		}
	}

	public void addVHDLCoverage(AbstractCoverage vhdlCoverage, String toolTipText) {
		addCoverage(vhdlCoverage, vhdlInternalPanel, toolTipText);
	}

	public void addHLDDCoverage(AbstractCoverage hlddCoverage) {
		addCoverage(hlddCoverage, hlddInternalPanel, null);
	}

	private void addCoverage(AbstractCoverage coverage, JPanel destinationPanel, String toolTipText) {
		/* Only show those coverages that are available */
		if (coverage != null) {
			CoverageBar coverageBar = new CoverageBar(coverage);
			coverageBar.setToolTipText(toolTipText);
			destinationPanel.add(coverageBar);
		}
	}
}
