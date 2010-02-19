package ui.graphics;

import ui.base.AbstractCoverage;
import ui.base.SplittedCoverage;
import ui.graphics.CoverageBar;

import javax.swing.*;

import ui.UniversalFrameLocator;
import ui.IconAdder;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 23:14:37
 */
public class CoverageFrame extends JFrame {
    private JPanel mainPanel;

    public CoverageFrame(AbstractCoverage nodeCoverage, AbstractCoverage edgeCoverage, AbstractCoverage toggleCoverage, String name) {
        super(name);
        /* Don't show the frame if none of the coverages is available */
        if (nodeCoverage == null && edgeCoverage == null && toggleCoverage == null) {
            dispose();
            return;
        }
        IconAdder.setFrameIcon(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        /* Create panels from input data and add them to the frame */
        createMainPanel(nodeCoverage, edgeCoverage, toggleCoverage);
        /* Add created panel to contentPane */
        getContentPane().add(mainPanel);
    }

    public void setVisible() {
        //        setSize(1024, 400); // todo...
        pack();
        UniversalFrameLocator.centerFrame(this);
        setVisible(true);
    }

    private void createMainPanel(AbstractCoverage nodeCoverage, AbstractCoverage edgeCoverage, AbstractCoverage toggleCoverage) {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        /* Only show those coverages that are available */
        if (nodeCoverage != null) {
            CoverageBar nodeBar = new CoverageBar(nodeCoverage);       
            mainPanel.add(nodeBar);
        }
        if (edgeCoverage != null) {
            CoverageBar edgeBar = new CoverageBar(edgeCoverage);
            mainPanel.add(edgeBar);
        }
        if (toggleCoverage != null) {
            CoverageBar toggleBar = new CoverageBar(toggleCoverage);
            mainPanel.add(toggleBar);
        }

    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        new CoverageFrame(new SplittedCoverage(10, 20, "Node Coverage"), new SplittedCoverage(20, 25, "Edge coverage"), new SplittedCoverage(35, 35, "Toggle coverage"), "Title");
    }
}
