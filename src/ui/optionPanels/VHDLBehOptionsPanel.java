package ui.optionPanels;

import ui.ApplicationForm.FileDropHandler;
import ui.BusinessLogic.HLDDRepresentationType;
import ui.OutputFileGenerator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Anton Chepurov
 */
public class VHDLBehOptionsPanel {
	private JRadioButton flattenRadioButton;
	private JPanel mainPanel;
	private JRadioButton reducedRadioButton;
	private JRadioButton minimizedRadioButton;
	private JRadioButton asGraphsRadioButton;
	private JRadioButton extraGraphsRadioButton;
	private JRadioButton asFunctionRadioButton;
	private JRadioButton fullRadioButton;
	private JRadioButton fullTree4RTLRadioButton;


	public VHDLBehOptionsPanel(OutputFileGenerator outputFileGenerator, FileDropHandler fileDropHandler) {
		fullTree4RTLRadioButton.addChangeListener(outputFileGenerator);
		fullRadioButton.addChangeListener(outputFileGenerator);
		reducedRadioButton.addChangeListener(outputFileGenerator);
		minimizedRadioButton.addChangeListener(outputFileGenerator);
		asFunctionRadioButton.addChangeListener(outputFileGenerator);
		asGraphsRadioButton.addChangeListener(outputFileGenerator);
		flattenRadioButton.addChangeListener(outputFileGenerator);
		extraGraphsRadioButton.addChangeListener(outputFileGenerator);

		fullTree4RTLRadioButton.addKeyListener(fileDropHandler);
		fullRadioButton.addKeyListener(fileDropHandler);
		reducedRadioButton.addKeyListener(fileDropHandler);
		minimizedRadioButton.addKeyListener(fileDropHandler);
		asFunctionRadioButton.addKeyListener(fileDropHandler);
		extraGraphsRadioButton.addKeyListener(fileDropHandler);
		asGraphsRadioButton.addKeyListener(fileDropHandler);
		flattenRadioButton.addKeyListener(fileDropHandler);

		fullTree4RTLRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fullTree4RTLRadioButton.isSelected()) {
					String message = "This option requires a design with *exactly 1* process inside, and" +
							"\nshould only be used when RTL generation fails due to FSM explosion" +
							"\n(or whenever a compact FSM graph is preferred)." +
							"\n" +
							"\nThis option avoids FSM explosion by producing a true Full-tree structure" +
							"\nwhere the *entire* process structure is replicated in *all* graphs as is." +
							"\nThis prevents most of FSM merging from happening." +
							"\n(Note that 'Full-tree' option trims empty branches by default, which" +
							"\ncauses FSM explosion during merging of many different graphs.)" +
							"\n" +
							"\nIf you proceed with converting a multi-process design, RTL generation" +
							"\nmay still fail due to explosion, while the size of resulting 'Full-tree 4 RTL'" +
							"\nwill be considerably larger than that of a simple 'Full-tree'." +
							"\nYou'll be warned once more if multiple processes are detected." +
							"\n\nDo you want to proceed?";
					int answer = JOptionPane.showConfirmDialog(null, message, "Warning",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (answer != JOptionPane.YES_OPTION) {
						minimizedRadioButton.setSelected(true);
					}
				}
			}
		});
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public boolean shouldFlattenCS() {
		return flattenRadioButton.isSelected();
	}

	public boolean shouldCreateCSGraphs() {
		return asGraphsRadioButton.isSelected();
	}

	public boolean shouldCreateExtraCSGraphs() {
		return extraGraphsRadioButton.isSelected();
	}

	public HLDDRepresentationType getHlddType() {
		if (reducedRadioButton.isSelected()) {
			return HLDDRepresentationType.REDUCED;
		} else if (minimizedRadioButton.isSelected()) {
			return HLDDRepresentationType.MINIMIZED;
		} else if (fullTree4RTLRadioButton.isSelected()) {
			return HLDDRepresentationType.FULL_TREE_4_RTL;
		} else return HLDDRepresentationType.FULL_TREE;
	}
}
