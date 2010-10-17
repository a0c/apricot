package ui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Anton Chepurov
 */
public class SingleFileSelector {
	/* Static fields */
	public static final String INVALID_FILE_TEXT = "Incompatible file selected";
	private static final File NO_FILE = new File("");

	/* The only instance of the class */
	private static final SingleFileSelector INSTANCE = new SingleFileSelector();

	/* Instance fields */
	private File selectedFile;
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
	 * @param extensions		 <code>null</code> to accept any extension
	 * @param title dialog title
	 * @param invalidFileMessage message to show on invalid file selection
	 * @param dialogType		 OPEN or SAVE
	 * @param proposedFileName file to be selected by default
	 * @return file selector, modified by method parameters
	 */
	public static SingleFileSelector getInstance(DialogType dialogType, String[] extensions, String proposedFileName,
												 String title, String invalidFileMessage) {
		if (INSTANCE.selectedFile != null && !INSTANCE.selectedFile.exists()) {
			File file = INSTANCE.selectedFile.getParentFile();
			while (!file.exists()) {
				file = file.getParentFile();
			}
			INSTANCE.fileChooser.setCurrentDirectory(file);
		}
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
		if (proposedFileName == null) {
			INSTANCE.fileChooser.setSelectedFile(NO_FILE);
		} else {
			INSTANCE.fileChooser.setSelectedFile(new File(proposedFileName));
		}
		/* Show "OPEN/SAVE file" dialog */
		int selection = JFileChooser.CANCEL_OPTION;
		if (dialogType == DialogType.OPEN) {
			selection = INSTANCE.fileChooser.showOpenDialog(null);
		} else if (dialogType == DialogType.SAVE) {
			selection = INSTANCE.fileChooser.showSaveDialog(null);
		} else {
			JOptionPane.showMessageDialog(null,
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
