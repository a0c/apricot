package ui.optionPanels;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 17.06.2008
 * <br>Time: 14:43:26
 */
public class PSLOptionsPanel {
    private JButton ppgLibButton;
    private JTextField ppgLibTextField;
    private JPanel mainPanel;

    public PSLOptionsPanel() {
        ppgLibTextField.setOpaque(false);
        ppgLibTextField.setFocusable(false);
    }

    public JButton getPpgLibButton() {
        return ppgLibButton;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getPpgLibTextField() {
        return ppgLibTextField;
    }
}
