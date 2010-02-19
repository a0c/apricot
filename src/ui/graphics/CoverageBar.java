package ui.graphics;

import ui.base.AbstractCoverage;
import ui.base.SplittedCoverage;
import ui.base.PureCoverage;

import javax.swing.*;
import javax.swing.plaf.ProgressBarUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 23:56:11
 */
public class CoverageBar extends JPanel {
    private static final int threshold_good = 90;
    private static final int threshold_week = 50;
    private static final Font FONT = new Font("arial", Font.BOLD, 14);
    private static final Color FOREGROUND_COLOR = Color.BLACK;

    public CoverageBar(AbstractCoverage coverage) {
        this.setBorder(BorderFactory.createEtchedBorder());
        /* Create TopPanel */
        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(300, 25));
        topPanel.setMinimumSize(new Dimension(300, 25));
        topPanel.setMaximumSize(new Dimension(300, 25));
        topPanel.setLayout(new BorderLayout());
        /* Add TopPanel */
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;
        this.add(Box.createVerticalGlue(), c);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.NONE;
        this.add(topPanel, c);
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.VERTICAL;
        this.add(Box.createVerticalGlue(), c);

        JLabel titleLabel = new JLabel(coverage.getTitle());
        titleLabel.setFont(FONT);

        /* Set BAR VALUES */
        JProgressBar bar;
        if (coverage instanceof SplittedCoverage) {
            SplittedCoverage splittedCoverage = (SplittedCoverage) coverage;
            bar = new JProgressBar(0, splittedCoverage.getTotal());
            bar.setUI(new BlackTitledProgressBarUI());
            bar.setValue(splittedCoverage.getCovered());
            bar.setString(coverage.percentageAsString() +  "% (" + coverage.toString() + ")");
        } else {
            PureCoverage pureCoverage = (PureCoverage) coverage;
            bar = new JProgressBar(0, 100);
            bar.setUI(new BlackTitledProgressBarUI());
            bar.setValue((int) (pureCoverage.getCoverage() * 100));
            bar.setString(coverage.percentageAsString() + "%");
            bar.setForeground(FOREGROUND_COLOR);
        }

        /* Set STRING */
        bar.setStringPainted(true);
        bar.setFont(FONT);

        /* Set COLOR */
        int percent = (int) (bar.getPercentComplete() * 100);
        bar.setForeground(
                percent >= threshold_good
                        ? Color.GREEN.darker() : percent >= threshold_week
                        ? Color.ORANGE : Color.RED
        );

        topPanel.add(titleLabel, BorderLayout.LINE_START);
        topPanel.add(bar, BorderLayout.LINE_END);
    }

    private class BlackTitledProgressBarUI extends BasicProgressBarUI {
        public Color getSelectionForeground() {
            return Color.BLACK;
        }
    }
}
