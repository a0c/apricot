package ui.optionPanels;

import ui.ApplicationForm.FileDropHandler;
import ui.BusinessLogic;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class VHDLBehDdOptionsPanel {
	private JPanel mainPanel;
	private JCheckBox simplifyCheckBox;
	private JRadioButton reducedRadioButton;
	private JRadioButton minimizedRadioButton;
	private JRadioButton expandCSCheckBox;
	private JRadioButton fullRadioButton;
	private JRadioButton collapseCheckBox;

	public VHDLBehDdOptionsPanel(FileDropHandler fileDropHandler) {
		fullRadioButton.addKeyListener(fileDropHandler);
		reducedRadioButton.addKeyListener(fileDropHandler);
		minimizedRadioButton.addKeyListener(fileDropHandler);
		expandCSCheckBox.addKeyListener(fileDropHandler);
		collapseCheckBox.addKeyListener(fileDropHandler);
		simplifyCheckBox.addKeyListener(fileDropHandler);
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public boolean shouldSimplify() {
		return simplifyCheckBox.isSelected();
	}

	public BusinessLogic.HLDDRepresentationType getHlddType() {
		if (reducedRadioButton.isSelected()) {
			return BusinessLogic.HLDDRepresentationType.REDUCED;
		} else if (minimizedRadioButton.isSelected()) {
			return BusinessLogic.HLDDRepresentationType.MINIMIZED;
		} else return BusinessLogic.HLDDRepresentationType.FULL_TREE;
	}

	public boolean shouldExpandCS() {
		return expandCSCheckBox.isSelected();
	}
}
