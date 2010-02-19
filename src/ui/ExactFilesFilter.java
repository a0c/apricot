package ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 16.12.2008
 * <br>Time: 22:10:42
 */
public class ExactFilesFilter extends FileFilter {
    private final String[] exactFileNames;
    private String description = null;

    /**
     * @param exactFileNames neither <code>null</code> nor null-length array is allowed.
     */
    public ExactFilesFilter(String... exactFileNames) {
        if (exactFileNames == null || exactFileNames.length == 0) {
            throw new RuntimeException("Unexpected bug: exactFileNames should contain at least 1 file name.");
        }
        this.exactFileNames = exactFileNames;
        /* Compose description string */
        StringBuilder builder = new StringBuilder();
        for (String exactFileName : exactFileNames) {
            builder.append(exactFileName).append("; ");
        }
        description = builder.substring(0, builder.lastIndexOf(";")).trim();
    }

    public boolean accept(File f) {
        return f.isDirectory() || isAcceptedFile(f.getName());
    }

    private boolean isAcceptedFile(String fileName) {
        for (String exactFileName : exactFileNames) {
            if (fileName.equalsIgnoreCase(exactFileName)) return true;
        }
        return false;
    }

    public String getDescription() {
        return description;
    }

}
