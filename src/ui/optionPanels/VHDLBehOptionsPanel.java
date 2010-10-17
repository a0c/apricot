package ui.optionPanels;

import ui.BusinessLogic.HLDDRepresentationType;
import ui.OutputFileGenerator;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class VHDLBehOptionsPanel {
	private JRadioButton flattenRadioButton;
	private JPanel mainPanel;
	private JRadioButton reducedRadioButton;
	private JRadioButton minimizedRadioButton;
	private JRadioButton asGraphsRadioButton;
	private JRadioButton extraGraphsRadioButton;
	private JRadioButton asFunctionRadioButton;
	private JRadioButton fullRadioButton;


	public VHDLBehOptionsPanel(OutputFileGenerator outputFileGenerator) {
		fullRadioButton.addChangeListener(outputFileGenerator);
		reducedRadioButton.addChangeListener(outputFileGenerator);
		minimizedRadioButton.addChangeListener(outputFileGenerator);
		asFunctionRadioButton.addChangeListener(outputFileGenerator);
		asGraphsRadioButton.addChangeListener(outputFileGenerator);
		flattenRadioButton.addChangeListener(outputFileGenerator);
		extraGraphsRadioButton.addChangeListener(outputFileGenerator);
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public boolean shouldFlattenCS() {
		return flattenRadioButton.isSelected();
	}

	public boolean shouldCreateCSGraphs() {
		return asGraphsRadioButton.isSelected();
	}

	public boolean shouldCreateExtraCSGraphs() {
		return extraGraphsRadioButton.isSelected();
	}

	public HLDDRepresentationType getHlddType() {
		if (reducedRadioButton.isSelected()) {
			return HLDDRepresentationType.REDUCED;
		} else if (minimizedRadioButton.isSelected()) {
			return HLDDRepresentationType.MINIMIZED;
		} else return HLDDRepresentationType.FULL_TREE;
	}
}
