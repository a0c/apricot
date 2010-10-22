package ui;

import java.io.File;

/**
 * @author Anton Chepurov
 */
public class FileDependencyResolver {

	public static File deriveVhdlFile(File hlddFile) {

		File vhdlFile = null;

		ConverterSettings settings = ConverterSettings.loadSmartComment(hlddFile);

		if (settings != null) {

			vhdlFile = settings.getSourceFile();
		}

		if (vhdlFile == null)
			vhdlFile = deriveFileFrom(hlddFile, ".agm", ".vhdl");

		if (vhdlFile == null)
			vhdlFile = deriveFileFrom(hlddFile, ".agm", ".vhd");

		return vhdlFile;
	}

	public static File deriveCovFile(File hlddFile) {

		return deriveFileFrom(hlddFile, ".agm", ".cov");
	}

	public static File deriveTstFile(File hlddFile) {

		return deriveFileFrom(hlddFile, ".agm", ".tst");
	}

	public static File deriveBaseModelFile(File pslFile) {

		return deriveFileFrom(pslFile, ".psl", ".agm");
	}

	public static File deriveTgmFile(File hlddFile) {

		return deriveFileFrom(hlddFile, ".agm", ".tgm");
	}

	@Deprecated
	//todo: method used for temporal fix
	public static File deriveMapFile(File covFile) {

		return deriveFileFrom(covFile, ".cov", ".map");
	}

	//todo:...

	public static File derivePngFile(File hlddFile) {

		return deriveFileFrom(hlddFile, ".agm", ".png");
	}

	public static File deriveConfigFile(File vhdlFile) {

		File configFile = null;
		try {
			configFile = deriveFileFrom(vhdlFile, ".vhd", ".config");
		} catch (RuntimeException e) {
			// do nothing, leave configFile == null
		}

		if (configFile == null) {
			try {
				configFile = deriveFileFrom(vhdlFile, ".vhdl", ".config");
			} catch (RuntimeException e) {
				// do nothing, leave configFile == null
			}
		}

		return configFile;
	}

	/**
	 * @param sourceFile		   file to derive from
	 * @param sourceFileExtension  extension to replace
	 * @param derivedFileExtension target extension
	 * @return derived file, if it exists, or <code>null</code> if it doesn't exist.
	 */
	public static File deriveFileFrom(File sourceFile, String sourceFileExtension, String derivedFileExtension) {
		String sourcePath = sourceFile.getAbsolutePath();
		if (sourcePath.endsWith(sourceFileExtension)) {
			File derivedFile = new File(sourcePath.replace(sourceFileExtension, derivedFileExtension));
			return derivedFile.exists() ? derivedFile : null;
		} else {
			throw new RuntimeException("Mismatch between real sourceFile extension and provided sourceFileExtension: \"" +
					sourcePath + "\" and \"" + sourceFileExtension + "\"");
		}
	}

}
