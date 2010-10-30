package ui.graphics;

import ui.base.AbstractCoverage;
import ui.base.PureCoverage;
import ui.base.SplitCoverage;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

/**
 * @author Anton Chepurov
 */
public class CoverageBar extends JPanel {
	private static final int THRESHOLD_GOOD = 90;
	private static final int THRESHOLD_WEEK = 50;
	private static final Font FONT = new Font("arial", Font.BOLD, 14);
	private static final Color FOREGROUND_COLOR = Color.BLACK;

	public CoverageBar(AbstractCoverage coverage) {
		/* Create TopPanel */
		JPanel topPanel = new JPanel();
		Dimension fixedSize = new Dimension(300, 24);
		topPanel.setPreferredSize(fixedSize);
		topPanel.setMinimumSize(fixedSize);
		topPanel.setMaximumSize(fixedSize);
		topPanel.setLayout(new BorderLayout());
		this.setMinimumSize(fixedSize);
		this.setMaximumSize(fixedSize);

		/* Add TopPanel */
		this.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.VERTICAL;
		this.add(Box.createVerticalGlue(), constraints);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.NONE;
		this.add(topPanel, constraints);
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.fill = GridBagConstraints.VERTICAL;
		this.add(Box.createVerticalGlue(), constraints);

		JLabel titleLabel = new JLabel(coverage.getTitle());
		titleLabel.setFont(FONT);

		/* Set BAR VALUES */
		JProgressBar progressBar;
		if (coverage instanceof SplitCoverage) {
			SplitCoverage splitCoverage = (SplitCoverage) coverage;
			progressBar = new JProgressBar(0, splitCoverage.getTotal());
			progressBar.setUI(new BlackTitledProgressBarUI());
			progressBar.setValue(splitCoverage.getCovered());
			progressBar.setString(coverage.percentageAsString() + "% (" + coverage.toString() + ")");
		} else {
			PureCoverage pureCoverage = (PureCoverage) coverage;
			progressBar = new JProgressBar(0, 100);
			progressBar.setUI(new BlackTitledProgressBarUI());
			progressBar.setValue((int) (pureCoverage.getCoverage() * 100));
			progressBar.setString(coverage.percentageAsString() + "%");
			progressBar.setForeground(FOREGROUND_COLOR);
		}

		/* Set STRING */
		progressBar.setStringPainted(true);
		progressBar.setFont(FONT);

		/* Set COLOR */
		int percent = (int) (progressBar.getPercentComplete() * 100);
		progressBar.setForeground(
				percent >= THRESHOLD_GOOD
						? Color.GREEN.darker() : percent >= THRESHOLD_WEEK
						? Color.ORANGE : Color.RED
		);

		topPanel.add(titleLabel, BorderLayout.LINE_START);
		topPanel.add(progressBar, BorderLayout.LINE_END);
	}

	private class BlackTitledProgressBarUI extends BasicProgressBarUI {
		public Color getSelectionForeground() {
			return Color.BLACK;
		}
	}
}
