package ui.optionPanels;

import ui.BusinessLogic.HLDDRepresentationType;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 16.12.2008
 * <br>Time: 16:48:15
 */
public class VHDLBehOptionsPanel {
    private JRadioButton expandCSCheckBox;
    private JPanel mainPanel;
    private JRadioButton reducedRadioButton;
    private JRadioButton minimizedRadioButton;


    public JPanel getMainPanel() {
        return mainPanel;
    }

    public boolean shouldExpandCS() {
        return expandCSCheckBox.isSelected();
    }

    public HLDDRepresentationType getHlddType() {
        if (reducedRadioButton.isSelected()) {
            return HLDDRepresentationType.REDUCED;
        } else if (minimizedRadioButton.isSelected()) {
            return HLDDRepresentationType.MINIMIZED;
        } else return HLDDRepresentationType.FULL_TREE;
    }

}
