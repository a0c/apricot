package ui;

import javax.swing.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 24.06.2008
 * <br>Time: 14:58:03
 */
public class DemoForm {
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JComboBox parserComboBox;
    private JButton VHDLButton;
    private JButton HLDDButton;
    private JTextArea textAreaHLDD;
    private JTextArea textAreaVHDL;
    private JButton parseButton;
    private JPanel optionsPanel;
    private JPanel notAvailablePanel;
    private JPanel Parsers;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Demo");
        frame.setContentPane(new DemoForm().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UniversalFrameLocator.centerFrame(frame);
        frame.pack();
        frame.setVisible(true);
    }
}
