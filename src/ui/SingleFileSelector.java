package ui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 28.07.2006
 * <br>Time: 15:26:09
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

    /* Disable multiple instantiation */
    private SingleFileSelector() {}

    public boolean isFileSelected() {
        return isFileSelected;
    }

//    public File getSelectedFile() {
//        return selectedFile.getName().contains(extensions) ?
//                selectedFile :
//                new File(selectedFile.getAbsolutePath() + "." + extensions);
//    }

    public File getRestrictedSelectedFile() {
        return selectedFile;
    }

    /**
     * @param extensions <code>null</code> to accept any extension
     * @param title
     * @param invalidFileMessage
     * @param dialogType OPEN or SAVE
     * @param proposedFileName
     * @return
     */
    public static SingleFileSelector getInstance(DialogType dialogType, String[] extensions, String proposedFileName,
                                          String title, String invalidFileMessage) {
        if (INSTANCE.selectedFile != null && !INSTANCE.selectedFile.exists()) {
            File file = INSTANCE.selectedFile.getParentFile();
            while (!file.exists()) {
                file = file.getParentFile();
            }
            JOptionPane.showMessageDialog(null, "Previously opened file is not found (was deleted probably). Going up to folder:\n" + file.toString(), "Notification", JOptionPane.INFORMATION_MESSAGE);
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
        if (dialogType == DialogType.OPEN_DIALOG) {
            selection = INSTANCE.fileChooser.showOpenDialog(null);
        } else if (dialogType == DialogType.SAVE_DIALOG) {
            selection = INSTANCE.fileChooser.showSaveDialog(null);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Wrong parameter for ShowDialog.\nMust be 'open' or 'save'. " ,
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
        OPEN_DIALOG, SAVE_DIALOG
    }

}
