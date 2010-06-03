package ui.optionPanels;

import ui.BusinessLogic.HLDDRepresentationType;
import ui.OutputFileGenerator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 16.12.2008
 * <br>Time: 16:48:15
 */
public class VHDLBehOptionsPanel {
    private JRadioButton flattenRadioButton;
    private JPanel mainPanel;
    private JRadioButton reducedRadioButton;
    private JRadioButton minimizedRadioButton;
	private JRadioButton asGraphsRadioButton;
	private JCheckBox subGraphsCheckBox;
	private JRadioButton asFunctionRadioButton;
	private JRadioButton fullRadioButton;


	public VHDLBehOptionsPanel(OutputFileGenerator outputFileGenerator) {
		DisableSubGraphsListener disableSubGrListener = new DisableSubGraphsListener();
		flattenRadioButton.addChangeListener(disableSubGrListener);
		asGraphsRadioButton.addChangeListener(disableSubGrListener);
		asFunctionRadioButton.addChangeListener(disableSubGrListener);
		fullRadioButton.addChangeListener(outputFileGenerator);
		reducedRadioButton.addChangeListener(outputFileGenerator);
		minimizedRadioButton.addChangeListener(outputFileGenerator);
		asFunctionRadioButton.addChangeListener(outputFileGenerator);
		asGraphsRadioButton.addChangeListener(outputFileGenerator);
		flattenRadioButton.addChangeListener(outputFileGenerator);
		subGraphsCheckBox.addChangeListener(outputFileGenerator);
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
		return subGraphsCheckBox.isSelected();
	}

    public HLDDRepresentationType getHlddType() {
        if (reducedRadioButton.isSelected()) {
            return HLDDRepresentationType.REDUCED;
        } else if (minimizedRadioButton.isSelected()) {
            return HLDDRepresentationType.MINIMIZED;
        } else return HLDDRepresentationType.FULL_TREE;
    }

	private class DisableSubGraphsListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (flattenRadioButton.isSelected() || asGraphsRadioButton.isSelected()) {
				subGraphsCheckBox.setSelected(false);
				subGraphsCheckBox.setEnabled(false);
			} else if (asFunctionRadioButton.isSelected()) {
				subGraphsCheckBox.setEnabled(true);
			}
		}
	}
}
