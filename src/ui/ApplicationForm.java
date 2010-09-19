package ui;

import ui.optionPanels.VHDLBehOptionsPanel;
import ui.optionPanels.VHDLBehDdOptionsPanel;
import ui.optionPanels.HLDDBehOptionsPanel;
import ui.optionPanels.PSLOptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import ui.BusinessLogic.ParserID;
import ui.fileViewer.TabbedPaneListener;
import ui.fileViewer.TableForm;
import ui.fileViewer.TabComponent;
import ui.fileViewer.MouseSelectionAdapter;
import base.helpers.ExceptionSolver;
import io.ConsoleWriter;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 21.02.2008
 * <br>Time: 11:20:06
 */
public class ApplicationForm implements ActionListener {
    public static final String LIB_DIR = "." + File.separator + "lib" + File.separator;
    private static final String BASE_MODEL_BUTTON_TOOLTIP = "<html>Base HLDD model file.<p>This should at least " +
            "declare all the variables (their lengths and flags) used in PSL properties.</html>";

    private JComboBox parserComboBox;
    private JButton vhdlButton;
    private JTextField vhdlTextField;
    private JTextField hlddTextField;
    private JButton hlddButton;
    private JButton parseButton;
    private JPanel optionsPanel;
	private JTabbedPane tabbedPane;
    private JCheckBox checkAssertionCheckBox;
    private JButton simulateButton;
    private JSpinner drawPatternCountSpinner;
    private JButton drawButton;
    private JButton hlddSimulButton;
    private JTextField hlddSimulTextField;
    private JButton chkFileButton;
    private JFormattedTextField chkFileTextField;
    private JSpinner patternNrSpinnerAssert;
    private JButton hlddAssertButton;
    private JTextField hlddAssertTextField;
    private JButton tgmButton;
    private JButton checkButton;
    private JButton hlddCoverageButton;
    private JTextField hlddCoverageTextField;
    private JButton analyzeButton;
    private JSpinner patternNrSpinnerCoverage;
    private JPanel mainPanel;
    private JTextArea consoleTextArea;
    private JTabbedPane fileViewerTabbedPane1;
    private JPanel clickMePanel1;
    private JScrollPane infoScrollPane;
    private JPanel consolePanel;
    private JRadioButton randomAssertRadioButton;
    private JRadioButton tstAssertRadioButton;
    private JRadioButton tstCovRadioButton;
    private JRadioButton randomCovRadioButton;
    private JButton vhdlCovButton;
    private JTextField vhdlCovTextField;
    private JButton covButton;
    private JTextField covTextField;
    private JButton showButton;
    private JTextField tgmTextField;
    private JCheckBox analyzeCoverageCheckBox;
    private JTabbedPane pictureTabPane;
    private JTabbedPane upperRightTabbedPane;
    private JTabbedPane fileViewerTabbedPane2;
    private JPanel clickMePanel2;
    private JSplitPane fileViewerSplitPane;
	private JCheckBox nodeCheckBox;
	private JCheckBox edgeCheckBox;
	private JCheckBox toggleCheckBox;
	private JCheckBox conditionCheckBox;
	private MouseSelectionAdapter upperRightTabbedPaneAdapter;
    private MouseSelectionAdapter picturePaneAdapter;


    private JButton ppgLibButton;

    private final Map<JButton, JTextField> textFieldByButton = new HashMap<JButton, JTextField>();

    private static JFrame frame;

    private BusinessLogic businessLogic = null;
    private ParserID selectedParserId = null;
    private VHDLBehOptionsPanel vhdlBehOptionsPanel = null;
    private VHDLBehDdOptionsPanel vhdlBehDdOptionsPanel = null;
    private HLDDBehOptionsPanel hlddBehOptionsPanel = null;
    private PSLOptionsPanel pslOptionsPanel = null;

    private BusinessLogicSimulation businessLogicSimul = null;
    private BusinessLogicAssertionChecker businessLogicAssertionChecker = null;
    private BusinessLogicCoverageAnalyzer businessLogicCoverageAnalyzer = null;

    private TabbedPaneListener tabbedPaneListener;
    private TabbedPaneListener tabbedPaneListener2;

	public ApplicationForm() {
        /* ConsoleWriter to write into a consoleTextArea */
        ConsoleWriter consoleWriter = new ConsoleWriter(consoleTextArea, false);
        infoScrollPane.getVerticalScrollBar().addAdjustmentListener((AdjustmentListener) consolePanel);
        consoleTextArea.addMouseListener(((ConsolePanel) consolePanel).getConsoleMouseAdapter());

        fileViewerSplitPane.setDividerLocation(700);
        /* Create PARSERS options panels */
        selectedParserId = ParserID.getSelected(parserComboBox.getSelectedIndex());
		OutputFileGenerator outputFileGenerator = new OutputFileGenerator(this, hlddButton);
		vhdlBehOptionsPanel = new VHDLBehOptionsPanel(outputFileGenerator);
        vhdlBehDdOptionsPanel = new VHDLBehDdOptionsPanel(outputFileGenerator);
        hlddBehOptionsPanel = new HLDDBehOptionsPanel();
        pslOptionsPanel = new PSLOptionsPanel();

        ppgLibButton = pslOptionsPanel.getPpgLibButton();

        /* PARSERS */
        businessLogic = new BusinessLogic(this, consoleWriter);
        addActionListener(vhdlButton, hlddButton, parseButton, parserComboBox, ppgLibButton);
		vhdlTextField.getDocument().addDocumentListener(outputFileGenerator);
		vhdlTextField.getDocument().addDocumentListener(new RTLOutputFileGenerator(this, hlddButton));
        /* SIMULATION */
        businessLogicSimul = new BusinessLogicSimulation(this, consoleWriter);
        addActionListener(hlddSimulButton, simulateButton, chkFileButton, drawButton);
        /* ASSERTION CHECKER */
        businessLogicAssertionChecker = new BusinessLogicAssertionChecker(this, consoleWriter);
        addActionListener(hlddAssertButton, tgmButton, checkButton);
        /* COVERAGE ANALYSIS */
        businessLogicCoverageAnalyzer = new BusinessLogicCoverageAnalyzer(this, consoleWriter);
        addActionListener(hlddCoverageButton, analyzeButton, vhdlCovButton, covButton, showButton);

        /* Add Mouse Listener to the File Viewer Tabbed Pane */
        tabbedPaneListener = new TabbedPaneListener(this, fileViewerTabbedPane1, clickMePanel1);
        tabbedPaneListener2 = new TabbedPaneListener(this, fileViewerTabbedPane2, clickMePanel2);
        clickMePanel1.addMouseListener(tabbedPaneListener);
        clickMePanel2.addMouseListener(tabbedPaneListener2);
        upperRightTabbedPaneAdapter = new MouseSelectionAdapter(upperRightTabbedPane);
        picturePaneAdapter = new MouseSelectionAdapter(pictureTabPane);


        /* Add empty Mouse Listener to the Glass Pane, to disable user input while SwingWorker is working */
        Component glassPane = frame.getGlassPane();
        glassPane.addMouseListener(new MouseAdapter() {});

        updateParserUI();
        ExceptionSolver.getInstance().setFrame(frame);
        /* Create Button-to-TextField mapping */
        mapTextFieldsToButtons();
        randomAssertRadioButton.addChangeListener(
                new RadioButtonToSpinnerLinker(randomAssertRadioButton, patternNrSpinnerAssert));
        randomCovRadioButton.addChangeListener(
                new RadioButtonToSpinnerLinker(randomCovRadioButton, patternNrSpinnerCoverage));

		CoverageCheckBoxSetter checkBoxSetter = new CoverageCheckBoxSetter();
		analyzeCoverageCheckBox.addActionListener(checkBoxSetter);
		edgeCheckBox.addActionListener(checkBoxSetter);
		conditionCheckBox.addActionListener(checkBoxSetter);
		nodeCheckBox.addActionListener(checkBoxSetter);
		toggleCheckBox.addActionListener(checkBoxSetter);
	}

    private void mapTextFieldsToButtons() {
        textFieldByButton.put(vhdlButton, vhdlTextField);
        textFieldByButton.put(hlddButton, hlddTextField);
        textFieldByButton.put(ppgLibButton, pslOptionsPanel.getPpgLibTextField());
        textFieldByButton.put(hlddSimulButton, hlddSimulTextField);
        textFieldByButton.put(hlddAssertButton, hlddAssertTextField);
        textFieldByButton.put(tgmButton, tgmTextField);
        textFieldByButton.put(chkFileButton, chkFileTextField);
        textFieldByButton.put(hlddCoverageButton, hlddCoverageTextField);
        textFieldByButton.put(vhdlCovButton, vhdlCovTextField);
        textFieldByButton.put(covButton, covTextField);
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
        if (sourceButton == vhdlButton) {
            switch (selectedParserId) {
                case VhdlBeh2HlddBeh:
                    extensions = new String[]{"vhdl", "vhd"};
                    dialogTitle = "Select source VHDL Behavioural file";
                    invalidFileMessage = "Selected file is not a VHDL file!";
                    break;
                case VhdlBehDd2HlddBeh:
                    extensions = new String[]{"vhdl", "vhd"};
                    dialogTitle = "Select source HIF file"; //todo: VHDL Behavioural DD 
                    invalidFileMessage = "Selected file is not a HIF file!"; //todo: VHDL
                    break;
                case HlddBeh2HlddRtl:
                    extensions = new String[]{"agm"};
                    dialogTitle = "Select source HLDD Behavioural file";
                    invalidFileMessage = "Selected file is not an HLDD file!";
                    break;
                case PSL2THLDD:
                    extensions = new String[]{"agm"};
                    dialogTitle = "Select Base HLDD model file";
                    invalidFileMessage = "Selected file is not an HLDD file!";
                    break;
                default:
                    return;
            }
        } else if (sourceButton == hlddButton) {
            proposedFileName = businessLogic.getProposedFileName();
            invalidFileMessage = "Selected file is not an HLDD file!";
            switch (selectedParserId) {
                case VhdlBeh2HlddBeh:
                    extensions = new String[]{"agm"};
                    dialogTitle = "Select output HLDD Behavioural file";
                    break;
                case VhdlBehDd2HlddBeh:
                    extensions = new String[]{"agm"};
                    dialogTitle = "Select output HLDD Behavioural file";
                    break;
                case HlddBeh2HlddRtl:
                    extensions = new String[]{"agm"};
                    dialogTitle = "Select output HLDD RTL file";
                    break;
                case PSL2THLDD:
                    extensions = new String[]{"psl"};
                    dialogTitle = "Select source PSL file";
                    invalidFileMessage = "Selected file is not a PSL file!";
                    break;
                default:
                    return;
            }
        } else if (sourceButton == ppgLibButton) {
            extensions = new String[]{"lib"};
            dialogTitle = "Select PPG Library file";
            invalidFileMessage = "Selected file is not a PPG Library file!";
        } else if (sourceButton == hlddSimulButton) {
            proposedFileName = businessLogicSimul.getProposedFileName();
            extensions = new String[]{"agm"};
            dialogTitle = "Select HLDD model file to simulate";
            invalidFileMessage = "Selected file is not an HLDD file!";
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
        } else if (sourceButton == hlddCoverageButton) {
            extensions = new String[]{"agm"};
            dialogTitle = "Select HLDD model file to analyze";
            invalidFileMessage = "Selected file is not an HLDD file!";
        } else if (sourceButton == vhdlCovButton) {
            extensions = new String[]{"vhdl", "vhd"};
            dialogTitle = "Select source VHDL Behavioural file";
            invalidFileMessage = "Selected file is not a VHDL file!";
        } else if (sourceButton == covButton) {
            extensions = new String[]{"cov"};
            dialogTitle = "Select Coverage file";
            invalidFileMessage = "Selected file is not a Coverage file!";
        } else return;

        SingleFileSelector selector = SingleFileSelector.getInstance(SingleFileSelector.DialogType.OPEN_DIALOG,
                extensions, proposedFileName, dialogTitle, invalidFileMessage);
        if (selector.isFileSelected()) {
            try {
                /* Check the input for VALIDITY */
                selector.validateFile();
                File selectedFile = selector.getRestrictedSelectedFile();
                if (sourceButton == ppgLibButton) {
                    businessLogic.setSourceFile(selectedFile);
                } else if (sourceButton == hlddSimulButton) {
                    businessLogicSimul.setHlddFile(selectedFile);
                } else if (sourceButton == hlddAssertButton) {
                    businessLogicAssertionChecker.setHlddFile(selectedFile);
                    /* Automatically look for identical Patterns file and TGM file */
                    triggerAutomaticSelection(selectedFile, sourceButton);
                } else if (sourceButton == tgmButton) {
                    checkAssertionCheckBox.setSelected(true);
                    businessLogicAssertionChecker.setTgmFile(selectedFile);
                } else if (sourceButton == chkFileButton) {
                    businessLogicAssertionChecker.setSimulFile(selectedFile);
                    businessLogicAssertionChecker.loadChkFile();
                } else if (sourceButton == hlddCoverageButton) {
                    businessLogicCoverageAnalyzer.setHlddFile(selectedFile);
                    triggerAutomaticSelection(selectedFile, sourceButton);
                } else if (sourceButton == vhdlCovButton) {
                    businessLogicCoverageAnalyzer.setVhdlFile(selectedFile);

                } else if (sourceButton == covButton) {
                    businessLogicCoverageAnalyzer.setCovFile(selectedFile);

                } else {
                    if (sourceButton == vhdlButton) {
                        if (selectedParserId == ParserID.PSL2THLDD) {
                            businessLogic.setBaseModelFile(selectedFile);
                        } else {
                            businessLogic.setSourceFile(selectedFile);
                        }
                        /* Clear HLDD File in all Parsers */
                        businessLogic.setDestFile(null);
                        updateTextFieldFor(hlddButton, null);
                    } else {
                        businessLogic.setDestFile(selectedFile);
                        /* Automatically look for identical PSL Base Model file */
                        triggerAutomaticSelection(selectedFile, sourceButton);
                    }
                }
                updateTextFieldFor(sourceButton, selectedFile);

            } catch (ExtendedException e) {
                showErrorMessage(e);
            }
        }

    }

    private void triggerAutomaticSelection(File selectedFile, JButton pressedButton) {
        if (pressedButton == hlddButton && selectedParserId == ParserID.PSL2THLDD) {
            /* Automatically look for identical PSL Base Model file */
            File baseModelFile = businessLogic.deriveBaseModelFileFrom(selectedFile);
            if (baseModelFile != null) {
                businessLogic.setBaseModelFile(baseModelFile);
                updateTextFieldFor(vhdlButton, baseModelFile);
            }
        } else if (pressedButton == hlddAssertButton) {
            /* Automatically look for identical TGM file */
            File tgmFile = businessLogicAssertionChecker.deriveTGMFileFrom(selectedFile);
            if (tgmFile != null) {
                checkAssertionCheckBox.setSelected(true);
                businessLogicAssertionChecker.setTgmFile(tgmFile);
                updateTextFieldFor(tgmButton, tgmFile);
            }

            /* Automatically look for identical Patterns file */
            selectIdenticalTSTFile(businessLogicAssertionChecker.derivePatternsFileFrom(selectedFile),
                    tstAssertRadioButton, randomAssertRadioButton, patternNrSpinnerAssert);

        } else if (pressedButton == hlddCoverageButton) {            
            /* Automatically look for identical Patterns file */
            selectIdenticalTSTFile(businessLogicCoverageAnalyzer.derivePatternsFileFrom(selectedFile),
                    tstCovRadioButton, randomCovRadioButton, patternNrSpinnerCoverage);
        }
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


    private void updateParserUI() {
        /* Update OPTIONS */
        updateOptions();
        /* Update BUTTONS */
        updateButtons();
        /* Update TEXT AREAS */
        updateTextAreas();
        /* todo... */

    }

    private void updateTextAreas() {
        //todo: get texts from businessLogic
        updateTextFieldFor(vhdlButton, null);
        updateTextFieldFor(hlddButton, null);
        updateTextFieldFor(ppgLibButton, null);
    }

    private void updateButtons() {
        vhdlButton.setToolTipText(null);
        switch (selectedParserId) {
            case VhdlBeh2HlddBeh:
                vhdlButton.setText("VHDL");
                hlddButton.setText("HLDD");
                vhdlButton.setMnemonic(KeyEvent.VK_V);
                hlddButton.setMnemonic(KeyEvent.VK_H);
                break;
            case VhdlBehDd2HlddBeh:
                vhdlButton.setText("HIF");
                hlddButton.setText("HLDD");
                vhdlButton.setMnemonic(KeyEvent.VK_I);
                hlddButton.setMnemonic(KeyEvent.VK_H);
                break;
            case HlddBeh2HlddRtl:
                vhdlButton.setText("Beh.");
                hlddButton.setText("RTL");
                vhdlButton.setMnemonic(KeyEvent.VK_B);
                hlddButton.setMnemonic(KeyEvent.VK_R);
                break;
            default:
                vhdlButton.setText("Base Model");
                vhdlButton.setToolTipText(BASE_MODEL_BUTTON_TOOLTIP);
                hlddButton.setText("PSL");
                vhdlButton.setMnemonic(KeyEvent.VK_B);
                hlddButton.setMnemonic(KeyEvent.VK_P);
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

//        frame.pack();
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
        return randomCovRadioButton.isSelected();
    }

    public boolean shouldReuseConstants() {
        switch (selectedParserId) {
            case VhdlBeh2HlddBeh:
                return false;
            case VhdlBehDd2HlddBeh:
                return vhdlBehDdOptionsPanel.shouldReuseConstants();
            default:
                return false;
        }
//        if (vhdlBehOptionsPanel.getMainPanel().isShowing()) {
//            return vhdlBehOptionsPanel.shouldReuseConstants();
//        } else if (vhdlBehDdOptionsPanel.getMainPanel().isShowing()) {
//            return vhdlBehDdOptionsPanel.shouldReuseConstants();
//        } else return false;
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
        frame.setContentPane(applicationForm.getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addComponentListener(new MainWindowResizer());
        IconAdder.setFrameIcon(frame);
        frame.pack();
        frame.setVisible(true);
        UniversalFrameLocator.maximize(frame);
        
        ToolTipManager.sharedInstance().setDismissDelay(15000);

        /* Process exceptions of all SwingWorkers */ // http://book.javanb.com/java-threads-3rd/jthreads3-CHP-13-SECT-5.html
        Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(applicationForm));

    }

    private Container getMainPanel() {
        return mainPanel;
    }

    void updateTextFieldFor(JButton parentButton, File file) {
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
				if (textFieldToUpdate == hlddTextField && file.exists()) {
					color = Color.RED;
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
            if (source instanceof JButton && (source == vhdlButton || source == hlddButton || source == ppgLibButton
                    || source == hlddSimulButton
                    || source == chkFileButton || source == hlddAssertButton || source == tgmButton
                    || source == hlddCoverageButton || source == vhdlCovButton || source == covButton)) {

                showSelectFileDialog(((JButton) source));

            } else if (source == parseButton) {

                /* Reset previous logic */
                businessLogic.reset();
                /* Parse and convert file */
                businessLogic.processParse();

            } else if (source == parserComboBox) {
                selectedParserId = ParserID.getSelected(parserComboBox.getSelectedIndex());
                businessLogic.clearFiles();
                updateParserUI();
            } else if (source == simulateButton) {
                businessLogicSimul.processSimulate();
            } else if (source == checkButton) {
                businessLogicAssertionChecker.processCheck();
            } else if (source == drawButton) {
                businessLogicAssertionChecker.processDraw();
            } else if (source == analyzeButton) {
                businessLogicCoverageAnalyzer.processAnalyze();
            } else if (source == showButton) {
                businessLogicCoverageAnalyzer.processShow();
            }
        } catch (ExtendedException e1) {
			JOptionPane.showMessageDialog(frame, e1.getMessage(), e1.getTitle(), JOptionPane.WARNING_MESSAGE);
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
        CommentDialog commentDialog = new CommentDialog(frame, "File successfully converted");
        if (commentDialog.isCommentAdded()) {
            businessLogic.addComment(commentDialog.getComment());
        }
    }

    public void doClickShowButton() {
        showButton.doClick();
    }

    public void doLoadHlddGraph(File hlddGraphFile) {
        if (hlddGraphFile != null) {
            addPictureTab(hlddGraphFile.getName(), hlddGraphFile.getAbsolutePath(), new PicturePanel(hlddGraphFile));
        }
    }

    public int getPatternCountForAssert() {
        return (Integer) patternNrSpinnerAssert.getValue();
//        return Integer.valueOf(patternNrTextField.getText());
    }

    public int getPatternCountForCoverage() {
        return (Integer) patternNrSpinnerCoverage.getValue();
    }

    public int getDrawPatternCount() {
        return (Integer) drawPatternCountSpinner.getValue();
    }

    public boolean isDoCheckAssertion() {
        return checkAssertionCheckBox.isSelected();
    }

    public boolean isDoAnalyzeCoverage() {
        return analyzeCoverageCheckBox.isSelected();
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

    public void updateCovTextField(File file) {
        updateTextFieldFor(covButton, file);
    }

    public void updateVhdlCovTextField(File file) {
        updateTextFieldFor(vhdlCovButton, file);
    }

    public void updateDrawSpinner(int maxValue) {
        drawPatternCountSpinner.setModel(new SpinnerNumberModel(maxValue, 1, maxValue, 1));
        drawPatternCountSpinner.updateUI();
    }

    private void createUIComponents() {
        drawPatternCountSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, null, 1));
        patternNrSpinnerAssert = new JSpinner(new SpinnerNumberModel(1000, 1, null, 1));
        patternNrSpinnerCoverage = new JSpinner(new SpinnerNumberModel(1000, 1, null, 1));
        addAllSelectingFocusListeners(drawPatternCountSpinner, patternNrSpinnerAssert, patternNrSpinnerCoverage);

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

	public void setDestFile(File destFile) {
		businessLogic.setDestFile(destFile);
	}

	public void setEnableHlddSimulButton(boolean enable) {
        hlddSimulButton.setEnabled(enable);
    }

    public void setEnableSimulateButton(boolean enable) {
        simulateButton.setEnabled(enable);
    }

    public void setEnableHlddCoverageButton(boolean enable) {
        hlddCoverageButton.setEnabled(enable);
    }

    public void setEnableAnalyzeButton(boolean enable) {
        analyzeButton.setEnabled(enable);
    }

    public void setEnabledVhdlCoverageButton(boolean enabled) {
        vhdlCovButton.setEnabled(enabled);
    }

    public void setEnabledCovButton(boolean enabled) {
        covButton.setEnabled(enabled);
    }

    public void setEnabledShowButton(boolean enabled) {
        showButton.setEnabled(enabled);
    }

    public void setEnableDrawButton(boolean enable) {
        drawButton.setEnabled(enable);
    }

    public void addFileViewerTabFromFile(File selectedFile, Collection<Integer> nodesLines,
                                         Collection<Integer> edgesLines, JTabbedPane tabbedPane) {
        if (selectedFile.getName().endsWith(".chk") || selectedFile.getName().endsWith(".sim") ||
                selectedFile.getName().endsWith(".tst")) {
            businessLogicAssertionChecker.setSimulFile(selectedFile);
            businessLogicAssertionChecker.loadChkFile();
            businessLogicAssertionChecker.processDraw();
        } else {
            if (tabbedPane == null) {
                tabbedPane = fileViewerTabbedPane1;
            }
            addFileViewerTab(tabbedPane, selectedFile.getName(), selectedFile.getAbsolutePath(), new TableForm(selectedFile,
                    tabbedPane.getComponentAt(tabbedPane.getTabCount() - 1).getWidth(), nodesLines, edgesLines).getMainPanel());
        }

    }

    public void addFileViewerTab(JTabbedPane tabbedPane, String tabTitle, String tabToolTip, JComponent component) {
        /* Search for equal existing tab */
        int insertionIndex = getIdenticalTabIndex(tabbedPane, tabToolTip);
        if (insertionIndex == -1) {
            /* Previously existing tab is not found. Create a new one. */
            insertionIndex = tabbedPane.getTabCount() - 1;
            tabbedPane.insertTab(tabTitle, null, component, null, insertionIndex);
            tabbedPane.setTabComponentAt(insertionIndex, new TabComponent(tabbedPane, tabTitle, tabToolTip,
                    tabbedPane == fileViewerTabbedPane1 ? tabbedPaneListener : tabbedPaneListener2));
        } else {
            /* Previously existing tab is found. Replace its component with a new one (the specified one). */
            tabbedPane.setComponentAt(insertionIndex, component);
            System.gc();
        }
        /* Activate new tab */
        tabbedPane.setSelectedIndex(insertionIndex);
    }

    public void addSimulation(String tabTitle, String tabToolTip, JComponent component) {
        addFileViewerTab(fileViewerTabbedPane2, tabTitle, tabToolTip, component);
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
            upperRightTabbedPane.insertTab(tabTitle, null, component, tabToolTip/*null*/, insertionIndex);
            upperRightTabbedPane.setTabComponentAt(insertionIndex, new TabComponent(upperRightTabbedPane, tabTitle, tabToolTip, upperRightTabbedPaneAdapter));
        } else {
            /* Previously existing tab is found. Replace its component with a new one (the specified one). */
            upperRightTabbedPane.setComponentAt(insertionIndex, component);
            System.gc();
        }
        /* Activate new tab */
        upperRightTabbedPane.setSelectedIndex(insertionIndex);
    }



    private int getIdenticalTabIndex(JTabbedPane tabbedPane, String tabToolTip) {
        for (int index = 0; index < tabbedPane.getTabCount(); index++) {
            Component tabComponent = tabbedPane.getTabComponentAt(index);
            if (tabComponent instanceof TabComponent) {
                if (((TabComponent) tabComponent).getToolTipText().equals(tabToolTip)) {
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
			if (source == analyzeCoverageCheckBox) {
				boolean isSelected = analyzeCoverageCheckBox.isSelected();
				/* Switch ALL boxes ON/OFF */
				edgeCheckBox.setSelected(isSelected);
				conditionCheckBox.setSelected(isSelected);
				nodeCheckBox.setSelected(isSelected);
				toggleCheckBox.setSelected(isSelected);
			} else {
				if (edgeCheckBox.isSelected() || conditionCheckBox.isSelected()
						|| nodeCheckBox.isSelected() || toggleCheckBox.isSelected()) {
					analyzeCoverageCheckBox.setSelected(true);
				} else {
					analyzeCoverageCheckBox.setSelected(false);
				}
			}
		}
	}
}
