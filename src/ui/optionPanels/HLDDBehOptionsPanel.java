package ui.optionPanels;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 23.02.2008
 * <br>Time: 22:13:06
 */
public class HLDDBehOptionsPanel {
    private JPanel mainPanel;
    private JCheckBox trimTerminalNodesCheckBox;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public boolean shouldTrimTerminalNodes() {
        return trimTerminalNodesCheckBox.isSelected();
    }
}
