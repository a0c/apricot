package ui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

/**
 * @author Anton Chepurov
 */
public class SingleFileSelector {
	/* Static fields */
	public static final String INVALID_FILE_TEXT = "Incompatible file selected";

	/* The only instance of the class */
	private static final SingleFileSelector INSTANCE = new SingleFileSelector();

	/* Instance fields */
	private File selectedFile = null;
	private String[] extensions;
	private String invalidFileMessage;
	private JFileChooser fileChooser = new JFileChooser(File.listRoots()[0]);
	private FileFilter fileFilter;
	private boolean isFileSelected = false;

	/**
	 * Disable multiple instantiation
	 */
	private SingleFileSelector() {
	}

	public boolean isFileSelected() {
		return isFileSelected;
	}

	public File getRestrictedSelectedFile() {
		return selectedFile;
	}

	/**
	 * @param extensions <code>null</code> to accept any extension
	 * @param title	dialog title
	 * @param invalidFileMessage message to show on invalid file selection
	 * @param parent parent to take icon from
	 * @param dialogType OPEN or SAVE
	 * @param proposedFileName file to be selected by default
	 * @return file selector, modified by method parameters
	 */
	public static SingleFileSelector getInstance(DialogType dialogType, String[] extensions, String proposedFileName,
												 String title, String invalidFileMessage, Component parent) {

		setCurrentDirectory(INSTANCE.selectedFile);

		INSTANCE.isFileSelected = false;
		INSTANCE.extensions = extensions;
		INSTANCE.invalidFileMessage = invalidFileMessage;
		/* Compose and Set DIALOG_TITLE. */
		/* Remove old file filter and create a new one */
		INSTANCE.fileChooser.removeChoosableFileFilter(INSTANCE.fileFilter);
		String dialogTitle = title;
		if (!isArbitraryExceptionAllowed(extensions)) {
			INSTANCE.fileFilter = createFileFilter(extensions);
			INSTANCE.fileChooser.addChoosableFileFilter(INSTANCE.fileFilter);
			dialogTitle += " (" + INSTANCE.fileFilter.getDescription() + ")";
		}
		INSTANCE.fileChooser.setDialogTitle(dialogTitle);

		/* Select proposed file or remove selection of already set file */
		if (proposedFileName != null) {
			INSTANCE.fileChooser.setSelectedFile(new File(proposedFileName));
		}
		/* Show "OPEN/SAVE file" dialog */
		int selection = JFileChooser.CANCEL_OPTION;
		if (dialogType == DialogType.OPEN) {
			selection = INSTANCE.fileChooser.showOpenDialog(parent);
		} else if (dialogType == DialogType.SAVE) {
			selection = INSTANCE.fileChooser.showSaveDialog(parent);
		} else {
			JOptionPane.showMessageDialog(parent,
					"Wrong parameter for ShowDialog.\nMust be 'open' or 'save'. ",
					"Error",
					JOptionPane.WARNING_MESSAGE);
		}
		/* Remember user's choice */
		if (selection == JFileChooser.APPROVE_OPTION) {
			INSTANCE.selectedFile = INSTANCE.fileChooser.getSelectedFile();
			INSTANCE.isFileSelected = true;
		}
		return INSTANCE;
	}

	public static void setCurrentDirectory(File currentFile) {
		if (currentFile != null) {
			while (currentFile != null && !currentFile.exists()) {
				currentFile = currentFile.getParentFile();
			}
			if (currentFile != null) {
				INSTANCE.fileChooser.setCurrentDirectory(currentFile);
			}
		}
		// default directory fileChooser is initialised to on Win is C:\\
	}

	private static FileFilter createFileFilter(String[] extensions) {
		return extensions[0].contains(".") ? new ExactFilesFilter(extensions)
				: new MultipleFilesFilter(extensions);
	}

	/**
	 * Checks the selected file to comply with the specified extensions.
	 * If the file doesn't comply, an {@link ui.ExtendedException} is thrown.
	 *
	 * @throws ExtendedException if the selected file doesn't comply with the specified extensions
	 */
	public void validateFile() throws ExtendedException {
		if (isFileSelected) {
			if (!isArbitraryExceptionAllowed(extensions) && !fileChooser.accept(selectedFile)) {
				throw new ExtendedException(invalidFileMessage, INVALID_FILE_TEXT);
			}
		}
	}

	private static boolean isArbitraryExceptionAllowed(String[] acceptedExtensions) {
		return acceptedExtensions == null;
	}

	public enum DialogType {
		OPEN, SAVE
	}

}
