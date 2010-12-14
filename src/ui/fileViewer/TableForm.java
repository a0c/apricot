package ui.fileViewer;

import io.QuietCloser;
import ui.ApplicationForm;
import ui.ApplicationForm.FileDropHandler;
import ui.PairedTabbedPane;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class TableForm {

	private static final Color NODE_DEFAULT_COLOR = Color.ORANGE;
	private static final Color EDGE_DEFAULT_COLOR = Color.CYAN;
	private static final Color CANDIDATES1_DEFAULT_COLOR = Color.YELLOW;
	private static final Color CANDIDATES2_DEFAULT_COLOR = Color.PINK;

	static final Border MUTATION_BORDER = BorderFactory.createLineBorder(Color.GREEN, 2);

	private static final String COLUMN_1_TITLE = "Nr.";
	private static final String COLUMN_2_TITLE = "File line";

	private static final int COLUMN_1_MAX_WIDTH = 50;

	private JTable aTable;
	private JPanel mainPanel;
	private JLabel nodesLabel;
	private JCheckBox edgesCheckBox;
	private JLabel candidates1Label;
	private JLabel candidates2Label;
	private Color nodesColor = NODE_DEFAULT_COLOR;
	private Color edgesColor = EDGE_DEFAULT_COLOR;
	private Color candidates1Color = CANDIDATES1_DEFAULT_COLOR;
	private Color candidates2Color = CANDIDATES2_DEFAULT_COLOR;

	private String maxLine = "";
	private final LinesStorage linesStorage;
	private final ApplicationForm applicationForm;
	private boolean showMutation = false;
	private TabComponent tabComponent;

	public TableForm(File selectedFile, int totalVisibleWidth, LinesStorage linesStorage,
					 FileDropHandler fileDropHandler, ApplicationForm applicationForm) {
		this.linesStorage = linesStorage;
		this.applicationForm = applicationForm;
		this.linesStorage.setOffset(1);
		this.linesStorage.setTableForm(this);

		nodesLabel.setText(nodesLabel.getText() + this.linesStorage.generateNodesInfo());
		nodesLabel.setVisible(this.linesStorage.hasNodes());
		candidates1Label.setText(candidates1Label.getText() + this.linesStorage.generateCandidates1Info());
		candidates1Label.setVisible(this.linesStorage.hasCandidates1());
		candidates2Label.setText(candidates2Label.getText() + this.linesStorage.generateCandidates2Info());
		candidates2Label.setVisible(this.linesStorage.hasCandidates2());

		nodesLabel.setToolTipText(this.linesStorage.generateNodesStat());
		edgesCheckBox.setToolTipText(this.linesStorage.generateEdgesStat());
		candidates1Label.setToolTipText(this.linesStorage.generateCandidates1Stat());
		candidates2Label.setToolTipText(this.linesStorage.generateCandidates2Stat());

		aTable.addKeyListener(fileDropHandler);
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
		edgesCheckBox.addChangeListener(tableRepainter);
		nodesLabel.addMouseListener(colorChanger);
		edgesCheckBox.addMouseListener(colorChanger);
		candidates1Label.addMouseListener(colorChanger);
		candidates2Label.addMouseListener(colorChanger);

		nodesLabel.setBackground(nodesColor);
		edgesCheckBox.setBackground(edgesColor);
		candidates1Label.setBackground(candidates1Color);
		candidates2Label.setBackground(candidates2Color);

		aTable.addKeyListener(new UpAndDownJumper());
		aTable.addMouseMotionListener(new TooltipCleaner());
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

	public Color getColor() {
		if (linesStorage.hasCandidates2()) {
			return candidates2Color;
		}
		if (linesStorage.hasCandidates1()) {
			return candidates1Color;
		}
		if (linesStorage.hasNodes()) {
			return nodesColor;
		}
		return null;
	}

	public void setColorFor(JComponent component, Color newColor) {
		component.setBackground(newColor);
		if (component == nodesLabel) {
			nodesColor = newColor;
		} else if (component == edgesCheckBox) {
			edgesColor = newColor;
		} else if (component == candidates1Label) {
			candidates1Color = newColor;
		} else if (component == candidates2Label) {
			candidates2Color = newColor;
		}
		aTable.repaint();
	}

	boolean isNodesSelected() {
		return applicationForm.isCovNodesSelected();
	}

	boolean isEdgesSelected() {
		return edgesCheckBox.isSelected();
	}

	boolean isCandidates1Selected() {
		return applicationForm.isDiagCandidates1Selected();
	}

	boolean isCandidates2Selected() {
		return applicationForm.isDiagCandidates2Selected();
	}

	private void createUIComponents() {
		mainPanel = new TableFormPanel(this);
		aTable = new TableWithSecondRowUnselectable();
	}

	@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
	public boolean setShowingMutation(boolean show) {
		showMutation = show;
		aTable.repaint();
		boolean isShowing = linesStorage.hasMutation() && showMutation;
		if (isShowing) {
			scrollToSelection(linesStorage.getFirstMutationLine());
		}
		return isShowing;
	}

	public boolean isShowingMutation() {
		return showMutation;
	}

	public void setTabComponent(TabComponent tabComponent) {
		this.tabComponent = tabComponent;
	}

	public TabComponent getTabComponent() {
		return tabComponent;
	}

	private void scrollToSelection(int targetRow) {
		int overScrollRow = calculateOverScrollRow(targetRow);
		aTable.scrollRectToVisible(aTable.getCellRect(overScrollRow, 0, true));
	}

	private int calculateOverScrollRow(int targetRow) {

		int selectedRow = aTable.getSelectedRow();
		boolean isMovingDown = selectedRow == -1 || selectedRow <= targetRow;

		int overScrollRow = targetRow;
		if (isMovingDown) {
			int maxRow = aTable.getRowCount() - 1;
			if (overScrollRow + 10 < maxRow) {
				overScrollRow += 10;
			} else {
				overScrollRow = maxRow;
			}
		} else {
			int minRow = 0;
			if (overScrollRow - 10 > minRow) {
				overScrollRow -= 10;
			} else {
				overScrollRow = minRow;
			}
		}

		return overScrollRow;
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
				} else if (isCandidate2(row)) {
					cell.setBackground(table.getSelectionBackground().brighter().brighter());
					setToolTipText(linesStorage.generateCandidate2Stat(row));
				} else if (isCandidate1(row)) {
					cell.setBackground(table.getSelectionBackground().brighter().brighter());
					setToolTipText(linesStorage.generateCandidate1Stat(row));
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
				} else if (isCandidate2(row)) {
					bgColor = candidates2Color;
					setToolTipText(linesStorage.generateCandidate2Stat(row));
				} else if (isCandidate1(row)) {
					bgColor = candidates1Color;
					setToolTipText(linesStorage.generateCandidate1Stat(row));
				}
				cell.setBackground(bgColor);
			}
			if (linesStorage.hasMutationLine(row)) {
				if (cell instanceof JComponent) {
					((JComponent) cell).setBorder(showMutation ? MUTATION_BORDER : null);
				}
			}
			return cell;
		}

		private boolean isUncoveredEdge(int row) {
			return isEdgesSelected() && linesStorage.hasEdgeLine(row);
		}

		private boolean isUncoveredNode(int row) {
			return isNodesSelected() && linesStorage.hasNodeLine(row);
		}

		private boolean isCandidate1(int row) {
			return isCandidates1Selected() && linesStorage.hasCandidate1Line(row);
		}

		private boolean isCandidate2(int row) {
			return isCandidates2Selected() && linesStorage.hasCandidate2Line(row);
		}

	}

	private class NonEditableTableModel extends DefaultTableModel {

		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	private class UpAndDownJumper extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent e) {

			if (e.isAltDown() && e.isShiftDown()) {

				if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT) {

					moveTab();
				}
			} else if (e.isControlDown()) {

				if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT) {

					jumpToOtherTabbedPane();
				}
			} else if (e.isAltDown()) {

				if (e.getKeyCode() == KeyEvent.VK_UP) {

					gotoPreviousLine();

				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {

					gotoNextLine();

				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {

					switchNextTab();

				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {

					switchPreviousTab();
				}
			}
		}

		private void switchNextTab() {

			JTabbedPane tabbedPane = findTabbedPane();
			if (tabbedPane == null) return;

			int selected = tabbedPane.getSelectedIndex();
			int total = tabbedPane.getTabCount();

			int next = selected == (total - 1) ? 0 : selected + 1;

			tabbedPane.setSelectedIndex(next);
			tabbedPane.repaint();
		}

		private void switchPreviousTab() {

			JTabbedPane tabbedPane = findTabbedPane();
			if (tabbedPane == null) return;

			int selected = tabbedPane.getSelectedIndex();
			int total = tabbedPane.getTabCount();

			int previous = selected == 0 ? total - 1 : selected - 1;

			tabbedPane.setSelectedIndex(previous);
			tabbedPane.repaint();
		}

		private void jumpToOtherTabbedPane() {

			JTabbedPane tabbedPane = findTabbedPane();

			if (tabbedPane instanceof PairedTabbedPane) {

				JTabbedPane otherTabbedPane = ((PairedTabbedPane) tabbedPane).getPair();

				JTable table = new TableFinder(otherTabbedPane).find();
				if (table != null) {
					table.requestFocus();
				} else {
					otherTabbedPane.requestFocus();
				}
			}
		}

		private void moveTab() {

			JTabbedPane tabbedPane = findTabbedPane();
			if (tabbedPane == null) return;

			int i = tabbedPane.indexOfComponent(mainPanel);
			TabComponent tabComponent = (TabComponent) tabbedPane.getTabComponentAt(i);

			for (MouseListener mouseListener : tabComponent.getMouseListeners()) {
				mouseListener.mouseClicked(new MouseEvent(tabComponent, 0, 0, 0, 0, 0, 2, false));
			}
		}

		private JTabbedPane findTabbedPane() {
			Container parent;
			while (!((parent = mainPanel.getParent()) instanceof JTabbedPane)) {
				if (parent == null) {
					break;
				}
			}
			if (parent == null) {
				return null;
			}
			return (JTabbedPane) parent;
		}

		private void gotoNextLine() {
			int row = aTable.getSelectedRow();
			if (row == -1) {
				return;
			}
			int targetRow = linesStorage.findNextLine(row);
			if (targetRow == Integer.MAX_VALUE)
				targetRow = row;
			scrollToSelection(targetRow);
			aTable.setRowSelectionInterval(targetRow, targetRow);
			showTooltip(targetRow);
		}

		private void gotoPreviousLine() {
			int row = aTable.getSelectedRow();
			if (row == -1) {
				return;
			}
			int targetRow = linesStorage.findPrevLine(row);
			if (targetRow == -1)
				targetRow = row;
			scrollToSelection(targetRow);
			aTable.setRowSelectionInterval(targetRow, targetRow);
			showTooltip(targetRow);
		}

		private void showTooltip(int targetRow) {

			Rectangle rect = aTable.getCellRect(targetRow, 1, true);
			int tableWidth = aTable.getVisibleRect().width;
			moveMouse(rect, tableWidth);
			Component cell = aTable.getCellRenderer(targetRow, 1).
					getTableCellRendererComponent(aTable, true, false, false, targetRow, 1);
			aTable.setToolTipText(((ColorAndTooltipCellRenderer) cell).getToolTipText());
			ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(aTable, 0, 0, 0,
					rect.x + tableWidth / 2, rect.y, // X-Y of the mouse for the tool tip
					0, false));
		}

		private void moveMouse(Rectangle rect, int tableWidth) {
			try {
				Robot robot = new Robot();
				Point tableLocation = aTable.getLocationOnScreen();
				robot.mouseMove(tableLocation.x + tableWidth, tableLocation.y + rect.y);
			} catch (AWTException e) {
				/* do nothing */
			}
		}
	}

	/**
	 * We set a tooltip to aTable in UpAndDownJumper, so have to clean it
	 * when we move mouse in a clean area, so that a tooltip from
	 * UpAndDownJumper would not appear.
	 */
	private class TooltipCleaner extends MouseMotionAdapter {
		@Override
		public void mouseMoved(MouseEvent e) {
			Point point = e.getPoint();
			int row = aTable.rowAtPoint(point);
			int col = aTable.columnAtPoint(point);
			String tooltip = ((ColorAndTooltipCellRenderer) aTable.getCellRenderer(row, col)).getToolTipText();
			if (tooltip == null || tooltip.isEmpty()) {
				aTable.setToolTipText(null);
			}
		}
	}

	public class TableFormPanel extends JPanel {

		private final TableForm tableForm;

		public TableFormPanel(TableForm tableForm) {
			this.tableForm = tableForm;
		}

		public TableForm getTableForm() {
			return tableForm;
		}
	}

	private class TableWithSecondRowUnselectable extends JTable {
		@Override
		public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
			if (columnIndex == 1) {
				columnIndex = 0;
			}
			super.changeSelection(rowIndex, columnIndex, toggle, extend);
		}
	}
}
