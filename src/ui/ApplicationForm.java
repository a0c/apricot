package ui;

import base.helpers.ExceptionSolver;
import io.ConsoleWriter;
import ui.BusinessLogic.ParserID;
import ui.buttonPanels.BehButtons;
import ui.buttonPanels.BehDDButtons;
import ui.buttonPanels.PSLButtons;
import ui.buttonPanels.RTLButtons;
import ui.fileViewer.*;
import ui.optionPanels.HLDDBehOptionsPanel;
import ui.optionPanels.PSLOptionsPanel;
import ui.optionPanels.VHDLBehDdOptionsPanel;
import ui.optionPanels.VHDLBehOptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ui.FileDependencyResolver.*;

/**
 * @author Anton Chepurov
 */
public class ApplicationForm implements ActionListener {

	public static final String LIB_DIR = "." + File.separator + "lib" + File.separator;

	private JComboBox parserComboBox;
	private JButton parseButton;
	private JPanel optionsPanel;
	private JTabbedPane tabbedPane;
	private JCheckBox checkAssertionCheckBox;
	private JSpinner drawPatternCountSpinner;
	private JButton drawButton;
	private JButton chkFileButton;
	private JFormattedTextField chkFileTextField;
	private JSpinner patternNrSpinnerAssert;
	private JButton hlddAssertButton;
	private JTextField hlddAssertTextField;
	private JButton tgmButton;
	private JButton checkButton;
	private JButton covHlddButton;
	private JTextField covHlddTextField;
	private JButton covSimulButton;
	private JSpinner covPatternNrSpinner;
	private JPanel mainPanel;
	private JTextArea consoleTextArea;
	private JTabbedPane fileViewerTabbedPane1;
	private JPanel clickMePanel1;
	private JScrollPane infoScrollPane;
	private JPanel consolePanel;
	private JRadioButton randomAssertRadioButton;
	private JRadioButton tstAssertRadioButton;
	private JRadioButton covTstRButton;
	private JRadioButton covRandomRButton;
	private JButton covVhdlButton;
	private JTextField covVhdlTextField;
	private JButton covButton;
	private JTextField covTextField;
	private JButton covHighlightButton;
	private JTextField tgmTextField;
	private JCheckBox coverageCheckBox;
	private JTabbedPane pictureTabPane;
	private JTabbedPane upperRightTabbedPane;
	private JTabbedPane fileViewerTabbedPane2;
	private JPanel clickMePanel2;
	private JSplitPane fileViewerSplitPane;
	private JCheckBox nodeCheckBox;
	private JCheckBox edgeCheckBox;
	private JCheckBox toggleCheckBox;
	private JCheckBox conditionCheckBox;
	private JPanel buttonsPanel;
	private JCheckBox commentCheckBox;
	private JTextField diagHlddTextField;
	private JButton diagSimulButton;
	private JSpinner diagPatternNrSpinner;
	private JRadioButton diagRandomRButton;
	private JRadioButton diagTstRButton;
	private JCheckBox diagnoseCheckBox;
	private JCheckBox optimizeCheckBox;
	private JCheckBox potentialCheckBox;
	private JCheckBox scorebyratioCheckBox;
	private JCheckBox scorebyfailedCheckBox;
	private JButton diagHlddButton;
	private JButton diagVhdlButton;
	private JButton dgnButton;
	private JTextField diagVhdlTextField;
	private JTextField dgnTextField;
	private JButton diagHighlightButton;
	private JToggleButton revealMutationButton;
	private MouseSelectionAdapter upperRightTabbedPaneAdapter;
	private MouseSelectionAdapter picturePaneAdapter;

	private FileDropHandler fileDropHandler;

	private JButton ppgLibButton;

	private final Map<JButton, JTextField> textFieldByButton = new HashMap<JButton, JTextField>();

	private static JFrame frame;

	private BusinessLogic businessLogic = null;
	private ParserID selectedParserId = null;
	private VHDLBehOptionsPanel vhdlBehOptionsPanel = null;
	private VHDLBehDdOptionsPanel vhdlBehDdOptionsPanel = null;
	private HLDDBehOptionsPanel hlddBehOptionsPanel = null;
	private PSLOptionsPanel pslOptionsPanel = null;

	private BehButtons behButtons = null;
	private BehDDButtons behDDButtons = null;
	private RTLButtons rtlButtons = null;
	private PSLButtons pslButtons = null;

	private BusinessLogicAssertionChecker businessLogicAssertionChecker = null;
	private BusinessLogicCoverageAnalyzer businessLogicCoverageAnalyzer = null;
	private Highlighter highlighter;
	private Diagnosis diagnosis;

	private TabbedPaneListener tabbedPaneListener;
	private TabbedPaneListener tabbedPaneListener2;
	private JButton behVhdlBtn;
	private JButton behHlddBtn;
	private JTextField behVhdlTextField;
	private JTextField behHlddTextField;
	private JButton behDDVhdlBtn;
	private JButton behDDHlddBtn;
	private JTextField behDDVhdlTextField;
	private JTextField behDDHlddTextField;
	private JButton rtlBehBtn;
	private JButton rtlRtlBtn;
	private JTextField rtlBehTextField;
	private JTextField rtlRtlTextField;
	private JButton baseModelBtn;
	private JButton pslBtn;
	private JTextField baseModelTextField;
	private JTextField pslTextField;

	public ApplicationForm() {

		fileDropHandler = new FileDropHandler(this);
		addKeyListener(tabbedPane, parserComboBox, commentCheckBox, parseButton, upperRightTabbedPane,
				fileViewerTabbedPane1, fileViewerTabbedPane2, hlddAssertButton, tstAssertRadioButton,
				randomAssertRadioButton, patternNrSpinnerAssert, tgmButton, checkButton, checkAssertionCheckBox,
				chkFileButton, drawButton, drawPatternCountSpinner, covHlddButton, covSimulButton, covTstRButton,
				covRandomRButton, coverageCheckBox, nodeCheckBox, toggleCheckBox, conditionCheckBox,
				edgeCheckBox, covVhdlButton, covButton, covHighlightButton,
				diagHlddTextField, diagSimulButton, diagPatternNrSpinner, diagRandomRButton, diagTstRButton,
				diagnoseCheckBox, optimizeCheckBox, potentialCheckBox, scorebyratioCheckBox,
				scorebyfailedCheckBox, diagHlddButton, diagVhdlButton, dgnButton,
				diagVhdlTextField, dgnTextField, diagHighlightButton, revealMutationButton);

		/* ConsoleWriter to write into a consoleTextArea */
		ConsoleWriter consoleWriter = new ConsoleWriter(consoleTextArea, false);
		infoScrollPane.getVerticalScrollBar().addAdjustmentListener((AdjustmentListener) consolePanel);
		consoleTextArea.addMouseListener(((ConsolePanel) consolePanel).getConsoleMouseAdapter());

		fileViewerSplitPane.setDividerLocation(700);
		/* Create PARSERS options panels */
		updateParserId();
		OutputFileGenerator outputFileGenerator = new OutputFileGenerator(this);
		vhdlBehOptionsPanel = new VHDLBehOptionsPanel(outputFileGenerator, fileDropHandler);
		vhdlBehDdOptionsPanel = new VHDLBehDdOptionsPanel(fileDropHandler);
		hlddBehOptionsPanel = new HLDDBehOptionsPanel();
		pslOptionsPanel = new PSLOptionsPanel(fileDropHandler);

		behButtons = new BehButtons(fileDropHandler);
		behDDButtons = new BehDDButtons(fileDropHandler);
		rtlButtons = new RTLButtons(fileDropHandler);
		pslButtons = new PSLButtons(fileDropHandler);

		ppgLibButton = pslOptionsPanel.getPpgLibButton();
		behVhdlBtn = behButtons.getVhdlButton();
		behVhdlTextField = behButtons.getVhdlTextField();
		behHlddBtn = behButtons.getHlddButton();
		behHlddTextField = behButtons.getHlddTextField();
		behDDVhdlBtn = behDDButtons.getVhdlButton();
		behDDVhdlTextField = behDDButtons.getVhdlTextField();
		behDDHlddBtn = behDDButtons.getHlddButton();
		behDDHlddTextField = behDDButtons.getHlddTextField();
		rtlBehBtn = rtlButtons.getBehButton();
		rtlBehTextField = rtlButtons.getBehTextField();
		rtlRtlBtn = rtlButtons.getRtlButton();
		rtlRtlTextField = rtlButtons.getRtlTextField();
		baseModelBtn = pslButtons.getBaseModelButton();
		baseModelTextField = pslButtons.getBaseModelTextField();
		pslBtn = pslButtons.getPslButton();
		pslTextField = pslButtons.getPslTextField();

		addActionListener(behVhdlBtn, behHlddBtn, behDDVhdlBtn, behDDHlddBtn, rtlBehBtn, rtlRtlBtn, baseModelBtn, pslBtn,
				parseButton, parserComboBox, ppgLibButton);

		/* PARSERS */
		businessLogic = new BusinessLogic(this, consoleWriter);
		behButtons.addFileGenerator(outputFileGenerator);
		rtlButtons.addFileGenerator(new RTLOutputFileGenerator(this));
		//todo: FileGenerator for pslButtons. or like in setCOVFile() ?...
		/* ASSERTION CHECKER */
		businessLogicAssertionChecker = new BusinessLogicAssertionChecker(this, consoleWriter);
		addActionListener(hlddAssertButton, tgmButton, checkButton, chkFileButton, drawButton);
		checkAssertionCheckBox.addChangeListener(createAssertionButtonChanger());
		/* COVERAGE ANALYSIS */
		businessLogicCoverageAnalyzer = new BusinessLogicCoverageAnalyzer(this, consoleWriter);
		addActionListener(covHlddButton, covSimulButton);
		coverageCheckBox.addChangeListener(createCoverageButtonChanger());
		/* DIAGNOSIS */
		diagnosis = new Diagnosis(this, consoleWriter);
		addActionListener(diagHlddButton, diagSimulButton, revealMutationButton);
		revealMutationButton.addChangeListener(new TableFormFocuser());
		diagnoseCheckBox.addChangeListener(createDiagnoseButtonChanger());
		ScoreToggler scoreToggler = new ScoreToggler();
		scorebyfailedCheckBox.addChangeListener(scoreToggler);
		scorebyratioCheckBox.addChangeListener(scoreToggler);
		/* HIGHLIGHTER */
		highlighter = new Highlighter(this, consoleWriter);
		addActionListener(covVhdlButton, covButton, covHighlightButton, diagVhdlButton, dgnButton, diagHighlightButton);

		/* Add Mouse Listener to the File Viewer Tabbed Pane */
		tabbedPaneListener = new TabbedPaneListener(this, fileViewerTabbedPane1, clickMePanel1, fileViewerTabbedPane2);
		tabbedPaneListener2 = new TabbedPaneListener(this, fileViewerTabbedPane2, clickMePanel2, fileViewerTabbedPane1);
		clickMePanel1.addMouseListener(tabbedPaneListener);
		clickMePanel2.addMouseListener(tabbedPaneListener2);
		upperRightTabbedPaneAdapter = new MouseSelectionAdapter(upperRightTabbedPane);
		picturePaneAdapter = new MouseSelectionAdapter(pictureTabPane);

		fileViewerTabbedPane1.addKeyListener(new TabMover(fileViewerTabbedPane1));
		fileViewerTabbedPane2.addKeyListener(new TabMover(fileViewerTabbedPane2));
		fileViewerTabbedPane1.addChangeListener(new TableFormFocuser());
		fileViewerTabbedPane2.addChangeListener(new TableFormFocuser());

		/* Add empty Mouse Listener to the Glass Pane, to disable user input while SwingWorker is working */
		Component glassPane = frame.getGlassPane();
		glassPane.addMouseListener(new MouseAdapter() {
		});

		updateConverterUI();
		ExceptionSolver.getInstance().setFrame(frame);
		/* Create Button-to-TextField mapping */
		mapTextFieldsToButtons();
		randomAssertRadioButton.addChangeListener(
				new RadioButtonToSpinnerLinker(randomAssertRadioButton, patternNrSpinnerAssert));
		covRandomRButton.addChangeListener(
				new RadioButtonToSpinnerLinker(covRandomRButton, covPatternNrSpinner));

		CoverageCheckBoxSetter checkBoxSetter = new CoverageCheckBoxSetter();
		coverageCheckBox.addActionListener(checkBoxSetter);
		edgeCheckBox.addActionListener(checkBoxSetter);
		conditionCheckBox.addActionListener(checkBoxSetter);
		nodeCheckBox.addActionListener(checkBoxSetter);
		toggleCheckBox.addActionListener(checkBoxSetter);
	}

	private ChangeListener createDiagnoseButtonChanger() {
		return new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (diagnoseCheckBox.isSelected()) {
					diagSimulButton.setText("Diagnose");
					diagSimulButton.setMnemonic(KeyEvent.VK_D);
				} else {
					diagSimulButton.setText("Simulate");
					diagSimulButton.setMnemonic(KeyEvent.VK_S);
				}
				diagSimulButton.repaint();
			}
		};
	}

	private ChangeListener createCoverageButtonChanger() {
		return new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (coverageCheckBox.isSelected()) {
					covSimulButton.setText("Measure coverage");
					covSimulButton.setMnemonic(KeyEvent.VK_M);
				} else {
					covSimulButton.setText("Simulate");
					covSimulButton.setMnemonic(KeyEvent.VK_S);
				}
				covSimulButton.repaint();
			}
		};
	}

	private ChangeListener createAssertionButtonChanger() {
		return new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (checkAssertionCheckBox.isSelected()) {
					checkButton.setText("Check assertions");
					checkButton.setMnemonic(KeyEvent.VK_C);
				} else {
					checkButton.setText("Simulate");
					checkButton.setMnemonic(KeyEvent.VK_S);
				}
				checkButton.repaint();
			}
		};
	}

	private void addKeyListener(Component... components) {
		for (Component component : components) {
			component.addKeyListener(fileDropHandler);
		}
	}

	private void mapTextFieldsToButtons() {

		ButtonAndTextField[] store = new ButtonAndTextField[]{
				new ButtonAndTextField(behVhdlBtn, behVhdlTextField),
				new ButtonAndTextField(behHlddBtn, behHlddTextField),
				new ButtonAndTextField(behDDVhdlBtn, behDDVhdlTextField),
				new ButtonAndTextField(behDDHlddBtn, behDDHlddTextField),
				new ButtonAndTextField(rtlBehBtn, rtlBehTextField),
				new ButtonAndTextField(rtlRtlBtn, rtlRtlTextField),
				new ButtonAndTextField(baseModelBtn, baseModelTextField),
				new ButtonAndTextField(pslBtn, pslTextField),
				new ButtonAndTextField(ppgLibButton, pslOptionsPanel.getPpgLibTextField()),
				new ButtonAndTextField(hlddAssertButton, hlddAssertTextField),
				new ButtonAndTextField(tgmButton, tgmTextField),
				new ButtonAndTextField(chkFileButton, chkFileTextField),
				new ButtonAndTextField(covHlddButton, covHlddTextField),
				new ButtonAndTextField(covVhdlButton, covVhdlTextField),
				new ButtonAndTextField(covButton, covTextField),
				new ButtonAndTextField(diagHlddButton, diagHlddTextField),
				new ButtonAndTextField(diagVhdlButton, diagVhdlTextField),
				new ButtonAndTextField(dgnButton, dgnTextField)
		};

		for (ButtonAndTextField holder : store) {

			if (holder.button == null || holder.textField == null) {
				continue;
			}

			textFieldByButton.put(holder.button, holder.textField);

			holder.textField.addMouseListener(new FileOpener(holder.textField));
		}
	}

	private void addActionListener(JComponent... components) {
		for (JComponent component : components) {
			if (component instanceof AbstractButton) {
				((AbstractButton) component).addActionListener(this);
			} else if (component instanceof JComboBox) {
				((JComboBox) component).addActionListener(this);
			}
		}
	}

	private void showSelectFileDialog(JButton sourceButton) {

		String[] extensions;
		String dialogTitle;
		String invalidFileMessage;
		String proposedFileName = null;
		if (sourceButton == behVhdlBtn) {
			extensions = new String[]{"vhdl", "vhd"};
			dialogTitle = "Select source VHDL Behavioural file";
			invalidFileMessage = "Selected file is not a VHDL file!";
		} else if (sourceButton == behDDVhdlBtn) {
			extensions = new String[]{"vhdl", "vhd"};
			dialogTitle = "Select source HIF file"; //todo: VHDL Behavioural DD
			invalidFileMessage = "Selected file is not a HIF file!"; //todo: VHDL
		} else if (sourceButton == behHlddBtn || sourceButton == behDDHlddBtn) {
			proposedFileName = businessLogic.getProposedFileName();
			invalidFileMessage = "Selected file is not an HLDD file!";
			extensions = new String[]{"agm"};
			dialogTitle = "Select output HLDD Behavioural file";
		} else if (sourceButton == rtlBehBtn) {
			extensions = new String[]{"agm"};
			dialogTitle = "Select source HLDD Behavioural file";
			invalidFileMessage = "Selected file is not an HLDD file!";
		} else if (sourceButton == rtlRtlBtn) {
			proposedFileName = businessLogic.getProposedFileName();
			invalidFileMessage = "Selected file is not an HLDD file!";
			extensions = new String[]{"agm"};
			dialogTitle = "Select output HLDD RTL file";
		} else if (sourceButton == baseModelBtn) {
			extensions = new String[]{"agm"};
			dialogTitle = "Select Base HLDD model file";
			invalidFileMessage = "Selected file is not an HLDD file!";
		} else if (sourceButton == pslBtn) {
			proposedFileName = businessLogic.getProposedFileName();
			extensions = new String[]{"psl"};
			dialogTitle = "Select source PSL file";
			invalidFileMessage = "Selected file is not a PSL file!";
		} else if (sourceButton == ppgLibButton) {
			extensions = new String[]{"lib"};
			dialogTitle = "Select PPG Library file";
			invalidFileMessage = "Selected file is not a PPG Library file!";
		} else if (sourceButton == chkFileButton) {
			extensions = new String[]{"chk"};
			dialogTitle = "Select Simulation file to draw";
			invalidFileMessage = "Selected file is not a Simulation file!";
		} else if (sourceButton == hlddAssertButton) {
			proposedFileName = businessLogicAssertionChecker.getProposedFileName();
			extensions = new String[]{"agm"};
			dialogTitle = "Select HLDD model file to check assertions for";
			invalidFileMessage = "Selected file is not an HLDD file!";
		} else if (sourceButton == tgmButton) {
			String hlddFilePath = businessLogicAssertionChecker.getProposedFileName();
			if (hlddFilePath != null) {
				extensions = new String[]{new File(hlddFilePath).getName().replace(".agm", ".tgm")};
			} else {
				extensions = new String[]{"tgm"};
			}
			dialogTitle = "Select TGM file with assertions to check";
			invalidFileMessage = "Selected file is not a TGM file!";
		} else if (sourceButton == covHlddButton) {
			extensions = new String[]{"agm"};
			dialogTitle = "Select HLDD model file to analyze";
			invalidFileMessage = "Selected file is not an HLDD file!";
		} else if (sourceButton == covVhdlButton || sourceButton == diagVhdlButton) {
			extensions = new String[]{"vhdl", "vhd"};
			dialogTitle = "Select source VHDL Behavioural file";
			invalidFileMessage = "Selected file is not a VHDL file!";
		} else if (sourceButton == covButton) {
			extensions = new String[]{"cov"};
			dialogTitle = "Select Coverage file";
			invalidFileMessage = "Selected file is not a Coverage file!";
		} else if (sourceButton == diagHlddButton) {
			extensions = new String[]{"agm"};
			dialogTitle = "Select HLDD model file to diagnose";
			invalidFileMessage = "Selected file is not an HLDD file!";
		} else if (sourceButton == dgnButton) {
			extensions = new String[]{"dgn"};
			dialogTitle = "Select Diagnosis file";
			invalidFileMessage = "Selected file is not a Diagnosis file!";
		} else return;

		SingleFileSelector selector = SingleFileSelector.getInstance(SingleFileSelector.DialogType.OPEN,
				extensions, proposedFileName, dialogTitle, invalidFileMessage, getFrame());
		if (selector.isFileSelected()) {
			try {
				/* Check the input for VALIDITY */
				selector.validateFile();
				File selectedFile = selector.getRestrictedSelectedFile();
				if (sourceButton == ppgLibButton) {

					setPPGLibFile(selectedFile);

				} else if (sourceButton == hlddAssertButton) {

					setAssertHlddFile(selectedFile);

				} else if (sourceButton == tgmButton) {

					setTgmFile(selectedFile);

				} else if (sourceButton == chkFileButton) {

					setChkFile(selectedFile);

				} else if (sourceButton == covHlddButton) {

					setCovHlddFile(selectedFile);

				} else if (sourceButton == covVhdlButton) {

					setCovVhdlFile(selectedFile);

				} else if (sourceButton == covButton) {

					setCovFile(selectedFile);

				} else if (sourceButton == behVhdlBtn) {

					setBehVhdlFile(selectedFile);

				} else if (sourceButton == behDDVhdlBtn) {

					setBehDDVhdlFile(selectedFile);

				} else if (sourceButton == rtlBehBtn) {

					setRtlBehFile(selectedFile);

				} else if (sourceButton == baseModelBtn) {

					setBaseModelFile(selectedFile);

				} else if (sourceButton == behHlddBtn) {

					setBehHlddFile(selectedFile);

				} else if (sourceButton == behDDHlddBtn) {

					setBehDDHlddFile(selectedFile);

				} else if (sourceButton == rtlRtlBtn) {

					setRtlRtlFile(selectedFile);

				} else if (sourceButton == pslBtn) {

					setPslFile(selectedFile);

				} else if (sourceButton == dgnButton) {

					setDgnFile(selectedFile);

				} else if (sourceButton == diagVhdlButton) {

					setDiagVhdlFile(selectedFile);

				} else if (sourceButton == diagHlddButton) {

					setDiagHlddFile(selectedFile);

				}

			} catch (ExtendedException e) {
				showErrorMessage(e);
			}
		}

	}

	private void setDiagHlddFile(File hlddFile) {

		diagnosis.setHlddFile(hlddFile);
		updateTextFieldFor(diagHlddButton, hlddFile);

		/* Automatically look for identical Patterns file */
		selectIdenticalTSTFile(deriveTstFile(hlddFile),
				diagTstRButton, diagRandomRButton, diagPatternNrSpinner);

		SingleFileSelector.setCurrentDirectory(hlddFile);
	}

	private void setDiagVhdlFile(File vhdlFile) {

		updateTextFieldFor(diagVhdlButton, vhdlFile);

		setCovVhdlFile(vhdlFile);

		SingleFileSelector.setCurrentDirectory(vhdlFile);
	}

	private void setChkFile(File chkFile) {

		businessLogicAssertionChecker.setSimulationFile(chkFile);
		updateTextFieldFor(chkFileButton, chkFile);

		businessLogicAssertionChecker.loadChkFile();
	}

	private void setCovHlddFile(File hlddFile) {

		businessLogicCoverageAnalyzer.setHlddFile(hlddFile);
		updateTextFieldFor(covHlddButton, hlddFile);

		/* Automatically look for identical Patterns file */
		selectIdenticalTSTFile(deriveTstFile(hlddFile),
				covTstRButton, covRandomRButton, covPatternNrSpinner);

		SingleFileSelector.setCurrentDirectory(hlddFile);
	}

	private void setAssertHlddFile(File hlddFile) {

		businessLogicAssertionChecker.setHlddFile(hlddFile);
		updateTextFieldFor(hlddAssertButton, hlddFile);

		/* Automatically look for identical TGM file */
		File tgmFile = deriveTgmFile(hlddFile);
		if (tgmFile != null) {
			setTgmFile(tgmFile);
		}

		/* Automatically look for identical Patterns file */
		selectIdenticalTSTFile(deriveTstFile(hlddFile),
				tstAssertRadioButton, randomAssertRadioButton, patternNrSpinnerAssert);

		SingleFileSelector.setCurrentDirectory(hlddFile);
	}

	private void setTgmFile(File tgmFile) {

		businessLogicAssertionChecker.setTgmFile(tgmFile);

		updateTextFieldFor(tgmButton, tgmFile);

		checkAssertionCheckBox.setSelected(true);
	}

	private void setPslFile(File pslFile) {

		businessLogic.setPslFile(pslFile);
		updateTextFieldFor(pslBtn, pslFile);

		/* Automatically look for identical PSL Base Model file */
		File baseModelFile = deriveBaseModelFile(pslFile);
		if (baseModelFile != null) {
			setBaseModelFile(baseModelFile);
		}

		//todo: move this to loadFile()?
		SingleFileSelector.setCurrentDirectory(pslFile);
	}

	private void setBaseModelFile(File baseModelFile) {

		businessLogic.setBaseModelFile(baseModelFile);

		updateTextFieldFor(baseModelBtn, baseModelFile);

		SingleFileSelector.setCurrentDirectory(baseModelFile);
	}

	void setRtlRtlFile(File rtlRtlFile) {

		businessLogic.setRtlRtlFile(rtlRtlFile);

		updateTextFieldFor(rtlRtlBtn, rtlRtlFile);

	}

	private void setRtlBehFile(File behHlddFile) {

		businessLogic.setRtlBehFile(behHlddFile);

		updateTextFieldFor(rtlBehBtn, behHlddFile);

		SingleFileSelector.setCurrentDirectory(behHlddFile);

	}

	private void setBehDDHlddFile(File hlddFile) {

		businessLogic.setBehDDHlddFile(hlddFile);

		updateTextFieldFor(behDDHlddBtn, hlddFile);

		SingleFileSelector.setCurrentDirectory(hlddFile);
	}

	void setBehHlddFile(File hlddFile) {

		businessLogic.setBehHlddFile(hlddFile);

		updateTextFieldFor(behHlddBtn, hlddFile);

		SingleFileSelector.setCurrentDirectory(hlddFile);
	}

	private void setBehDDVhdlFile(File vhdlFile) {

		businessLogic.setBehDDVhdlFile(vhdlFile);

		updateTextFieldFor(behDDVhdlBtn, vhdlFile);

		SingleFileSelector.setCurrentDirectory(vhdlFile);

		/* clear HLDD file */
		setBehDDHlddFile(null);
	}

	private void setBehVhdlFile(File vhdlFile) {

		businessLogic.setBehVhdlFile(vhdlFile);

		updateTextFieldFor(behVhdlBtn, vhdlFile);

		SingleFileSelector.setCurrentDirectory(vhdlFile);

		/* clear HLDD file */
		setBehHlddFile(null);
		/* cover situation when user first selects SmartNames and only then VHDL file */
		behButtons.triggerSmartNames();
	}

	private void setPPGLibFile(File ppgLibFile) {

		businessLogic.setPpgLibFile(ppgLibFile);

		updateTextFieldFor(ppgLibButton, ppgLibFile);

		SingleFileSelector.setCurrentDirectory(ppgLibFile);
	}

	public void setDgnFile(File dgnFile) {
		setDgnFile(dgnFile, true);
	}

	public void setDgnFile(File dgnFile, boolean doClick) {

		if (doClick) {
			File covFile = deriveCovFile(dgnFile);
			setCovFile(covFile, false);
		}

		highlighter.setDgnFile(dgnFile);
		updateDgnTextField(dgnFile);

		if (dgnFile == null) {
			return;
		}
		/* Auto load VHDL file and show candidates */
		File vhdlFile = deriveVhdlFile(deriveHlddFile(dgnFile));

		if (vhdlFile != null) {
			setDiagVhdlFile(vhdlFile);
		}

		/* Automatically click HIGHLIGHT button, if both files are set */
		if (doClick) {
			if (vhdlFile != null) {
				doClickHighlightButton();
			}
		}

		SingleFileSelector.setCurrentDirectory(dgnFile);

	}

	public void setCovFile(File covFile) {
		setCovFile(covFile, true);
	}

	public void setCovFile(File covFile, boolean doClick) {

		if (doClick) {
			File dgnFile = deriveDgnFile(covFile);
			setDgnFile(dgnFile, false);
		}

		highlighter.setCovFile(covFile);
		updateCovTextField(covFile);

		if (covFile == null) {
			return;
		}
		/* Auto load VHDL file and show coverage */
		File vhdlFile = deriveVhdlFile(deriveHlddFile(covFile));

		if (vhdlFile != null) {
			setCovVhdlFile(vhdlFile);
		}

		/* Automatically click HIGHLIGHT button, if both files are set */
		if (doClick) {
			if (vhdlFile != null) {
				doClickHighlightButton();
			}
		}

		SingleFileSelector.setCurrentDirectory(covFile);
	}

	private void setCovVhdlFile(File vhdlFile) {

		highlighter.setVhdlFile(vhdlFile);

		updateCovVhdlTextField(vhdlFile);

		SingleFileSelector.setCurrentDirectory(vhdlFile);
	}

	private void selectIdenticalTSTFile(File tstFile, JRadioButton tstRadioButton, JRadioButton randomRadioButton,
										JSpinner patternNrSpinner) {
		if (tstFile != null) {
			tstRadioButton.setEnabled(true);
			tstRadioButton.setSelected(true);
			tstRadioButton.setToolTipText(tstFile.getAbsolutePath());
			patternNrSpinner.setEnabled(false);
		} else {
			tstRadioButton.setEnabled(false);
			tstRadioButton.setToolTipText(null);
			randomRadioButton.setSelected(true);
		}
	}

	public void showErrorMessage(ExtendedException e) {
		JOptionPane.showMessageDialog(frame, e.getMessage(), e.getTitle(), JOptionPane.ERROR_MESSAGE);
	}


	private void updateConverterUI() {
		/* Update OPTIONS */
		updateOptions();
		/* Update BUTTONS */
		updateButtons();
	}

	private void updateButtons() {

		buttonsPanel.removeAll();

		switch (selectedParserId) {
			case VhdlBeh2HlddBeh:
				buttonsPanel.add(behButtons.getMainPanel());
				break;
			case VhdlBehDd2HlddBeh:
				buttonsPanel.add(behDDButtons.getMainPanel());
				break;
			case HlddBeh2HlddRtl:
				buttonsPanel.add(rtlButtons.getMainPanel());
				break;
			case PSL2THLDD:
				buttonsPanel.add(pslButtons.getMainPanel());
				break;
		}
	}

	private void updateOptions() {

		optionsPanel.removeAll();

		switch (selectedParserId) {
			case VhdlBeh2HlddBeh:
				/* VHDL Beh <=> HLDD Beh */
				optionsPanel.add(vhdlBehOptionsPanel.getMainPanel());
				break;
			case VhdlBehDd2HlddBeh:
				/* VHDL Beh DD <=> HLDD Beh */
				optionsPanel.add(vhdlBehDdOptionsPanel.getMainPanel());
				break;
			case HlddBeh2HlddRtl:
				/* HLDD Beh <=> HLDD RTL */
				optionsPanel.add(hlddBehOptionsPanel.getMainPanel(), 0);
				break;
			default:
				/* PSL <=> THLDD */
				optionsPanel.add(pslOptionsPanel.getMainPanel());
		}

		frame.validate();
		frame.repaint();
	}

	public ParserID getSelectedParserId() {
		return selectedParserId;
	}

	public boolean isRandomAssert() {
		return randomAssertRadioButton.isSelected();
	}

	public boolean isRandomCov() {
		return covRandomRButton.isSelected();
	}

	public boolean isRandomDiag() {
		return diagRandomRButton.isSelected();
	}

	public boolean shouldFlattenCS() {
		switch (selectedParserId) {
			case VhdlBeh2HlddBeh:
				return vhdlBehOptionsPanel.shouldFlattenCS();
			case VhdlBehDd2HlddBeh:
				return vhdlBehDdOptionsPanel.shouldExpandCS();
			default:
				return false;
		}
	}

	public boolean shouldCreateCSGraphs() {
		switch (selectedParserId) {
			case VhdlBeh2HlddBeh:
				return vhdlBehOptionsPanel.shouldCreateCSGraphs();
			case VhdlBehDd2HlddBeh:
				return false;//todo...
			default:
				return false;
		}
	}

	public boolean shouldCreateExtraCSGraphs() {
		switch (selectedParserId) {
			case VhdlBeh2HlddBeh:
				return vhdlBehOptionsPanel.shouldCreateExtraCSGraphs();
			case VhdlBehDd2HlddBeh:
				return false;//todo...
			default:
				return false;
		}
	}

	public boolean shouldSimplify() {
		return vhdlBehDdOptionsPanel.shouldSimplify();
	}

	public boolean areSmartNamesAllowed() {
		return behButtons.areSmartNamesAllowed();
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if (System.getProperty("os.name").toUpperCase().startsWith("WIN")) {
			}
		} catch (ClassNotFoundException e) {
			/* Ignore exception because we can't do anything. Will use default. */
		} catch (InstantiationException e) {
			/* Ignore exception because we can't do anything. Will use default. */
		} catch (IllegalAccessException e) {
			/* Ignore exception because we can't do anything. Will use default. */
		} catch (UnsupportedLookAndFeelException e) {
			/* Ignore exception because we can't do anything. Will use default. */
		}

		frame = new JFrame("Apricot CAD"); // HLDD Tools
		ApplicationForm applicationForm = new ApplicationForm();
		frame.setTransferHandler(applicationForm.fileDropHandler);
		frame.setContentPane(applicationForm.getMainPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addComponentListener(new MainWindowResizer());
		IconAdder.setFrameIcon(frame);
		frame.pack();
		frame.setVisible(true);
		UniversalFrameManipulator.maximize(frame);

		ToolTipManager.sharedInstance().setDismissDelay(15000);

		/* Process exceptions of all SwingWorkers */ // http://book.javanb.com/java-threads-3rd/jthreads3-CHP-13-SECT-5.html
		Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(applicationForm));

	}

	private Container getMainPanel() {
		return mainPanel;
	}

	private void updateTextFieldFor(JButton parentButton, File file) {
		JTextField textFieldToUpdate = textFieldByButton.get(parentButton);
		if (textFieldToUpdate != null) {
			String text;
			String tooltip;
			Color color = Color.BLACK;
			if (file == null) {
				text = "";
				tooltip = null;
			} else {
				text = file.getName();
				tooltip = file.getAbsolutePath();
				if (file.exists()) {
					if (textFieldToUpdate == behHlddTextField || textFieldToUpdate == behDDHlddTextField
							|| textFieldToUpdate == rtlRtlTextField) {
						color = Color.RED;
					}
				}
			}
			textFieldToUpdate.setText(text);
			textFieldToUpdate.setToolTipText(tooltip);
			textFieldToUpdate.setForeground(color);
		}
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		try {
			if (source instanceof JButton && (source == behVhdlBtn || source == behHlddBtn
					|| source == behDDVhdlBtn || source == behDDHlddBtn
					|| source == rtlBehBtn || source == rtlRtlBtn
					|| source == baseModelBtn || source == pslBtn
					|| source == ppgLibButton
					|| source == chkFileButton || source == hlddAssertButton || source == tgmButton
					|| source == covHlddButton || source == covVhdlButton || source == covButton
					|| source == diagHlddButton || source == diagVhdlButton || source == dgnButton)) {

				showSelectFileDialog(((JButton) source));

			} else if (source == parseButton) {

				/* Reset previous logic */
				businessLogic.reset();
				/* Parse and convert file */
				businessLogic.processParse();

			} else if (source == parserComboBox) {
				updateParserId();
				updateConverterUI();
			} else if (source == checkButton) {
				businessLogicAssertionChecker.processCheck();
			} else if (source == drawButton) {
				businessLogicAssertionChecker.processDraw();
			} else if (source == covSimulButton) {
				businessLogicCoverageAnalyzer.processAnalyze();
			} else if (source == covHighlightButton) {
				highlighter.highlight();
			} else if (source == diagSimulButton) {
				diagnosis.diagnose();
			} else if (source == diagHighlightButton) {
				highlighter.highlight();
			} else if (source == revealMutationButton) {
				toggleMutations();
			}
		} catch (ExtendedException e1) {
			JOptionPane.showMessageDialog(frame, e1.getMessage(), e1.getTitle(), JOptionPane.WARNING_MESSAGE);
		}
	}

	private void toggleMutations() {

		boolean isOn = revealMutationButton.isSelected();

		revealMutationButton.setText(isOn ? "Hide mutation" : "Reveal mutation");

		toggleMutationsInTabbedPane(fileViewerTabbedPane1, isOn);
		toggleMutationsInTabbedPane(fileViewerTabbedPane2, isOn);

	}

	private void toggleMutationsInTabbedPane(JTabbedPane tabbedPane, boolean isOn) {

		Collection<TableForm> tableForms = new TableFormFinder(tabbedPane).find();
		for (TableForm tableForm : tableForms) {

			boolean isShowing = tableForm.setShowingMutation(isOn);

			TabComponent tabComponent = tableForm.getTabComponent();

			tabComponent.markMutated(isShowing);

			if (isShowing) {
				selectTab(tabComponent, tabbedPane);
			}
		}
	}

	private void selectTab(TabComponent tabComponent, JTabbedPane tabbedPane) {
		for (int i = 0, n = tabbedPane.getTabCount(); i < n; i++) {
			Component tComponent = tabbedPane.getTabComponentAt(i);
			if (tComponent == tabComponent) {
				tabbedPane.setSelectedIndex(i);
				return;
			}
		}
	}

	public void paintCreatedFileGreen() {

		if (!businessLogic.getDestinationFile().exists()) {
			return;
		}

		JTextField fieldToPaint;
		switch (selectedParserId) {
			case VhdlBeh2HlddBeh:
				fieldToPaint = behHlddTextField;
				break;
			case VhdlBehDd2HlddBeh:
				fieldToPaint = behDDHlddTextField;
				break;
			case HlddBeh2HlddRtl:
				fieldToPaint = rtlRtlTextField;
				break;
			default:
				fieldToPaint = null;
		}

		if (fieldToPaint != null) {
			fieldToPaint.setForeground(Color.GREEN.darker().darker());
			fieldToPaint.repaint();
		}
	}

	private void updateParserId() {
		int selectedIndex = parserComboBox.getSelectedIndex();
		switch (selectedIndex) {
			case 0:
				selectedParserId = ParserID.VhdlBeh2HlddBeh;
				break;
			case 1:
				selectedParserId = ParserID.VhdlBehDd2HlddBeh;
				break;
			case 2:
				selectedParserId = ParserID.HlddBeh2HlddRtl;
				break;
			case 3:
				selectedParserId = ParserID.PSL2THLDD;
				break;
			default:
				throw new RuntimeException("Cannot update selected ParserId for specified ComboBox index: " + selectedIndex);
		}
	}

	public void doSaveConvertedModel() {
		/* Save converted file */
		enableUI(false);
		try {
			businessLogic.saveModel();
		} catch (ExtendedException e1) {
			JOptionPane.showMessageDialog(frame, e1.getMessage(), e1.getTitle(), JOptionPane.WARNING_MESSAGE);
		} finally {
			enableUI(true);
		}
	}

	public void doAskForComment() {
		/* File successfully converted. Ask for comment */
		if (!commentCheckBox.isSelected()) {
			return;
		}
		CommentDialog commentDialog = new CommentDialog(frame, "File successfully converted");
		businessLogic.addComment(commentDialog.getComment());
	}

	public void doClickHighlightButton() {
		covHighlightButton.doClick();
	}

	public void doLoadHlddGraph(File hlddGraphFile) {
		if (hlddGraphFile != null) {
			addPictureTab(hlddGraphFile.getName(), hlddGraphFile.getAbsolutePath(), new PicturePanel(hlddGraphFile));
		}
	}

	public int getPatternCountForAssert() {
		return (Integer) patternNrSpinnerAssert.getValue();
	}

	public int getPatternCountForCoverage() {
		return (Integer) covPatternNrSpinner.getValue();
	}

	public int getPatternCountForDiag() {
		return (Integer) diagPatternNrSpinner.getValue();
	}

	public int getDrawPatternCount() {
		return (Integer) drawPatternCountSpinner.getValue();
	}

	public boolean isDoCheckAssertion() {
		return checkAssertionCheckBox.isSelected();
	}

	public boolean isDoAnalyzeCoverage() {
		return coverageCheckBox.isSelected();
	}

	public boolean isDoDiagnose() {
		return diagnoseCheckBox.isSelected();
	}

	public boolean isDoDiagOptimize() {
		return optimizeCheckBox.isSelected();
	}

	public boolean isDoDiagPotential() {
		return potentialCheckBox.isSelected();
	}

	public boolean isDoDiagScoreByFailed() {
		return scorebyfailedCheckBox.isSelected();
	}

	public boolean isDoDiagScoreByRatio() {
		return scorebyratioCheckBox.isSelected();
	}


	public String getCoverageAnalyzerDirective() {
		if (!isDoAnalyzeCoverage()) {
			return null;
		}
		StringBuilder directiveBuilder = new StringBuilder(4);
		if (nodeCheckBox.isSelected()) {
			directiveBuilder.append("n");
		}
		if (edgeCheckBox.isSelected()) {
			directiveBuilder.append("e");
		}
		if (conditionCheckBox.isSelected()) {
			directiveBuilder.append("c");
		}
		if (toggleCheckBox.isSelected()) {
			directiveBuilder.append("t");
		}
		return directiveBuilder.toString();
	}

	public void updateChkFileTextField(File file) {
		updateTextFieldFor(chkFileButton, file);
	}

	private void updateCovTextField(File file) {
		updateTextFieldFor(covButton, file);
	}

	private void updateDgnTextField(File file) {
		updateTextFieldFor(dgnButton, file);
	}

	private void updateCovVhdlTextField(File file) {
		updateTextFieldFor(covVhdlButton, file);
	}

	public void updateDrawSpinner(int maxValue) {
		drawPatternCountSpinner.setModel(new SpinnerNumberModel(maxValue, 1, maxValue, 1));
		drawPatternCountSpinner.updateUI();
	}

	private void createUIComponents() {
		fileViewerTabbedPane1 = new PairedTabbedPane();
		fileViewerTabbedPane2 = new PairedTabbedPane();
		((PairedTabbedPane) fileViewerTabbedPane1).setPair(fileViewerTabbedPane2);
		((PairedTabbedPane) fileViewerTabbedPane2).setPair(fileViewerTabbedPane1);

		drawPatternCountSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, null, 1));
		patternNrSpinnerAssert = new JSpinner(new SpinnerNumberModel(1000, 1, null, 1));
		covPatternNrSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, null, 1));
		diagPatternNrSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, null, 1));
		addAllSelectingFocusListeners(drawPatternCountSpinner, patternNrSpinnerAssert, covPatternNrSpinner, diagPatternNrSpinner);

		consolePanel = new ConsolePanel();
	}

	private void addAllSelectingFocusListeners(JSpinner... spinners) {
		for (JSpinner spinner : spinners) {
			spinner.addFocusListener(new AllSelectingFocusListener(spinner));
		}
	}

	public void enableUI(boolean enable) {
		if (enable) {
			frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			frame.getGlassPane().setVisible(false);
		} else {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			frame.getGlassPane().setVisible(true);
		}
	}

	public JFrame getFrame() {
		return frame;
	}

	public File getSourceFile() {
		return businessLogic.getSourceFile();
	}

	public void enableCoverageAnalyzer(boolean enable) {
		covHlddButton.setEnabled(enable);
		covSimulButton.setEnabled(enable);
	}

	public void enableAssertionLoader(boolean enable) {
		drawButton.setEnabled(enable);
	}

	public void enableDiagnosis(boolean enable) {
		diagHlddButton.setEnabled(enable);
		diagSimulButton.setEnabled(enable);
	}

	public void enableHighlighter(boolean enable) {
		covVhdlButton.setEnabled(enable);
		covButton.setEnabled(enable);
		covHighlightButton.setEnabled(enable);

		diagVhdlButton.setEnabled(enable);
		dgnButton.setEnabled(enable);
		diagHighlightButton.setEnabled(enable);
	}

	public void addFileViewerTabFromFile(File selectedFile, LinesStorage linesStorage, JTabbedPane tabbedPane) {
		if (isWaveform(selectedFile)) {
			setChkFile(selectedFile);
			businessLogicAssertionChecker.processDraw();
		} else {
			if (tabbedPane == null) {
				tabbedPane = fileViewerTabbedPane1;
			}
			addFileViewerTab(tabbedPane, selectedFile.getName(), selectedFile.getAbsolutePath(), new TableForm(selectedFile,
					tabbedPane.getComponentAt(tabbedPane.getTabCount() - 1).getWidth(), linesStorage, fileDropHandler).getMainPanel(),
					!linesStorage.isEmpty());
		}

	}

	public void addFileViewerTab(JTabbedPane tabbedPane, String tabTitle, String tabToolTip, JComponent component, boolean isDirty) {
		TabComponent tabComponent;
		/* Search for equal existing tab */
		int insertionIndex = getIdenticalTabIndex(tabbedPane, tabToolTip);
		if (insertionIndex == -1) {
			/* Previously existing tab is not found. Create a new one. */
			insertionIndex = tabbedPane.getTabCount() - 1;
			tabbedPane.insertTab(tabTitle, null, component, null, insertionIndex);
			tabComponent = new TabComponent(tabbedPane, tabTitle, tabToolTip,
					tabbedPane == fileViewerTabbedPane1 ? tabbedPaneListener : tabbedPaneListener2);
			TabComponent.setBackgroundFor(tabComponent, isDirty);
			tabbedPane.setTabComponentAt(insertionIndex, tabComponent);
		} else {
			/* Previously existing tab is found. Replace its component with a new one (the specified one). */
			tabComponent = (TabComponent) tabbedPane.getTabComponentAt(insertionIndex);
			TabComponent.setBackgroundFor(tabComponent, isDirty);
			tabbedPane.setComponentAt(insertionIndex, component);
			tabbedPane.repaint();
			System.gc();
		}
		if (component instanceof TableForm.TableFormPanel) {
			((TableForm.TableFormPanel) component).getTableForm().setTabComponent(tabComponent);
		}
		synchronizeScroll(tabbedPane, component, tabToolTip);
		/* Activate new tab */
		tabbedPane.setSelectedIndex(insertionIndex);
	}

	private void synchronizeScroll(JTabbedPane tabbedPane, JComponent component, String tabToolTip) {

		Component firstComp = component.getComponent(0);
		if (!(firstComp instanceof JScrollPane)) {
			return;
		}

		JTabbedPane otherPane = getOtherTabbedPane(tabbedPane);

		int index = findSameFileInOtherTabbedPane(tabToolTip, otherPane);

		if (index == -1) {
			return;
		}

		Component otherComponent = otherPane.getComponentAt(index);

		if (!(otherComponent instanceof JComponent)) {
			return;
		}
		Component otherFirstComp = ((JComponent) otherComponent).getComponent(0);

		if (!(otherFirstComp instanceof JScrollPane)) {
			return;
		}

		JScrollPane scroll = (JScrollPane) firstComp;
		JScrollPane otherScroll = (JScrollPane) otherFirstComp;

		scroll.getVerticalScrollBar().setModel(otherScroll.getVerticalScrollBar().getModel());
		scroll.getHorizontalScrollBar().setModel(otherScroll.getHorizontalScrollBar().getModel());
	}

	private JTabbedPane getOtherTabbedPane(JTabbedPane tabbedPane) {
		if (tabbedPane == fileViewerTabbedPane1) {
			return fileViewerTabbedPane2;
		} else if (tabbedPane == fileViewerTabbedPane2) {
			return fileViewerTabbedPane1;
		} else {
			throw new RuntimeException("Obtaining other tabbed pane for unknown pane (neither LEFT nor RIGHT)");
		}
	}

	public static int findSameFileInOtherTabbedPane(String toolTipText, JTabbedPane otherTabbedPane) {
		int total = otherTabbedPane.getTabCount();
		for (int i = 0; i < total; i++) {
			Component component = otherTabbedPane.getTabComponentAt(i);
			if (component instanceof TabComponent) {
				TabComponent tabComponent = (TabComponent) component;
				if (tabComponent.getToolTipText().equalsIgnoreCase(toolTipText)) {
					return i;
				}
			}
		}
		return -1;
	}

	public void addSimulation(String tabTitle, String tabToolTip, JComponent component) {
		addFileViewerTab(fileViewerTabbedPane2, tabTitle, tabToolTip, component, false);
	}

	public void addPictureTab(String tabTitle, String tabToolTip, JComponent component) {
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(component);
		/* Search for equal existing tab */
		int insertionIndex = getIdenticalTabIndex(pictureTabPane, tabToolTip);
		if (insertionIndex == -1) {
			/* Previously existing tab is not found. Create a new one. */
			insertionIndex = pictureTabPane.getTabCount();
			pictureTabPane.insertTab(tabTitle, null, scrollPane, tabToolTip/*null*/, insertionIndex);
			pictureTabPane.setTabComponentAt(insertionIndex, new TabComponent(pictureTabPane, tabTitle, tabToolTip, picturePaneAdapter));
		} else {
			/* Previously existing tab is found. Replace its component with a new one (the specified one). */
			pictureTabPane.setComponentAt(insertionIndex, scrollPane);
			System.gc();
		}
		/* Activate new tab */
		pictureTabPane.setSelectedIndex(insertionIndex);
	}

	public void addCoverage(String tabTitle, String tabToolTip, JComponent component) {
		/* Search for equal existing tab */
		int insertionIndex = getIdenticalTabIndex(upperRightTabbedPane, tabToolTip);
		if (insertionIndex == -1) {
			/* Previously existing tab is not found. Create a new one. */
			insertionIndex = upperRightTabbedPane.getTabCount();
			upperRightTabbedPane.insertTab(tabTitle, null, component, tabToolTip, insertionIndex);
			final TabComponent tabComponent = new TabComponent(upperRightTabbedPane, tabTitle, tabToolTip, upperRightTabbedPaneAdapter);
			tabComponent.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						File covFile = new File(tabComponent.getToolTipText());
						setCovFile(covFile);
					}
				}
			});
			upperRightTabbedPane.setTabComponentAt(insertionIndex, tabComponent);
		} else {
			/* Previously existing tab is found. Replace its component with a new one (the specified one). */
			upperRightTabbedPane.setComponentAt(insertionIndex, component);
		}
		/* Activate new tab */
		upperRightTabbedPane.setSelectedIndex(insertionIndex);
	}


	private int getIdenticalTabIndex(JTabbedPane tabbedPane, String tabToolTip) {
		for (int index = 0; index < tabbedPane.getTabCount(); index++) {
			Component tabComponent = tabbedPane.getTabComponentAt(index);
			if (tabComponent instanceof TabComponent) {
				if (((TabComponent) tabComponent).getToolTipText().equalsIgnoreCase(tabToolTip)) {
					return index;
				}
			}
		}
		return -1;
	}

	public BusinessLogic.HLDDRepresentationType getHlddRepresentationType() {
		if (selectedParserId == ParserID.VhdlBeh2HlddBeh) {
			return vhdlBehOptionsPanel.getHlddType();
		} else if (selectedParserId == ParserID.VhdlBehDd2HlddBeh) {
			return vhdlBehDdOptionsPanel.getHlddType();
		} else return null;
	}


	private static class MainWindowResizer extends ComponentAdapter {
		private static final int LIMIT_HEIGHT = 385;
		private static final int LIMIT_WIDTH = 1024;

		public void componentResized(ComponentEvent e) {
			Component component = e.getComponent();
			int width = component.getWidth();
			int height = component.getHeight();
			if (component.getHeight() < LIMIT_HEIGHT) {
				component.setSize(width, LIMIT_HEIGHT);
			}
			if (component.getWidth() < LIMIT_WIDTH) {
				component.setSize(LIMIT_WIDTH, height);
			}
		}
	}

	private class RadioButtonToSpinnerLinker implements ChangeListener {
		private final JRadioButton radioButton;
		private final JSpinner spinner;

		public RadioButtonToSpinnerLinker(JRadioButton radioButton, JSpinner spinner) {
			this.radioButton = radioButton;
			this.spinner = spinner;
		}

		public void stateChanged(ChangeEvent e) {
			spinner.setEnabled(radioButton.isSelected());
		}
	}

	private class AllSelectingFocusListener extends FocusAdapter {
		public AllSelectingFocusListener(JSpinner spinner) {
			JTextField field = getTextFieldEditor(spinner);
			if (field != null) {
				field.addFocusListener(this);
			}
		}

		private JTextField getTextFieldEditor(JSpinner spinner) {
			Component editor = spinner.getEditor().getComponent(0);
			return editor instanceof JTextField ? (JTextField) editor : null;
		}

		public void focusGained(FocusEvent e) {
			if (e.getSource() instanceof JTextField) {
				final JTextField textField = (JTextField) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						textField.selectAll();
					}
				});
			}
		}
	}

	private class CoverageCheckBoxSetter implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == coverageCheckBox) {
				boolean isSelected = coverageCheckBox.isSelected();
				/* Switch ALL boxes ON/OFF */
				edgeCheckBox.setSelected(isSelected);
				conditionCheckBox.setSelected(isSelected);
				nodeCheckBox.setSelected(isSelected);
				toggleCheckBox.setSelected(isSelected);
			} else {
				if (edgeCheckBox.isSelected() || conditionCheckBox.isSelected()
						|| nodeCheckBox.isSelected() || toggleCheckBox.isSelected()) {
					coverageCheckBox.setSelected(true);
				} else {
					coverageCheckBox.setSelected(false);
				}
			}
		}
	}

	private class ButtonAndTextField {
		private final JButton button;
		private final JTextField textField;

		public ButtonAndTextField(JButton button, JTextField textField) {
			this.button = button;
			this.textField = textField;
		}
	}

	public static class FileDropHandler extends TransferHandler implements KeyListener {

		private final ApplicationForm applicationForm;


		public FileDropHandler(ApplicationForm applicationForm) {
			this.applicationForm = applicationForm;
		}

		@Override
		public boolean canImport(TransferSupport support) {

			return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}

		@Override
		public boolean importData(TransferSupport support) {

			//noinspection SimplifiableIfStatement
			if (!canImport(support)) {
				return false;
			}

			processTransferable(support.getTransferable());

			return true; /* regardless of loading success */
		}

		private void processTransferable(Transferable transferable) {

			final List<File> files;
			try {
				//noinspection unchecked
				files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
			} catch (Exception e) {
				return;
			}

			// leave EDT
			new Thread(new Runnable() {
				public void run() {

					for (File file : files) {

						loadFile(file);
					}

				}
			}).start();

		}

		private void loadFile(final File file) {

			if (isVHDL(file)) {

				applicationForm.setBehVhdlFile(file);
				applicationForm.setBehDDVhdlFile(file);
				applicationForm.setCovVhdlFile(file);
				applicationForm.setDiagVhdlFile(file);

			} else if (isCOV(file)) {

				waitForPreviousToComplete(applicationForm.highlighter);

				applicationForm.setCovFile(file);

				applicationForm.tabbedPane.setSelectedIndex(2);

			} else if (isHLDD(file)) {

				applicationForm.setBehHlddFile(file);
				applicationForm.setBehDDHlddFile(file);
				applicationForm.setRtlBehFile(file);
				applicationForm.setBaseModelFile(file);
				applicationForm.setAssertHlddFile(file);
				applicationForm.setCovHlddFile(file);
				applicationForm.setDiagHlddFile(file);

			} else if (isPPG(file)) {

				applicationForm.setPPGLibFile(file);

				applicationForm.tabbedPane.setSelectedIndex(0);

				applicationForm.parserComboBox.setSelectedIndex(3);

			} else if (isWaveform(file)) {

				waitForPreviousToComplete(applicationForm.businessLogicAssertionChecker);

				applicationForm.addFileViewerTabFromFile(file, LinesStorage.emptyStorage(), applicationForm.fileViewerTabbedPane2);

				applicationForm.tabbedPane.setSelectedIndex(1);

			} else if (isPSL(file)) {

				applicationForm.setPslFile(file);

				applicationForm.tabbedPane.setSelectedIndex(0);

				applicationForm.parserComboBox.setSelectedIndex(3);

			} else if (isDGN(file)) {

				waitForPreviousToComplete(applicationForm.highlighter);

				applicationForm.setDgnFile(file);

				applicationForm.tabbedPane.setSelectedIndex(3);

			}
		}

		private void waitForPreviousToComplete(Lockable lockable) {

			while (lockable.isLocked()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {/* do nothing */
				}
			}
			lockable.lock();
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {

			if (isCtrlV(e)) {

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

				if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {

					processTransferable(clipboard.getContents(null));

				}
			}
		}

		private boolean isCtrlV(KeyEvent e) {
			return e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V;
		}

	}

	private class TableFormFocuser implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			Object source = e.getSource();
			if (source instanceof JTabbedPane) {
				JTabbedPane tabbedPane = (JTabbedPane) source;
				final JTable table = new TableFinder(tabbedPane).find();
				if (table != null) {
					// dirty hack: requesting focus at once will fail,
					// because after invoking user-defined ChangeListeners,
					// default ChangeListener of the JTabbedPane is invoked,
					// which transfers the focus away from where we've set it.
					Timer timer = new Timer(50, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							table.requestFocus();
							table.getSelectionModel().setSelectionInterval(0, 0);
						}
					});
					timer.setRepeats(false);
					timer.start();
				}
			} else {
				JTable table = new TableFinder(fileViewerTabbedPane1).find();
				if (table == null) {
					table = new TableFinder(fileViewerTabbedPane2).find();
				}
				if (table != null) {
					table.requestFocus();
				}
			}
		}
	}

	private class ScoreToggler implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			Object source = e.getSource();
			if (source == scorebyfailedCheckBox) {
				if (scorebyfailedCheckBox.isSelected()) {
					scorebyratioCheckBox.setSelected(false);
				}
			} else if (source == scorebyratioCheckBox) {
				if (scorebyratioCheckBox.isSelected()) {
					scorebyfailedCheckBox.setSelected(false);
				}
			}
		}
	}
}
