package ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Anton Chepurov
 */
public class MultipleFilesFilter extends FileFilter {

	private String[] acceptedExtensions;
	private String description;

	/**
	 * @param acceptedExtensions neither <code>null</code> nor null-length array is allowed.
	 */
	public MultipleFilesFilter(String... acceptedExtensions) {
		if (acceptedExtensions == null || acceptedExtensions.length == 0) {
			throw new RuntimeException("Unexpected bug: acceptedExtensions should contain at least 1 extension.");
		}
		this.acceptedExtensions = acceptedExtensions;
		/*  */
		StringBuilder stringBuilder = new StringBuilder();
		for (String acceptedExtension : acceptedExtensions) {
			stringBuilder.append("*.").append(acceptedExtension).append("; ");
		}
		description = stringBuilder.substring(0, stringBuilder.lastIndexOf(";")).trim();
	}

	@SuppressWarnings({"StandardVariableNames"})
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String extension = getExtension(f);
		return extension != null && isAcceptedExtension(extension);
	}

	/**
	 * @param file file to extract extension from
	 * @return <code>null</code> if the specified file doesn't contain extension, or its extension otherwise.
	 */
	private String getExtension(File file) {
		int lastIndex = file.getName().lastIndexOf(".");
		return lastIndex == -1 ? null : file.getName().substring(lastIndex + 1);
	}

	private boolean isAcceptedExtension(String extension) {
		for (String acceptedExtension : acceptedExtensions) {
			if (acceptedExtension.equals(extension)) return true;
		}
		return false;
	}

	public String getDescription() {
		return description;
	}

}
