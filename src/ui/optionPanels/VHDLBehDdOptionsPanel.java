package ui.optionPanels;

import ui.BusinessLogic;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 23.02.2008
 * <br>Time: 19:54:02
 */
public class VHDLBehDdOptionsPanel {
    private JPanel mainPanel;
    private JCheckBox reuseConstantsCheckBox;
    private JCheckBox simplifyCheckBox;
    private JRadioButton reducedRadioButton;
    private JRadioButton minimizedRadioButton;
    private JRadioButton expandCSCheckBox;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public boolean shouldReuseConstants() {
        return reuseConstantsCheckBox.isSelected();
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
