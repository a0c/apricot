package ui.fileViewer;

import io.QuietCloser;
import ui.ApplicationForm.FileDropHandler;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.*;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class TableForm {

	private static final Color NODE_DEFAULT_COLOR = Color.ORANGE;
	private static final Color EDGE_DEFAULT_COLOR = Color.CYAN;
	private static final Color CANDIDATES1_DEFAULT_COLOR = Color.PINK;
	private static final Color CANDIDATES2_DEFAULT_COLOR = Color.YELLOW;

	private static final String COLUMN_1_TITLE = "Nr.";
	private static final String COLUMN_2_TITLE = "File line";

	private static final int COLUMN_1_MAX_WIDTH = 50;

	private JTable aTable;
	private JPanel mainPanel;
	private JCheckBox nodesCheckBox;
	private JCheckBox edgesCheckBox;
	private JCheckBox candidates1CheckBox;
	private JCheckBox candidates2CheckBox;
	private Color nodesColor = NODE_DEFAULT_COLOR;
	private Color edgesColor = EDGE_DEFAULT_COLOR;
	private Color candidates1Color = CANDIDATES1_DEFAULT_COLOR;
	private Color candidates2Color = CANDIDATES2_DEFAULT_COLOR;

	private String maxLine = "";
	private final LinesStorage linesStorage;

	public TableForm(File selectedFile, int totalVisibleWidth, LinesStorage linesStorage, FileDropHandler fileDropHandler) {
		this.linesStorage = linesStorage;
		this.linesStorage.setOffset(1);
		nodesCheckBox.addKeyListener(fileDropHandler);
		candidates1CheckBox.addKeyListener(fileDropHandler);
		candidates2CheckBox.addKeyListener(fileDropHandler);
		/* Read File */
		String[][] indicesAndFileLines = readFileAsLines(selectedFile);
		/* Create table */
		DefaultTableModel tableModel = new NonEditableTableModel();
		tableModel.addColumn(COLUMN_1_TITLE, indicesAndFileLines[0]);
		tableModel.addColumn(COLUMN_2_TITLE, indicesAndFileLines[1]);
		aTable.setModel(tableModel);
		aTable.setShowHorizontalLines(false);
		aTable.getTableHeader().setReorderingAllowed(false);
		/* 1st Column max width */
		TableColumnModel columnModel = aTable.getColumnModel();
		columnModel.getColumn(0).setMaxWidth(COLUMN_1_MAX_WIDTH);
		columnModel.getColumn(0).setMinWidth(COLUMN_1_MAX_WIDTH);
		int column2MaxWidth = Math.max(
				totalVisibleWidth - COLUMN_1_MAX_WIDTH - aTable.getIntercellSpacing().width * 3
						- UIManager.getInt("ScrollBar.width") - 1,
				SwingUtilities.computeStringWidth(aTable.getFontMetrics(aTable.getFont()), maxLine) + 5);
		columnModel.getColumn(1).setMinWidth(column2MaxWidth);
		columnModel.getColumn(1).setMaxWidth(column2MaxWidth);
//        columnModel.getColumn(1).setPreferredWidth(SwingUtilities.computeStringWidth(aTable.getFontMetrics(aTable.getFont()), maxLine) + 5);
		/* Cell Renderer */
		columnModel.getColumn(0).setCellRenderer(new ColorAndTooltipCellRenderer());
		columnModel.getColumn(1).setCellRenderer(new ColorAndTooltipCellRenderer());

		ChangeListener tableRepainter = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				aTable.repaint();
			}
		};
		MouseAdapter colorChanger = new ColorChangingMouseAdapter(this);
		nodesCheckBox.addChangeListener(tableRepainter);
		edgesCheckBox.addChangeListener(tableRepainter);
		candidates1CheckBox.addChangeListener(tableRepainter);
		candidates2CheckBox.addChangeListener(tableRepainter);
		nodesCheckBox.addMouseListener(colorChanger);
		edgesCheckBox.addMouseListener(colorChanger);
		candidates1CheckBox.addMouseListener(colorChanger);
		candidates2CheckBox.addMouseListener(colorChanger);

		nodesCheckBox.setBackground(nodesColor);
		edgesCheckBox.setBackground(edgesColor);
		candidates1CheckBox.setBackground(candidates1Color);
		candidates2CheckBox.setBackground(candidates2Color);
	}

	private String[][] readFileAsLines(File file) {
		String[][] returnObject = new String[2][];
		LineNumberReader numberReader = null;
		try {
			numberReader = new LineNumberReader(new FileReader(file));
			String line;
			java.util.List<String> lineList = new LinkedList<String>();
			java.util.List<String> indicesList = new LinkedList<String>();
			int i = 1;
			while ((line = numberReader.readLine()) != null) {
				line = line.replaceAll("\t", "    ");
				lineList.add(line);
				indicesList.add(String.valueOf(i++));
				if (line.length() > maxLine.length()) {
					maxLine = line;
				}
			}
			returnObject[0] = indicesList.toArray(new String[indicesList.size()]);
			returnObject[1] = lineList.toArray(new String[lineList.size()]);
			return returnObject;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			QuietCloser.closeQuietly(numberReader);
		}
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public void setColorFor(JCheckBox checkBox, Color newColor) {
		checkBox.setBackground(newColor);
		if (checkBox == nodesCheckBox) {
			nodesColor = newColor;
		} else if (checkBox == edgesCheckBox) {
			edgesColor = newColor;
		} else if (checkBox == candidates1CheckBox) {
			candidates1Color = newColor;
		} else if (checkBox == candidates2CheckBox) {
			candidates2Color = newColor;
		}
		aTable.repaint();
	}

	private class ColorAndTooltipCellRenderer extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (isSelected) {
				if (isUncoveredNode(row)) {
					cell.setBackground(table.getSelectionBackground().brighter().brighter());
					setToolTipText(linesStorage.generateNodeStat(row));
				} else if (isUncoveredEdge(row)) {
					cell.setBackground(table.getSelectionBackground().brighter().brighter());
					setToolTipText(linesStorage.generateEdgeStat(row));
				} else if (isCandidate1(row)) {
					cell.setBackground(table.getSelectionBackground().brighter().brighter());
					setToolTipText(linesStorage.generateCandidate1Stat(row));
				} else if (isCandidate2(row)) {
					cell.setBackground(table.getSelectionBackground().brighter().brighter());
					setToolTipText(linesStorage.generateCandidate2Stat(row));
				} else {
					cell.setBackground(table.getSelectionBackground());
				}
			} else {
				Color bgColor = Color.WHITE;
				setToolTipText(null);
				if (isUncoveredNode(row)) {
					bgColor = nodesColor;
					setToolTipText(linesStorage.generateNodeStat(row));
				} else if (isUncoveredEdge(row)) {
					bgColor = edgesColor;
					setToolTipText(linesStorage.generateEdgeStat(row));
				} else if (isCandidate1(row)) {
					bgColor = candidates1Color;
					setToolTipText(linesStorage.generateCandidate1Stat(row));
				} else if (isCandidate2(row)) {
					bgColor = candidates2Color;
					setToolTipText(linesStorage.generateCandidate2Stat(row));
				}
				cell.setBackground(bgColor);
			}
			return cell;
		}

		private boolean isUncoveredEdge(int row) {
			return edgesCheckBox.isSelected() && linesStorage.hasEdgeLine(row);
		}

		private boolean isUncoveredNode(int row) {
			return nodesCheckBox.isSelected() && linesStorage.hasNodeLine(row);
		}

		private boolean isCandidate1(int row) {
			return candidates1CheckBox.isSelected() && linesStorage.hasCandidate1Line(row);
		}

		private boolean isCandidate2(int row) {
			return candidates2CheckBox.isSelected() && linesStorage.hasCandidate2Line(row);
		}

	}

	private class NonEditableTableModel extends DefaultTableModel {

		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
}
