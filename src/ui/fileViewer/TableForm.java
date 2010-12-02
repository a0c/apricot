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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class TableForm {

	private final static int OFFSET = 1;

	private static final Color NODE_DEFAULT_COLOR = Color.ORANGE;
	private static final Color EDGE_DEFAULT_COLOR = Color.CYAN;

	private static final String COLUMN_1_TITLE = "Nr.";
	private static final String COLUMN_2_TITLE = "File line";

	private static final String NODES_TOOLTIP = "Uncovered nodes";
	private static final String EDGES_TOOLTIP = "Uncovered edges";
	private static final int COLUMN_1_MAX_WIDTH = 50;

	private JTable aTable;
	private JPanel mainPanel;
	private JCheckBox nodesCheckBox;
	private JCheckBox edgesCheckBox;
	private Color nodesColor = NODE_DEFAULT_COLOR;
	private Color edgesColor = EDGE_DEFAULT_COLOR;

	private Collection<Integer> nodesLines = new HashSet<Integer>();
	private Collection<Integer> edgesLines = new HashSet<Integer>();

	private String maxLine = "";

	public TableForm(File selectedFile, int totalVisibleWidth, Collection<Integer> nodesLines, Collection<Integer> edgesLines, FileDropHandler fileDropHandler) {
		nodesCheckBox.addKeyListener(fileDropHandler);
		this.nodesLines = nodesLines;
		this.edgesLines = edgesLines;
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

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				aTable.repaint();
			}
		};
		MouseAdapter mouseAdapter = new ColorChangingMouseAdapter(this);
		nodesCheckBox.addChangeListener(changeListener);
		edgesCheckBox.addChangeListener(changeListener);
		nodesCheckBox.addMouseListener(mouseAdapter);
		edgesCheckBox.addMouseListener(mouseAdapter);

		nodesCheckBox.setBackground(nodesColor);
		edgesCheckBox.setBackground(edgesColor);
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
		}
		aTable.repaint();
	}

	private class ColorAndTooltipCellRenderer extends DefaultTableCellRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (isSelected) {
				if (isUncoveredNode(row)) {
					cell.setBackground(table.getSelectionBackground().brighter().brighter());
					setToolTipText(NODES_TOOLTIP);
				} else if (isUncoveredEdge(row)) {
					cell.setBackground(table.getSelectionBackground().brighter().brighter());
					setToolTipText(EDGES_TOOLTIP);
				} else {
					cell.setBackground(table.getSelectionBackground());
				}
			} else {
				Color bgColor = Color.WHITE;
				setToolTipText(null);
				if (isUncoveredNode(row)) {
					bgColor = nodesColor;
					setToolTipText(NODES_TOOLTIP);
				} else if (isUncoveredEdge(row)) {
					bgColor = edgesColor;
					setToolTipText(EDGES_TOOLTIP);
				}
				cell.setBackground(bgColor);
			}
			return cell;
		}

		private boolean isUncoveredEdge(int row) {
			return edgesCheckBox.isSelected() && isUncoveredLine(edgesLines, row);
		}

		private boolean isUncoveredNode(int row) {
			return nodesCheckBox.isSelected() && isUncoveredLine(nodesLines, row);
		}

		private boolean isUncoveredLine(Collection<Integer> linesCollection, int row) {
			return linesCollection != null && linesCollection.contains(row + OFFSET);
		}
	}

	private class NonEditableTableModel extends DefaultTableModel {

		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
}
