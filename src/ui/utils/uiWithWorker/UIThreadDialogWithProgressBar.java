package ui.utils.uiWithWorker;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 12.12.2008
 * <br>Time: 13:01:48
 */
public class UIThreadDialogWithProgressBar {
    private JPanel mainPanel;
    private JProgressBar progressBar;
    private JLabel label;


    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getLabel() {
        return label;
    }
}
