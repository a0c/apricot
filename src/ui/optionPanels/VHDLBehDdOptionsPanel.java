package ui.optionPanels;

import ui.BusinessLogic;
import ui.OutputFileGenerator;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 23.02.2008
 * <br>Time: 19:54:02
 */
public class VHDLBehDdOptionsPanel {
    private JPanel mainPanel;
    private JCheckBox simplifyCheckBox;
    private JRadioButton reducedRadioButton;
    private JRadioButton minimizedRadioButton;
    private JRadioButton expandCSCheckBox;
	private JRadioButton fullRadioButton;

	public VHDLBehDdOptionsPanel(OutputFileGenerator outputFileGenerator) {
		fullRadioButton.addChangeListener(outputFileGenerator);
		reducedRadioButton.addChangeListener(outputFileGenerator);
		minimizedRadioButton.addChangeListener(outputFileGenerator);
		//todo: update ConditionStatement Modes
	}

	public JPanel getMainPanel() {
        return mainPanel;
    }

    public boolean shouldSimplify() {
        return simplifyCheckBox.isSelected();
    }

    public BusinessLogic.HLDDRepresentationType getHlddType() {
        if (reducedRadioButton.isSelected()) {
            return BusinessLogic.HLDDRepresentationType.REDUCED;
        } else if (minimizedRadioButton.isSelected()) {
            return BusinessLogic.HLDDRepresentationType.MINIMIZED;
        } else return BusinessLogic.HLDDRepresentationType.FULL_TREE;
    }

    public boolean shouldExpandCS() {
        return expandCSCheckBox.isSelected();
    }
}
