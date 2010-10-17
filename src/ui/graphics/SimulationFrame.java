package ui.graphics;

import sun.awt.VerticalBagLayout;

import javax.swing.*;

import java.awt.*;
import java.util.Collection;

/**
 * @author Anton Chepurov
 */
public class SimulationFrame extends JFrame {

	private JComponent mainPanel;

	public SimulationFrame(long[][] variableValuesArray, char[][] assertionValuesArray, String[] variableNames,
						   Collection booleanIndices, int patternCount, String title) {
		super(title);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		/* Create panels from input data and add them to the frame */
		mainPanel = createPanel(variableValuesArray, assertionValuesArray, variableNames, booleanIndices, patternCount);
		add(mainPanel, BorderLayout.CENTER);
	}

	private JComponent createPanel(long[][] variableValuesArray, char[][] assertionValuesArray,
								   String[] variableNames, Collection booleanIndices, int patternCount) {
		/* Check the number of Variable Names to be equal to the total number of variable and assertion values.
		* If their amounts are different, then discard Variable Names*/
		if (variableNames != null && variableNames.length != variableValuesArray.length + assertionValuesArray.length) {
			variableNames = null;
			String message = "Number of variables in TGM/AGM file is different from the one in CHK/SIM file." +
					"\nVariable and property names will not be shown on the waveform.";
			JOptionPane.showMessageDialog(null, message, "Note!", JOptionPane.INFORMATION_MESSAGE);
		}

		int variableNameIndex = 0;
		String longestVarName = getLongestVarName(variableNames);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(Color.cyan);

		/* LINE TITLES */
		JScrollPane titlesScrollPane = null;
		if (variableNames != null) {
			JPanel titlesPanel = new JPanel();
			titlesPanel.setLayout(new VerticalBagLayout());
			titlesPanel.setBackground(Color.black);

			/* Create Empty Border line */
			titlesPanel.add(Box.createVerticalStrut(AbstractShape.SIZE / 2));

			/* Var Names */
			for (String varName : variableNames) {
				titlesPanel.add(new TitleLine(varName, longestVarName));
			}
			/* CLOCK */
			titlesPanel.add(new TitleLine("CLOCK", longestVarName));

			titlesScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			titlesScrollPane.setBackground(Color.black);
			titlesScrollPane.getViewport().add(titlesPanel);

			mainPanel.add(titlesScrollPane, BorderLayout.LINE_START);
		}

		/* Create TOP PANEL and add LINES into it */
		JPanel linesPanel = new JPanel();
		linesPanel.setLayout(new VerticalBagLayout());
		linesPanel.setBackground(Color.black);

		/* Create Empty Border line */
		linesPanel.add(Box.createVerticalStrut(AbstractShape.SIZE / 2));

		/* Create VARIABLE LINES */
		for (int i = 0; i < variableValuesArray.length; i++, variableNameIndex++) {
			long[] variableValues = variableValuesArray[i];
			if (variableValues != null) {
				linesPanel.add(new VariableLine(patternCount,
						booleanIndices.contains(i), variableValues));
			}
		}
		/* Create ASSERTION LINES */
		for (int i = 0; i < assertionValuesArray.length; i++, variableNameIndex++) {
			char[] assertionValues = assertionValuesArray[i];
			String toolTip = variableNames == null ? "Property " + (i + 1) : variableNames[variableNameIndex];
			linesPanel.add(new AssertionLine(toolTip, patternCount, assertionValues));
		}

		/* Create CLOCK AXIS */
		linesPanel.add(new ClockAxisLine(patternCount > 0 ? patternCount : 0));

		/* SCROLL pane */
		JScrollPane linesScrollPane = new JScrollPane();
		linesScrollPane.setBackground(Color.black);
		linesScrollPane.getViewport().add(linesPanel);

		mainPanel.add(linesScrollPane);
		if (titlesScrollPane != null) {
			SyncronousScrollBarsListener.linkScrollPanes(linesScrollPane, titlesScrollPane);
		}

		return mainPanel;
	}

	private String getLongestVarName(String[] variableNames) {
		if (variableNames == null) {
			return null;
		}
		/* Get longest varName */
		String longestString = null;
		for (String varName : variableNames) {
			if (longestString == null) {
				longestString = varName;
				continue;
			}
			if (varName.length() > longestString.length()) {
				longestString = varName;
			}
		}
		return longestString;
	}

	public JComponent getMainPanel() {
		return mainPanel;
	}
}
